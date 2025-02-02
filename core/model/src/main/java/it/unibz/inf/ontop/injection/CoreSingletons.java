package it.unibz.inf.ontop.injection;

import it.unibz.inf.ontop.iq.type.UniqueTermTypeExtractor;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.TargetAtomFactory;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbolFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;

/**
 * Helper for accessing (most of) the Guice "singletons" of the ontop-model module
 *
 * Useful for writing low-level classes that are not instantiated by the Guice framework
 */
public interface CoreSingletons {

    TermFactory getTermFactory();

    TypeFactory getTypeFactory();

    FunctionSymbolFactory getFunctionSymbolFactory();
    DBFunctionSymbolFactory getDBFunctionsymbolFactory();

    AtomFactory getAtomFactory();

    SubstitutionFactory getSubstitutionFactory();

    IntermediateQueryFactory getIQFactory();

    CoreUtilsFactory getCoreUtilsFactory();

    TargetAtomFactory getTargetAtomFactory();

    /**
     * TODO: refactor and remove
     */
    QueryTransformerFactory getQueryTransformerFactory();

    UniqueTermTypeExtractor getUniqueTermTypeExtractor();

    OntopModelSettings getSettings();

    // TODO: complete
}
