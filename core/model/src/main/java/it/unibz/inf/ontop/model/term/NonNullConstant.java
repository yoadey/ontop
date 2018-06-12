package it.unibz.inf.ontop.model.term;

import it.unibz.inf.ontop.model.type.TermType;

import java.util.Optional;

public interface NonNullConstant extends Constant {

    TermType getType();

    @Override
    default Optional<TermType> getOptionalType() {
        return Optional.of(getType());
    }

    @Override
    default boolean isNull() {
        return false;
    }
}
