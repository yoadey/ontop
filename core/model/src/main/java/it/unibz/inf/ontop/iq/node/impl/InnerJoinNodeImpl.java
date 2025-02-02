package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.*;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.evaluator.TermNullabilityEvaluator;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.node.normalization.ConditionSimplifier.ExpressionAndSubstitution;
import it.unibz.inf.ontop.iq.node.normalization.ConditionSimplifier;
import it.unibz.inf.ontop.iq.node.normalization.InnerJoinNormalizer;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.substitution.impl.ImmutableSubstitutionTools;
import it.unibz.inf.ontop.substitution.impl.ImmutableUnificationTools;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;


public class InnerJoinNodeImpl extends JoinLikeNodeImpl implements InnerJoinNode {

    private static final String JOIN_NODE_STR = "JOIN" ;
    private final ConstructionNodeTools constructionNodeTools;
    private final JoinOrFilterVariableNullabilityTools variableNullabilityTools;
    private final ConditionSimplifier conditionSimplifier;
    private final InnerJoinNormalizer normalizer;

    @AssistedInject
    protected InnerJoinNodeImpl(@Assisted Optional<ImmutableExpression> optionalFilterCondition,
                                TermNullabilityEvaluator nullabilityEvaluator,
                                TermFactory termFactory, TypeFactory typeFactory,
                                IntermediateQueryFactory iqFactory, SubstitutionFactory substitutionFactory,
                                ConstructionNodeTools constructionNodeTools,
                                ImmutableUnificationTools unificationTools, ImmutableSubstitutionTools substitutionTools,
                                JoinOrFilterVariableNullabilityTools variableNullabilityTools, ConditionSimplifier conditionSimplifier,
                                InnerJoinNormalizer normalizer) {
        super(optionalFilterCondition, nullabilityEvaluator, termFactory, iqFactory, typeFactory,
                substitutionFactory, unificationTools, substitutionTools);
        this.constructionNodeTools = constructionNodeTools;
        this.variableNullabilityTools = variableNullabilityTools;
        this.conditionSimplifier = conditionSimplifier;
        this.normalizer = normalizer;
    }

    @AssistedInject
    private InnerJoinNodeImpl(@Assisted ImmutableExpression joiningCondition,
                              TermNullabilityEvaluator nullabilityEvaluator,
                              TermFactory termFactory, TypeFactory typeFactory,
                              IntermediateQueryFactory iqFactory, SubstitutionFactory substitutionFactory,
                              ConstructionNodeTools constructionNodeTools,
                              ImmutableUnificationTools unificationTools, ImmutableSubstitutionTools substitutionTools,
                              JoinOrFilterVariableNullabilityTools variableNullabilityTools, ConditionSimplifier conditionSimplifier, InnerJoinNormalizer normalizer) {
        super(Optional.of(joiningCondition), nullabilityEvaluator, termFactory, iqFactory, typeFactory,
                substitutionFactory, unificationTools, substitutionTools);
        this.constructionNodeTools = constructionNodeTools;
        this.variableNullabilityTools = variableNullabilityTools;
        this.conditionSimplifier = conditionSimplifier;
        this.normalizer = normalizer;
    }

    @AssistedInject
    private InnerJoinNodeImpl(TermNullabilityEvaluator nullabilityEvaluator, TermFactory termFactory,
                              TypeFactory typeFactory, IntermediateQueryFactory iqFactory,
                              SubstitutionFactory substitutionFactory, ConstructionNodeTools constructionNodeTools,
                              ImmutableUnificationTools unificationTools, ImmutableSubstitutionTools substitutionTools,
                              JoinOrFilterVariableNullabilityTools variableNullabilityTools, ConditionSimplifier conditionSimplifier, InnerJoinNormalizer normalizer) {
        super(Optional.empty(), nullabilityEvaluator, termFactory, iqFactory, typeFactory,
                substitutionFactory, unificationTools, substitutionTools);
        this.constructionNodeTools = constructionNodeTools;
        this.variableNullabilityTools = variableNullabilityTools;
        this.conditionSimplifier = conditionSimplifier;
        this.normalizer = normalizer;
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public InnerJoinNode clone() {
        return iqFactory.createInnerJoinNode(getOptionalFilterCondition());
    }

    @Override
    public InnerJoinNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer)
            throws QueryNodeTransformationException {
        return transformer.transform(this);
    }

    @Override
    public ImmutableSet<ImmutableSubstitution<NonVariableTerm>> getPossibleVariableDefinitions(ImmutableList<IQTree> children) {
        return children.stream()
                .map(IQTree::getPossibleVariableDefinitions)
                .filter(s -> !s.isEmpty())
                .reduce(ImmutableSet.of(), this::combineVarDefs);
    }

    private ImmutableSet<ImmutableSubstitution<NonVariableTerm>> combineVarDefs(
            ImmutableSet<ImmutableSubstitution<NonVariableTerm>> s1,
            ImmutableSet<ImmutableSubstitution<NonVariableTerm>> s2) {
        return s1.isEmpty()
                ? s2
                : s1.stream()
                    .flatMap(d1 -> s2.stream()
                        /*
                         * Takes the first definition of a common variable.
                         *
                         * Behaves like an union except that is robust to "non-identical" definitions.
                         * If normalized, two definitions for the same variables are expected to be compatible.
                         *
                         * If not normalized, the definitions may be incompatible, but that's fine
                         * since they will not produce any result.
                         *
                         */
                        .map(d2 -> d2.composeWith2(d1)))
                    .collect(ImmutableCollectors.toSet());
    }


    @Override
    public InnerJoinNode changeOptionalFilterCondition(Optional<ImmutableExpression> newOptionalFilterCondition) {
        return iqFactory.createInnerJoinNode(newOptionalFilterCondition);
    }

    @Override
    public boolean isVariableNullable(IntermediateQuery query, Variable variable) {

        if (isFilteringNullValue(variable))
            return false;

        // Non-already
        boolean alsoProjectedByAnotherChild = false;

        for(QueryNode child : query.getChildren(this)) {
            if (query.getVariables(child).contains(variable)) {
                // Joining conditions cannot be null
                if (alsoProjectedByAnotherChild)
                    return false;

                if (child.isVariableNullable(query, variable))
                    alsoProjectedByAnotherChild = true;
                else
                    return false;
            }
        }

        if (!alsoProjectedByAnotherChild)
            throw new IllegalArgumentException("The variable " + variable + " is not projected by " + this);

        return true;
    }

    @Override
    public boolean isSyntacticallyEquivalentTo(QueryNode node) {
        return (node instanceof InnerJoinNode) &&
            this.getOptionalFilterCondition().equals(((InnerJoinNode) node).getOptionalFilterCondition());
    }

    @Override
    public boolean isEquivalentTo(QueryNode queryNode) {
        return (queryNode instanceof InnerJoinNode)
                && getOptionalFilterCondition().equals(((InnerJoinNode) queryNode).getOptionalFilterCondition());
    }

    @Override
    public String toString() {
        return JOIN_NODE_STR + getOptionalFilterString();
    }

    /**
     * TODO: refactor
     */
    @Override
    public IQTree normalizeForOptimization(ImmutableList<IQTree> children, VariableGenerator variableGenerator,
                                           IQProperties currentIQProperties) {
        return normalizer.normalizeForOptimization(this, children, variableGenerator, currentIQProperties);
    }

    @Override
    public IQTree applyDescendingSubstitution(ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution,
                                              Optional<ImmutableExpression> constraint, ImmutableList<IQTree> children) {

        Optional<ImmutableExpression> unoptimizedExpression = getOptionalFilterCondition()
                .map(descendingSubstitution::applyToBooleanExpression);


        VariableNullability dummyVariableNullability = variableNullabilityTools.getDummyVariableNullability(
                constructionNodeTools.computeNewProjectedVariables(descendingSubstitution, getProjectedVariables(children)));

        try {
            ExpressionAndSubstitution expressionAndSubstitution = conditionSimplifier.simplifyCondition(
                    unoptimizedExpression, ImmutableSet.of(), dummyVariableNullability);

            Optional<ImmutableExpression> downConstraint = conditionSimplifier.computeDownConstraint(constraint,
                    expressionAndSubstitution, dummyVariableNullability);

            ImmutableSubstitution<? extends VariableOrGroundTerm> downSubstitution =
                    ((ImmutableSubstitution<VariableOrGroundTerm>)descendingSubstitution)
                            .composeWith2(expressionAndSubstitution.getSubstitution());

            ImmutableList<IQTree> newChildren = children.stream()
                    .map(c -> c.applyDescendingSubstitution(downSubstitution, downConstraint))
                    .collect(ImmutableCollectors.toList());

            IQTree joinTree = iqFactory.createNaryIQTree(
                    iqFactory.createInnerJoinNode(expressionAndSubstitution.getOptionalExpression()),
                    newChildren);
            return expressionAndSubstitution.getSubstitution().isEmpty()
                    ? joinTree
                    : iqFactory.createUnaryIQTree(
                    iqFactory.createConstructionNode(
                            constructionNodeTools.computeNewProjectedVariables(descendingSubstitution,
                                    getProjectedVariables(children)),
                            (ImmutableSubstitution<ImmutableTerm>)(ImmutableSubstitution<?>)
                                    expressionAndSubstitution.getSubstitution()),
                    joinTree);
        } catch (UnsatisfiableConditionException e) {
            return iqFactory.createEmptyNode(computeNewlyProjectedVariables(descendingSubstitution, children));
        }
    }

    @Override
    public IQTree applyDescendingSubstitutionWithoutOptimizing(
            ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution, ImmutableList<IQTree> children) {

        InnerJoinNode newJoinNode = getOptionalFilterCondition()
                .map(descendingSubstitution::applyToBooleanExpression)
                .map(iqFactory::createInnerJoinNode)
                .orElseGet(iqFactory::createInnerJoinNode);

        ImmutableList<IQTree> newChildren = children.stream()
                .map(c -> c.applyDescendingSubstitutionWithoutOptimizing(descendingSubstitution))
                .collect(ImmutableCollectors.toList());

        return iqFactory.createNaryIQTree(newJoinNode, newChildren);
    }

    private ImmutableSet<Variable> getProjectedVariables(ImmutableList<IQTree> children) {
        return children.stream()
                    .flatMap(c -> c.getVariables().stream())
                    .collect(ImmutableCollectors.toSet());
    }

    @Override
    public VariableNullability getVariableNullability(ImmutableList<IQTree> children) {
        return variableNullabilityTools.getVariableNullability(children, getOptionalFilterCondition());
    }

    @Override
    public boolean isConstructed(Variable variable, ImmutableList<IQTree> children) {
        return children.stream()
                .anyMatch(c -> c.isConstructed(variable));
    }

    @Override
    public boolean isDistinct(ImmutableList<IQTree> children) {
        return children.stream().allMatch(IQTree::isDistinct);
    }

    @Override
    public IQTree liftIncompatibleDefinitions(Variable variable, ImmutableList<IQTree> children, VariableGenerator variableGenerator) {
        return IntStream.range(0, children.size()).boxed()
                .map(i -> Maps.immutableEntry(i, children.get(i)))
                .filter(e -> e.getValue().isConstructed(variable))
                // index -> new child
                .map(e -> Maps.immutableEntry(e.getKey(), e.getValue().liftIncompatibleDefinitions(variable, variableGenerator)))
                .filter(e -> {
                            QueryNode newRootNode = e.getValue().getRootNode();
                            return (newRootNode instanceof UnionNode)
                                    && ((UnionNode) newRootNode).hasAChildWithLiftableDefinition(variable,
                                    e.getValue().getChildren());
                })
                .findFirst()
                .map(e -> liftUnionChild(e.getKey(), (NaryIQTree) e.getValue(), children, variableGenerator))
                .orElseGet(() -> iqFactory.createNaryIQTree(this, children));
    }

    @Override
    public IQTree propagateDownConstraint(ImmutableExpression constraint, ImmutableList<IQTree> children) {
        return propagateDownCondition(Optional.of(constraint), children);
    }

    @Override
    public IQTree acceptTransformer(IQTree tree, IQTreeVisitingTransformer transformer, ImmutableList<IQTree> children) {
        return transformer.transformInnerJoin(tree,this, children);
    }

    @Override
    public <T> T acceptVisitor(IQVisitor<T> visitor, ImmutableList<IQTree> children) {
        return visitor.visitInnerJoin(this, children);
    }

    @Override
    public void validateNode(ImmutableList<IQTree> children) throws InvalidIntermediateQueryException {
        if (children.size() < 2) {
            throw new InvalidIntermediateQueryException("JOIN node " + this
                    +" does not have at least 2 children.\n" + children);
        }

        getOptionalFilterCondition()
                .ifPresent(e -> checkExpression(e, children));

        checkNonProjectedVariables(children);
    }

    @Override
    public IQTree removeDistincts(ImmutableList<IQTree> children, IQProperties properties) {
        ImmutableList<IQTree> newChildren = children.stream()
                .map(IQTree::removeDistincts)
                .collect(ImmutableCollectors.toList());

        IQProperties newProperties = newChildren.equals(children)
                ? properties.declareDistinctRemovalWithoutEffect()
                : properties.declareDistinctRemovalWithEffect();

        return iqFactory.createNaryIQTree(this, children, newProperties);
    }

    /**
     * For unique constraints to emerge from an inner join, children must provide unique constraints
     * and being naturally joined over some of such constraints.
     */
    @Override
    public ImmutableSet<ImmutableSet<Variable>> inferUniqueConstraints(ImmutableList<IQTree> children) {

        ImmutableSet<IQTree> childrenSet = ImmutableSet.copyOf(children);

        ImmutableMap<IQTree, ImmutableSet<ImmutableSet<Variable>>> constraintMap = childrenSet.stream()
                .collect(ImmutableCollectors.toMap(
                        c -> c,
                        IQTree::inferUniqueConstraints));

        /*
         * Pre-condition: all the children must have at least one unique constraint
         */
        if (constraintMap.values().stream().anyMatch(AbstractCollection::isEmpty))
            return ImmutableSet.of();

        // Non-saturated
        ImmutableMultimap<IQTree, IQTree> directDependencyMap = IntStream.range(0, children.size() - 1)
                .boxed()
                .flatMap(i -> IntStream.range(i +1, children.size())
                        .boxed()
                        .flatMap(j -> extractFunctionalDependencies(children.get(i), children.get(j), constraintMap)))
                .collect(ImmutableCollectors.toMultimap());

        Multimap<IQTree, IQTree> saturatedDependencyMap = saturateDependencies(directDependencyMap);

        return saturatedDependencyMap.asMap().entrySet().stream()
                .filter(e -> e.getValue().containsAll(Sets.difference(childrenSet, ImmutableSet.of(e.getKey())).immutableCopy()))
                .map(Map.Entry::getKey)
                .flatMap(child -> constraintMap.get(child).stream())
                .collect(ImmutableCollectors.toSet());
    }

    private Stream<Map.Entry<IQTree, IQTree>> extractFunctionalDependencies(
            IQTree t1, IQTree t2, ImmutableMap<IQTree, ImmutableSet<ImmutableSet<Variable>>> constraintMap) {
        ImmutableSet<Variable> commonVariables = Sets.intersection(t1.getVariables(), t2.getVariables())
                .immutableCopy();
        if (commonVariables.isEmpty())
            return Stream.empty();

        return Stream.of(
                Optional.of(Maps.immutableEntry(t1, t2))
                        .filter(e -> constraintMap.get(e.getValue()).stream()
                                .anyMatch(commonVariables::containsAll)),
                Optional.of(Maps.immutableEntry(t2, t1))
                        .filter(e -> constraintMap.get(e.getValue()).stream()
                                .anyMatch(commonVariables::containsAll)))
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    private Multimap<IQTree, IQTree> saturateDependencies(ImmutableMultimap<IQTree, IQTree> directDependencyMap) {
        Multimap<IQTree, IQTree> mutableMultimap = HashMultimap.create(directDependencyMap);

        boolean hasConverged = false;
        while (!hasConverged) {
            hasConverged = true;

            for (IQTree determinant : directDependencyMap.keys()) {
                ImmutableSet<IQTree> dependents = ImmutableSet.copyOf(mutableMultimap.get(determinant));
                for (IQTree dependent : dependents) {
                    if (mutableMultimap.putAll(determinant, mutableMultimap.get(dependent)))
                        hasConverged = false;
                }
            }
        }
        return mutableMultimap;
    }


    private IQTree propagateDownCondition(Optional<ImmutableExpression> initialConstraint, ImmutableList<IQTree> children) {
        VariableNullability childrenVariableNullability = variableNullabilityTools.getChildrenVariableNullability(children);

        try {
            ExpressionAndSubstitution conditionSimplificationResults = conditionSimplifier.simplifyCondition(
                    getOptionalFilterCondition(), ImmutableSet.of(), childrenVariableNullability);

            // TODO: find a way to avoid creating dummy objects
            // NB: if some variables are not nullable at the join level, they may be at the child level
            VariableNullability dummyVariableNullability = variableNullabilityTools
                    .getDummyVariableNullability(getProjectedVariables(children));

            Optional<ImmutableExpression> downConstraint = conditionSimplifier.computeDownConstraint(initialConstraint,
                    conditionSimplificationResults, dummyVariableNullability);

            //TODO: propagate different constraints to different children

            ImmutableList<IQTree> newChildren = Optional.of(conditionSimplificationResults.getSubstitution())
                    .filter(s -> !s.isEmpty())
                    .map(s -> children.stream()
                            .map(child -> child.applyDescendingSubstitution(s, downConstraint))
                            .collect(ImmutableCollectors.toList())
                    )
                    .orElseGet(() -> downConstraint
                            .map(s -> children.stream()
                                    .map(child -> child.propagateDownConstraint(s))
                                    .collect(ImmutableCollectors.toList()))
                            .orElse(children));

            InnerJoinNode newJoin = conditionSimplificationResults.getOptionalExpression().equals(getOptionalFilterCondition())
                    ? this
                    : conditionSimplificationResults.getOptionalExpression()
                    .map(iqFactory::createInnerJoinNode)
                    .orElseGet(iqFactory::createInnerJoinNode);

            NaryIQTree joinTree = iqFactory.createNaryIQTree(newJoin, newChildren);

            return Optional.of(conditionSimplificationResults.getSubstitution())
                    .filter(s -> !s.isEmpty())
                    .map(s -> iqFactory.createConstructionNode(children.stream()
                            .flatMap(c -> c.getVariables().stream())
                            .collect(ImmutableCollectors.toSet()),
                            (ImmutableSubstitution<ImmutableTerm>)(ImmutableSubstitution<?>)s))
                    .map(c -> (IQTree) iqFactory.createUnaryIQTree(c, joinTree))
                    .orElse(joinTree);

        } catch (UnsatisfiableConditionException e) {
            return iqFactory.createEmptyNode(getProjectedVariables(children));
        }
    }

    private IQTree liftUnionChild(int childIndex, NaryIQTree newUnionChild, ImmutableList<IQTree> initialChildren,
                                  VariableGenerator variableGenerator) {
        UnionNode newUnionNode = iqFactory.createUnionNode(initialChildren.stream()
                .flatMap(c -> c.getVariables().stream())
                .collect(ImmutableCollectors.toSet()));

        return iqFactory.createNaryIQTree(newUnionNode,
                newUnionChild.getChildren().stream()
                        .map(unionGrandChild -> createJoinSubtree(childIndex, unionGrandChild, initialChildren))
                        .collect(ImmutableCollectors.toList()))
                .normalizeForOptimization(variableGenerator);
    }

    private IQTree createJoinSubtree(int childIndex, IQTree unionGrandChild, ImmutableList<IQTree> initialChildren) {
        return iqFactory.createNaryIQTree(this,
                IntStream.range(0, initialChildren.size())
                        .boxed()
                        .map(i -> i == childIndex
                                ? unionGrandChild
                                : initialChildren.get(i))
                        .collect(ImmutableCollectors.toList()));
    }

    private ImmutableSet<Variable> computeNewlyProjectedVariables(
            ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution,
            ImmutableList<IQTree> children) {
        ImmutableSet<Variable> formerProjectedVariables = getProjectedVariables(children);

        return constructionNodeTools.computeNewProjectedVariables(descendingSubstitution, formerProjectedVariables);
    }

 }
