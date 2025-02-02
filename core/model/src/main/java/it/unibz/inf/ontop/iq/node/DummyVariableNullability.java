package it.unibz.inf.ontop.iq.node;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;

public interface DummyVariableNullability extends VariableNullability {

    @Override
    @Deprecated
    VariableNullability update(ImmutableSubstitution<? extends ImmutableTerm> substitution,
                               ImmutableSet<Variable> projectedVariables);
}
