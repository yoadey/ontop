package it.unibz.inf.ontop.iq.node.impl;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.evaluator.TermNullabilityEvaluator;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.node.normalization.ConditionSimplifier.ExpressionAndSubstitution;
import it.unibz.inf.ontop.iq.node.normalization.ConditionSimplifier;
import it.unibz.inf.ontop.iq.node.normalization.FilterNormalizer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.substitution.impl.ImmutableSubstitutionTools;
import it.unibz.inf.ontop.substitution.impl.ImmutableUnificationTools;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Optional;


public class FilterNodeImpl extends JoinOrFilterNodeImpl implements FilterNode {

    private static final String FILTER_NODE_STR = "FILTER";
    private final ConstructionNodeTools constructionNodeTools;
    private final ConditionSimplifier conditionSimplifier;
    private final CoreUtilsFactory coreUtilsFactory;
    private final FilterNormalizer normalizer;
    private final JoinOrFilterVariableNullabilityTools variableNullabilityTools;

    @AssistedInject
    private FilterNodeImpl(@Assisted ImmutableExpression filterCondition, TermNullabilityEvaluator nullabilityEvaluator,
                           TermFactory termFactory, TypeFactory typeFactory, SubstitutionFactory substitutionFactory,
                           ImmutableUnificationTools unificationTools, ImmutableSubstitutionTools substitutionTools,
                           IntermediateQueryFactory iqFactory,
                           ConstructionNodeTools constructionNodeTools, ConditionSimplifier conditionSimplifier,
                           CoreUtilsFactory coreUtilsFactory, FilterNormalizer normalizer, JoinOrFilterVariableNullabilityTools variableNullabilityTools) {
        super(Optional.of(filterCondition), nullabilityEvaluator, termFactory, iqFactory, typeFactory,
                substitutionFactory, unificationTools, substitutionTools);
        this.constructionNodeTools = constructionNodeTools;
        this.conditionSimplifier = conditionSimplifier;
        this.coreUtilsFactory = coreUtilsFactory;
        this.normalizer = normalizer;
        this.variableNullabilityTools = variableNullabilityTools;
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public FilterNode clone() {
        return iqFactory.createFilterNode(getFilterCondition());
    }

    @Override
    public FilterNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer) throws QueryNodeTransformationException {
        return transformer.transform(this);
    }

    @Override
    public ImmutableExpression getFilterCondition() {
        return getOptionalFilterCondition().get();
    }

    @Override
    public FilterNode changeFilterCondition(ImmutableExpression newFilterCondition) {
        return iqFactory.createFilterNode(newFilterCondition);
    }

    @Override
    public boolean isVariableNullable(IntermediateQuery query, Variable variable) {
        if (isFilteringNullValue(variable))
            return false;

        return query.getFirstChild(this)
                .map(c -> c.isVariableNullable(query, variable))
                .orElseThrow(() -> new InvalidIntermediateQueryException("A filter node must have a child"));
    }

    @Override
    public VariableNullability getVariableNullability(IQTree child) {
        return variableNullabilityTools.updateWithFilter(getFilterCondition(),
                child.getVariableNullability().getNullableGroups(), child.getVariables());
    }


    @Override
    public IQTree liftIncompatibleDefinitions(Variable variable, IQTree child, VariableGenerator variableGenerator) {
        IQTree newChild = child.liftIncompatibleDefinitions(variable, variableGenerator);
        QueryNode newChildRoot = newChild.getRootNode();

        /*
         * Lift the union above the filter
         */
        if ((newChildRoot instanceof UnionNode)
                && ((UnionNode) newChildRoot).hasAChildWithLiftableDefinition(variable, newChild.getChildren())) {
            UnionNode unionNode = (UnionNode) newChildRoot;
            ImmutableList<IQTree> grandChildren = newChild.getChildren();

            ImmutableList<IQTree> newChildren = grandChildren.stream()
                    .map(c -> (IQTree) iqFactory.createUnaryIQTree(this, c))
                    .collect(ImmutableCollectors.toList());

            return iqFactory.createNaryIQTree(unionNode, newChildren);
        }
        return iqFactory.createUnaryIQTree(this, newChild);
    }

    @Override
    public IQTree propagateDownConstraint(ImmutableExpression constraint, IQTree child) {
        return propagateDownCondition(child, Optional.of(constraint));
    }

    private IQTree propagateDownCondition(IQTree child, Optional<ImmutableExpression> initialConstraint) {
        try {
            VariableNullability childVariableNullability = child.getVariableNullability();

            // TODO: also consider the constraint for simplifying the condition
            ExpressionAndSubstitution conditionSimplificationResults = conditionSimplifier
                    .simplifyCondition(getFilterCondition(), childVariableNullability);

            Optional<ImmutableExpression> downConstraint = conditionSimplifier.computeDownConstraint(initialConstraint,
                    conditionSimplificationResults, childVariableNullability);

            IQTree newChild = Optional.of(conditionSimplificationResults.getSubstitution())
                    .filter(s -> !s.isEmpty())
                    .map(s -> child.applyDescendingSubstitution(s, downConstraint))
                    .orElseGet(() -> downConstraint
                            .map(child::propagateDownConstraint)
                            .orElse(child));

            IQTree filterLevelTree = conditionSimplificationResults.getOptionalExpression()
                    .map(e -> e.equals(getFilterCondition()) ? this : iqFactory.createFilterNode(e))
                    .map(filterNode -> (IQTree) iqFactory.createUnaryIQTree(filterNode, newChild))
                    .orElse(newChild);

            return Optional.of(conditionSimplificationResults.getSubstitution())
                    .filter(s -> !s.isEmpty())
                    .map(s -> (ImmutableSubstitution<ImmutableTerm>)(ImmutableSubstitution<?>)s)
                    .map(s -> iqFactory.createConstructionNode(child.getVariables(), s))
                    .map(c -> (IQTree) iqFactory.createUnaryIQTree(c, filterLevelTree))
                    .orElse(filterLevelTree);


        } catch (UnsatisfiableConditionException e) {
            return iqFactory.createEmptyNode(child.getVariables());
        }

    }

    @Override
    public IQTree acceptTransformer(IQTree tree, IQTreeVisitingTransformer transformer, IQTree child) {
        return transformer.transformFilter(tree,this, child);
    }

    @Override
    public <T> T acceptVisitor(IQVisitor<T> visitor, IQTree child) {
        return visitor.visitFilter(this, child);
    }

    @Override
    public void validateNode(IQTree child) throws InvalidIntermediateQueryException {
        checkExpression(getFilterCondition(), ImmutableList.of(child));
    }

    @Override
    public ImmutableSet<ImmutableSubstitution<NonVariableTerm>> getPossibleVariableDefinitions(IQTree child) {
        return child.getPossibleVariableDefinitions();
    }

    @Override
    public IQTree removeDistincts(IQTree child, IQProperties iqProperties) {
        IQTree newChild = child.removeDistincts();

        IQProperties newProperties = newChild.equals(child)
                ? iqProperties.declareDistinctRemovalWithoutEffect()
                : iqProperties.declareDistinctRemovalWithEffect();

        return iqFactory.createUnaryIQTree(this, newChild, newProperties);
    }

    @Override
    public ImmutableSet<ImmutableSet<Variable>> inferUniqueConstraints(IQTree child) {
        return child.inferUniqueConstraints();
    }

    @Override
    public boolean isConstructed(Variable variable, IQTree child) {
        return child.isConstructed(variable);
    }

    /**
     * TODO: detect minus encodings
     */
    @Override
    public boolean isDistinct(IQTree child) {
        return child.isDistinct();
    }

    @Override
    public boolean isSyntacticallyEquivalentTo(QueryNode node) {
        return (node instanceof FilterNode)
                && ((FilterNode) node).getFilterCondition().equals(this.getFilterCondition());
    }

    @Override
    public ImmutableSet<Variable> getRequiredVariables(IntermediateQuery query) {
        return getLocallyRequiredVariables();
    }

    @Override
    public boolean isEquivalentTo(QueryNode queryNode) {
        return (queryNode instanceof FilterNode)
                && getFilterCondition().equals(((FilterNode) queryNode).getFilterCondition());
    }

    @Override
    public String toString() {
        return FILTER_NODE_STR + getOptionalFilterString();
    }

    /**
     * TODO: Optimization: lift direct construction and filter nodes before normalizing them
     *  (so as to reduce the recursive pressure)
     */
    @Override
    public IQTree normalizeForOptimization(IQTree initialChild, VariableGenerator variableGenerator,
                                           IQProperties currentIQProperties) {
        return normalizer.normalizeForOptimization(this, initialChild, variableGenerator, currentIQProperties);
    }

    @Override
    public IQTree applyDescendingSubstitution(
            ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution,
            Optional<ImmutableExpression> constraint, IQTree child) {

        ImmutableExpression unoptimizedExpression = descendingSubstitution.applyToBooleanExpression(getFilterCondition());

        ImmutableSet<Variable> newlyProjectedVariables = constructionNodeTools
                .computeNewProjectedVariables(descendingSubstitution, child.getVariables());

        VariableNullability dummyVariableNullability = coreUtilsFactory.createDummyVariableNullability(
                newlyProjectedVariables.stream());

        try {
            ExpressionAndSubstitution expressionAndSubstitution = conditionSimplifier.simplifyCondition(unoptimizedExpression, dummyVariableNullability);

            Optional<ImmutableExpression> downConstraint = conditionSimplifier.computeDownConstraint(constraint,
                    expressionAndSubstitution, dummyVariableNullability);

            ImmutableSubstitution<? extends VariableOrGroundTerm> downSubstitution =
                    ((ImmutableSubstitution<VariableOrGroundTerm>)descendingSubstitution)
                            .composeWith2(expressionAndSubstitution.getSubstitution());

            IQTree newChild = child.applyDescendingSubstitution(downSubstitution, downConstraint);
            IQTree filterLevelTree = expressionAndSubstitution.getOptionalExpression()
                    .map(iqFactory::createFilterNode)
                    .map(n -> (IQTree) iqFactory.createUnaryIQTree(n, newChild))
                    .orElse(newChild);
            return expressionAndSubstitution.getSubstitution().isEmpty()
                    ? filterLevelTree
                    : iqFactory.createUnaryIQTree(
                            iqFactory.createConstructionNode(newlyProjectedVariables,
                                    (ImmutableSubstitution<ImmutableTerm>)(ImmutableSubstitution<?>)
                                            expressionAndSubstitution.getSubstitution()),
                            filterLevelTree);
        } catch (UnsatisfiableConditionException e) {
            return iqFactory.createEmptyNode(newlyProjectedVariables);
        }
    }

    @Override
    public IQTree applyDescendingSubstitutionWithoutOptimizing(
            ImmutableSubstitution<? extends VariableOrGroundTerm> descendingSubstitution, IQTree child) {
        FilterNode newFilterNode = iqFactory.createFilterNode(
                descendingSubstitution.applyToBooleanExpression(getFilterCondition()));

        return iqFactory.createUnaryIQTree(newFilterNode,
                child.applyDescendingSubstitutionWithoutOptimizing(descendingSubstitution));
    }
}
