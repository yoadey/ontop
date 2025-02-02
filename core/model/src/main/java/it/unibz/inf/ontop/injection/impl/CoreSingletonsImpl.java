package it.unibz.inf.ontop.injection.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OntopModelSettings;
import it.unibz.inf.ontop.injection.QueryTransformerFactory;
import it.unibz.inf.ontop.iq.type.UniqueTermTypeExtractor;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.TargetAtomFactory;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbolFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;

@Singleton
public class CoreSingletonsImpl implements CoreSingletons {

    private final TermFactory termFactory;
    private final TypeFactory typeFactory;
    private final FunctionSymbolFactory functionSymbolFactory;
    private final DBFunctionSymbolFactory dbFunctionsymbolFactory;
    private final AtomFactory atomFactory;
    private final SubstitutionFactory substitutionFactory;
    private final CoreUtilsFactory coreUtilsFactory;
    private final TargetAtomFactory targetAtomFactory;
    private final UniqueTermTypeExtractor uniqueTermTypeExtractor;
    private final IntermediateQueryFactory iqFactory;
    private final OntopModelSettings settings;

    @Inject
    private CoreSingletonsImpl(TermFactory termFactory, TypeFactory typeFactory,
                               FunctionSymbolFactory functionSymbolFactory,
                               DBFunctionSymbolFactory dbFunctionsymbolFactory, AtomFactory atomFactory,
                               SubstitutionFactory substitutionFactory, CoreUtilsFactory coreUtilsFactory,
                               TargetAtomFactory targetAtomFactory, UniqueTermTypeExtractor uniqueTermTypeExtractor,
                               IntermediateQueryFactory iqFactory, OntopModelSettings settings) {
        this.termFactory = termFactory;
        this.typeFactory = typeFactory;
        this.functionSymbolFactory = functionSymbolFactory;
        this.dbFunctionsymbolFactory = dbFunctionsymbolFactory;
        this.atomFactory = atomFactory;
        this.substitutionFactory = substitutionFactory;
        this.coreUtilsFactory = coreUtilsFactory;
        this.targetAtomFactory = targetAtomFactory;
        this.uniqueTermTypeExtractor = uniqueTermTypeExtractor;
        this.iqFactory = iqFactory;
        this.settings = settings;
    }

    @Override
    public TermFactory getTermFactory() {
        return termFactory;
    }

    @Override
    public TypeFactory getTypeFactory() {
        return typeFactory;
    }

    @Override
    public FunctionSymbolFactory getFunctionSymbolFactory() {
        return functionSymbolFactory;
    }

    @Override
    public DBFunctionSymbolFactory getDBFunctionsymbolFactory() {
        return dbFunctionsymbolFactory;
    }

    @Override
    public AtomFactory getAtomFactory() {
        return atomFactory;
    }

    @Override
    public SubstitutionFactory getSubstitutionFactory() {
        return substitutionFactory;
    }

    @Override
    public IntermediateQueryFactory getIQFactory() {
        return iqFactory;
    }

    @Override
    public CoreUtilsFactory getCoreUtilsFactory() {
        return coreUtilsFactory;
    }

    @Override
    public TargetAtomFactory getTargetAtomFactory() {
        return targetAtomFactory;
    }

    @Override
    public QueryTransformerFactory getQueryTransformerFactory() {
        throw new RuntimeException("TODO: remove it");
    }

    @Override
    public UniqueTermTypeExtractor getUniqueTermTypeExtractor() {
        return uniqueTermTypeExtractor;
    }

    @Override
    public OntopModelSettings getSettings() {
        return settings;
    }
}
