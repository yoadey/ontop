package it.unibz.inf.ontop.model.term.functionsymbol.db.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.InequalityLabel;
import it.unibz.inf.ontop.model.term.functionsymbol.db.*;
import it.unibz.inf.ontop.model.type.*;
import it.unibz.inf.ontop.model.vocabulary.SPARQL;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public abstract class AbstractDBFunctionSymbolFactory implements DBFunctionSymbolFactory {

    private static final String BNODE_PREFIX = "_:ontop-bnode-";
    private static final String PLACEHOLDER = "{}";

    /**
     * Name (in the DB dialect), arity -> predefined REGULAR DBFunctionSymbol
     *
     * A regular function symbol is identified by its name in the DB dialect and can be used in the INPUT MAPPING file.
     *
     */
    private final ImmutableTable<String, Integer, DBFunctionSymbol> predefinedRegularFunctionTable;

    // Created in init()
    private DBTypeConversionFunctionSymbol temporaryToStringCastFunctionSymbol;
    // Created in init()
    private DBBooleanFunctionSymbol dbStartsWithFunctionSymbol;
    // Created in init()
    private DBBooleanFunctionSymbol dbEndsWithFunctionSymbol;
    // Created in init()
    private DBBooleanFunctionSymbol dbLikeFunctionSymbol;
    // Created in init()
    private DBIfElseNullFunctionSymbol ifElseNullFunctionSymbol;
    // Created in init()
    private DBNotFunctionSymbol dbNotFunctionSymbol;

    // Created in init()
    private DBBooleanFunctionSymbol containsFunctionSymbol;
    // Created in init()
    private DBFunctionSymbol r2rmlIRISafeEncodeFunctionSymbol;
    // Created in init()
    private DBFunctionSymbol strBeforeFunctionSymbol;
    // Created in init()
    private DBFunctionSymbol strAfterFunctionSymbol;
    // Created in init()
    private DBFunctionSymbol md5FunctionSymbol;
    // Created in init()
    private DBFunctionSymbol sha1FunctionSymbol;
    // Created in init()
    private DBFunctionSymbol sha256FunctionSymbol;
    // Created in init()
    private DBFunctionSymbol sha512FunctionSymbol;
    // Created in init()
    private DBFunctionSymbol yearFromDatetimeFunctionSymbol;
    // Created in init()
    private DBFunctionSymbol yearFromDateFunctionSymbol;
    // Created in init()
    private DBFunctionSymbol monthFromDatetimeFunctionSymbol;
    // Created in init()
    private DBFunctionSymbol monthFromDateFunctionSymbol;
    // Created in init()
    private DBFunctionSymbol dayFromDatetimeFunctionSymbol;
    // Created in init()
    private DBFunctionSymbol dayFromDateFunctionSymbol;
    // Created in init()
    private DBFunctionSymbol hoursFunctionSymbol;
    // Created in init()
    private DBFunctionSymbol minutesFunctionSymbol;
    // Created in init()
    private DBFunctionSymbol secondsFunctionSymbol;
    // Created in init()
    private DBFunctionSymbol tzFunctionSymbol;

    // Created in init()
    private DBBooleanFunctionSymbol nonStrictNumericEqOperator;
    // Created in init()
    private DBBooleanFunctionSymbol nonStrictStringEqOperator;
    // Created in init()
    private DBBooleanFunctionSymbol nonStrictDatetimeEqOperator;
    // Created in init()
    private DBBooleanFunctionSymbol nonStrictDateEqOperator;
    // Created in init()
    private DBBooleanFunctionSymbol nonStrictDefaultEqOperator;
    // Created in init()
    private DBBooleanFunctionSymbol booleanIfElseNullFunctionSymbol;

    /**
     *  For conversion function symbols that are SIMPLE CASTs from an undetermined type (no normalization)
     */
    private final Map<DBTermType, DBTypeConversionFunctionSymbol> castMap;
    /**
     *  For conversion function symbols that implies a NORMALIZATION as RDF lexical term
     *
     *  Created in init()
     */
    private ImmutableTable<DBTermType, RDFDatatype, DBTypeConversionFunctionSymbol> normalizationTable;

    /**
     *  For conversion function symbols that implies a DENORMALIZATION from RDF lexical term
     *
     *  Created in init()
     */
    private ImmutableTable<DBTermType, RDFDatatype, DBTypeConversionFunctionSymbol> deNormalizationTable;

    /**
     * Created in init()
     */
    private ImmutableTable<Integer, Boolean, DBFunctionSymbol> countTable;

    /**
     * Only for SIMPLE casts to DB string.
     * (Source DB type -> function symbol)
     *
     * NB: why using a map instead of table? Because tables are not thread-safe while some maps are.
     */
    private final Map<DBTermType, DBTypeConversionFunctionSymbol> simpleCastToDBStringMap;

    /**
     * Only for SIMPLE casts FROM DB string.
     * (Target DB type -> function symbol)
     *
     */
    private final Map<DBTermType, DBTypeConversionFunctionSymbol> simpleCastFromDBStringMap;

    /**
     *  For conversion function symbols that are SIMPLE CASTs from a determined type (no normalization)
     */
    private final Table<DBTermType, DBTermType, DBTypeConversionFunctionSymbol> otherSimpleCastTable;

    private final Table<String, DBTermType, DBMathBinaryOperator> binaryMathTable;
    private final Map<String, DBMathBinaryOperator> untypedBinaryMathMap;

    /**
     * For the CASE functions
     */
    private final Map<Integer, DBFunctionSymbol> caseMapWithOrder;
    private final Map<Integer, DBFunctionSymbol> caseMapWithoutOrder;

    /**
     * For the CASE functions
     */
    private final Map<Integer, DBBooleanFunctionSymbol> booleanCaseMapWithOrder;
    private final Map<Integer, DBBooleanFunctionSymbol> booleanCaseMapWithoutOrder;

    /**
     * For the strict equalities
     */
    private final Map<Integer, DBStrictEqFunctionSymbol> strictEqMap;

    /**
     * For the strict NOT equalities
     */
    private final Map<Integer, DBBooleanFunctionSymbol> strictNEqMap;

    /**
     * For the FalseORNulls
     */
    private final Map<Integer, FalseOrNullFunctionSymbol> falseOrNullMap;

    /**
     * For the TrueORNulls
     */
    private final Map<Integer, TrueOrNullFunctionSymbol> trueOrNullMap;

    /**
     * Coalesce functions according to their arities
     */
    private final Map<Integer, DBFunctionSymbol> coalesceMap;

    private final Map<InequalityLabel, DBBooleanFunctionSymbol> numericInequalityMap;
    private final Map<InequalityLabel, DBBooleanFunctionSymbol> booleanInequalityMap;
    private final Map<InequalityLabel, DBBooleanFunctionSymbol> stringInequalityMap;
    private final Map<InequalityLabel, DBBooleanFunctionSymbol> datetimeInequalityMap;
    private final Map<InequalityLabel, DBBooleanFunctionSymbol> dateInequalityMap;
    private final Map<InequalityLabel, DBBooleanFunctionSymbol> defaultInequalityMap;

    private final Map<DBTermType, DBFunctionSymbol> absMap;
    private final Map<DBTermType, DBFunctionSymbol> ceilMap;
    private final Map<DBTermType, DBFunctionSymbol> floorMap;
    private final Map<DBTermType, DBFunctionSymbol> roundMap;

    private final Map<DBTermType, DBFunctionSymbol> typeNullMap;

    private final TypeFactory typeFactory;
    private final DBTermType rootDBType;
    private final DBTermType dbStringType;
    private final DBTermType dbBooleanType;
    private final DBTermType dbIntegerType;
    private final DBTermType dbDecimalType;

    /**
     * Name (in the DB dialect), arity -> not predefined untyped DBFunctionSymbol
     */
    private final Table<String, Integer, DBFunctionSymbol> untypedFunctionTable;

    /**
     * Name (in the DB dialect), arity -> DBBooleanFunctionSymbol
     *
     * Only for boolean function symbols that are not predefined but created on-the-fly
     */
    private final Table<String, Integer, DBBooleanFunctionSymbol> notPredefinedBooleanFunctionTable;

    private final Map<String, IRIStringTemplateFunctionSymbol> iriTemplateMap;
    private final Map<String, BnodeStringTemplateFunctionSymbol> bnodeTemplateMap;

    private final Map<DBTermType, DBFunctionSymbol> distinctSumMap;
    private final Map<DBTermType, DBFunctionSymbol> regularSumMap;

    private final Map<DBTermType, DBFunctionSymbol> distinctAvgMap;
    private final Map<DBTermType, DBFunctionSymbol> regularAvgMap;

    private final Map<DBTermType, DBFunctionSymbol> minMap;
    private final Map<DBTermType, DBFunctionSymbol> maxMap;

    // NB: Multi-threading safety is NOT a concern here
    // (we don't create fresh bnode templates for a SPARQL query)
    private final AtomicInteger counter;


    protected AbstractDBFunctionSymbolFactory(ImmutableTable<String, Integer, DBFunctionSymbol> predefinedRegularFunctionTable,
                                              TypeFactory typeFactory) {
        this.counter = new AtomicInteger();
        this.typeFactory = typeFactory;
        DBTypeFactory dbTypeFactory = typeFactory.getDBTypeFactory();
        this.rootDBType = dbTypeFactory.getAbstractRootDBType();
        this.predefinedRegularFunctionTable = predefinedRegularFunctionTable;
        this.dbStringType = dbTypeFactory.getDBStringType();
        this.dbBooleanType = dbTypeFactory.getDBBooleanType();
        this.dbIntegerType = dbTypeFactory.getDBLargeIntegerType();
        this.dbDecimalType = dbTypeFactory.getDBDecimalType();

        // NB: in terms of design, we prefer avoiding using tables as they are not thread-safe
        this.binaryMathTable = HashBasedTable.create();
        this.untypedFunctionTable = HashBasedTable.create();
        this.notPredefinedBooleanFunctionTable = HashBasedTable.create();

        this.simpleCastToDBStringMap = new ConcurrentHashMap<>();
        this.simpleCastFromDBStringMap = new ConcurrentHashMap<>();
        this.otherSimpleCastTable = HashBasedTable.create();

        this.untypedBinaryMathMap = new ConcurrentHashMap<>();
        this.caseMapWithOrder = new ConcurrentHashMap<>();
        this.caseMapWithoutOrder = new ConcurrentHashMap<>();
        this.booleanCaseMapWithOrder = new ConcurrentHashMap<>();
        this.booleanCaseMapWithoutOrder = new ConcurrentHashMap<>();
        this.strictEqMap = new ConcurrentHashMap<>();
        this.strictNEqMap = new ConcurrentHashMap<>();
        this.falseOrNullMap = new ConcurrentHashMap<>();
        this.trueOrNullMap = new ConcurrentHashMap<>();
        this.castMap = new ConcurrentHashMap<>();
        this.iriTemplateMap = new ConcurrentHashMap<>();
        this.bnodeTemplateMap = new ConcurrentHashMap<>();
        this.numericInequalityMap = new ConcurrentHashMap<>();
        this.booleanInequalityMap = new ConcurrentHashMap<>();
        this.stringInequalityMap = new ConcurrentHashMap<>();
        this.datetimeInequalityMap = new ConcurrentHashMap<>();
        this.dateInequalityMap = new ConcurrentHashMap<>();
        this.defaultInequalityMap = new ConcurrentHashMap<>();
        this.coalesceMap = new ConcurrentHashMap<>();

        this.absMap = new ConcurrentHashMap<>();
        this.ceilMap = new ConcurrentHashMap<>();
        this.floorMap = new ConcurrentHashMap<>();
        this.roundMap = new ConcurrentHashMap<>();

        this.distinctSumMap = new ConcurrentHashMap<>();
        this.regularSumMap = new ConcurrentHashMap<>();

        this.distinctAvgMap = new ConcurrentHashMap<>();
        this.regularAvgMap = new ConcurrentHashMap<>();

        this.minMap = new ConcurrentHashMap<>();
        this.maxMap = new ConcurrentHashMap<>();

        this.typeNullMap = new ConcurrentHashMap<>();
    }

    /**
     * Called automatically by Guice
     */
    @Inject
    protected void init() {
        normalizationTable = createNormalizationTable();
        deNormalizationTable = createDenormalizationTable();
        countTable = createDBCountTable();

        temporaryToStringCastFunctionSymbol = new TemporaryDBTypeConversionToStringFunctionSymbolImpl(rootDBType, dbStringType);
        dbStartsWithFunctionSymbol = createStrStartsFunctionSymbol();
        dbEndsWithFunctionSymbol = createStrEndsFunctionSymbol();
        dbLikeFunctionSymbol = createLikeFunctionSymbol();
        ifElseNullFunctionSymbol = createRegularIfElseNull();
        dbNotFunctionSymbol = createDBNotFunctionSymbol(dbBooleanType);

        booleanIfElseNullFunctionSymbol = createDBBooleanIfElseNull();
        nonStrictNumericEqOperator = createNonStrictNumericEquality();
        nonStrictStringEqOperator = createNonStrictStringEquality();
        nonStrictDatetimeEqOperator = createNonStrictDatetimeEquality();
        nonStrictDateEqOperator = createNonStrictDateEquality();
        nonStrictDefaultEqOperator = createNonStrictDefaultEquality();
        r2rmlIRISafeEncodeFunctionSymbol = createR2RMLIRISafeEncode();
        strAfterFunctionSymbol = createStrAfterFunctionSymbol();
        containsFunctionSymbol = createContainsFunctionSymbol();
        strBeforeFunctionSymbol = createStrBeforeFunctionSymbol();

        md5FunctionSymbol = createMD5FunctionSymbol();
        sha1FunctionSymbol = createSHA1FunctionSymbol();
        sha256FunctionSymbol = createSHA256FunctionSymbol();
        sha512FunctionSymbol = createSHA512FunctionSymbol();

        yearFromDatetimeFunctionSymbol = createYearFromDatetimeFunctionSymbol();
        yearFromDateFunctionSymbol = createYearFromDateFunctionSymbol();
        monthFromDatetimeFunctionSymbol = createMonthFromDatetimeFunctionSymbol();
        monthFromDateFunctionSymbol = createMonthFromDateFunctionSymbol();
        dayFromDatetimeFunctionSymbol = createDayFromDatetimeFunctionSymbol();
        dayFromDateFunctionSymbol = createDayFromDateFunctionSymbol();
        hoursFunctionSymbol = createHoursFunctionSymbol();
        minutesFunctionSymbol = createMinutesFunctionSymbol();
        secondsFunctionSymbol = createSecondsFunctionSymbol();
        tzFunctionSymbol = createTzFunctionSymbol();
    }


    protected ImmutableTable<DBTermType, RDFDatatype, DBTypeConversionFunctionSymbol> createNormalizationTable() {
        DBTypeFactory dbTypeFactory = typeFactory.getDBTypeFactory();
        ImmutableTable.Builder<DBTermType, RDFDatatype, DBTypeConversionFunctionSymbol> builder = ImmutableTable.builder();

        // Date time
        RDFDatatype xsdDatetime = typeFactory.getXsdDatetimeDatatype();
        RDFDatatype xsdDatetimeStamp = typeFactory.getXsdDatetimeStampDatatype();
        DBTermType defaultDBDateTimestampType = dbTypeFactory.getDBDateTimestampType();
        DBTypeConversionFunctionSymbol datetimeNormFunctionSymbol = createDateTimeNormFunctionSymbol(defaultDBDateTimestampType);
        builder.put(defaultDBDateTimestampType, xsdDatetime, datetimeNormFunctionSymbol);
        builder.put(defaultDBDateTimestampType, xsdDatetimeStamp, datetimeNormFunctionSymbol);
        // Boolean
        builder.put(dbBooleanType, typeFactory.getXsdBooleanDatatype(), createBooleanNormFunctionSymbol(dbBooleanType));

        return builder.build();
    }

    protected ImmutableTable<DBTermType, RDFDatatype, DBTypeConversionFunctionSymbol> createDenormalizationTable() {
        DBTypeFactory dbTypeFactory = typeFactory.getDBTypeFactory();

        DBTermType timestampType = dbTypeFactory.getDBDateTimestampType();
        DBTermType booleanType = dbTypeFactory.getDBBooleanType();

        ImmutableTable.Builder<DBTermType, RDFDatatype, DBTypeConversionFunctionSymbol> builder = ImmutableTable.builder();

        // Date time
        DBTypeConversionFunctionSymbol timestampDenormalization = createDateTimeDenormFunctionSymbol(timestampType);
        builder.put(timestampType, typeFactory.getXsdDatetimeDatatype(), timestampDenormalization);
        builder.put(timestampType, typeFactory.getXsdDatetimeStampDatatype(), timestampDenormalization);

        // Boolean
        builder.put(booleanType, typeFactory.getXsdBooleanDatatype(), createBooleanDenormFunctionSymbol());

        return builder.build();
    }

    protected ImmutableTable<Integer, Boolean, DBFunctionSymbol> createDBCountTable() {

        ImmutableTable.Builder<Integer, Boolean, DBFunctionSymbol> builder = ImmutableTable.builder();
        Stream.of(false, true)
                .forEach(isUnary -> Stream.of(false, true)
                        .forEach(isDistinct ->
                                builder.put(isUnary ? 1 : 0, isDistinct, createDBCount(isUnary, isDistinct))));
        return builder.build();
    }


    @Override
    public IRIStringTemplateFunctionSymbol getIRIStringTemplateFunctionSymbol(String iriTemplate) {
        return iriTemplateMap
                .computeIfAbsent(iriTemplate,
                        t -> IRIStringTemplateFunctionSymbolImpl.createFunctionSymbol(t, typeFactory));
    }

    @Override
    public BnodeStringTemplateFunctionSymbol getBnodeStringTemplateFunctionSymbol(String bnodeTemplate) {
        return bnodeTemplateMap
                .computeIfAbsent(bnodeTemplate,
                        t -> BnodeStringTemplateFunctionSymbolImpl.createFunctionSymbol(t, typeFactory));
    }

    @Override
    public BnodeStringTemplateFunctionSymbol getFreshBnodeStringTemplateFunctionSymbol(int arity) {
        String bnodeTemplate = IntStream.range(0, arity)
                .boxed()
                .map(i -> PLACEHOLDER)
                .reduce(
                        BNODE_PREFIX + counter.incrementAndGet(),
                        (prefix, suffix) -> prefix + "/" + suffix);

        return getBnodeStringTemplateFunctionSymbol(bnodeTemplate);
    }

    @Override
    public DBTypeConversionFunctionSymbol getTemporaryConversionToDBStringFunctionSymbol() {
        return temporaryToStringCastFunctionSymbol;
    }

    @Override
    public DBTypeConversionFunctionSymbol getDBCastFunctionSymbol(DBTermType targetType) {
        return castMap
                .computeIfAbsent(targetType, this::createSimpleCastFunctionSymbol);
    }

    @Override
    public DBTypeConversionFunctionSymbol getDBCastFunctionSymbol(DBTermType inputType, DBTermType targetType) {
        if (inputType.equals(dbStringType)) {
            if (simpleCastFromDBStringMap.containsKey(targetType))
                return simpleCastFromDBStringMap.get(targetType);

            DBTypeConversionFunctionSymbol castFunctionSymbol = createSimpleCastFunctionSymbol(inputType, targetType);
            simpleCastFromDBStringMap.put(targetType, castFunctionSymbol);
            return castFunctionSymbol;
        }
        else if (targetType.equals(dbStringType)) {
            if (simpleCastToDBStringMap.containsKey(inputType))
                return simpleCastToDBStringMap.get(inputType);

            DBTypeConversionFunctionSymbol castFunctionSymbol = createSimpleCastFunctionSymbol(inputType, targetType);
            simpleCastToDBStringMap.put(inputType, castFunctionSymbol);
            return castFunctionSymbol;
        }
        /*
         * Mutable tables are not thread-safe
         */
        else {
            synchronized (otherSimpleCastTable) {
                if (otherSimpleCastTable.contains(inputType, targetType))
                    return otherSimpleCastTable.get(inputType, targetType);

                DBTypeConversionFunctionSymbol castFunctionSymbol = createSimpleCastFunctionSymbol(inputType, targetType);
                otherSimpleCastTable.put(inputType, targetType, castFunctionSymbol);
                return castFunctionSymbol;
            }
        }
    }

    @Override
    public DBFunctionSymbol getRegularDBFunctionSymbol(String nameInDialect, int arity) {
        String canonicalName = canonicalizeRegularFunctionSymbolName(nameInDialect);

        // Looks first in the immutable table
        Optional<DBFunctionSymbol> optionalPredefinedSymbol = Optional.ofNullable(predefinedRegularFunctionTable.get(canonicalName, arity));
        if (optionalPredefinedSymbol.isPresent()) {
            return optionalPredefinedSymbol.get();
        }

        /*
         * Mutable tables are not thread-safe
         */
        synchronized (untypedFunctionTable) {
            Optional<DBFunctionSymbol> optionalUntypedSymbol = Optional.ofNullable(
                    untypedFunctionTable.get(canonicalName, arity));
            // NB: we don't look inside notPredefinedBooleanFunctionTable to avoid enforcing the boolean type
            if (optionalUntypedSymbol.isPresent())
                return optionalUntypedSymbol.get();

            DBFunctionSymbol symbol = createRegularUntypedFunctionSymbol(canonicalName, arity);
            untypedFunctionTable.put(canonicalName, arity, symbol);
            return symbol;
        }
    }

    @Override
    public DBBooleanFunctionSymbol getRegularDBBooleanFunctionSymbol(String nameInDialect, int arity) {
        String canonicalName = canonicalizeRegularFunctionSymbolName(nameInDialect);

        // Looks first in the immutable table
        Optional<DBFunctionSymbol> optionalRegularSymbol = Optional.ofNullable(predefinedRegularFunctionTable.get(canonicalName, arity));
        if (optionalRegularSymbol.isPresent()) {
            DBFunctionSymbol functionSymbol = optionalRegularSymbol.get();
            if (functionSymbol instanceof DBBooleanFunctionSymbol)
                return (DBBooleanFunctionSymbol) functionSymbol;
            else
                throw new IllegalArgumentException(nameInDialect + " is known not to be a boolean function symbol");
        }

        /*
         * Mutable tables are not thread-safe
         */
        synchronized (notPredefinedBooleanFunctionTable) {
            Optional<DBFunctionSymbol> optionalSymbol = Optional.ofNullable(notPredefinedBooleanFunctionTable.get(canonicalName, arity));

            // NB: we don't look inside untypedFunctionTable as they are not declared as boolean

            if (optionalSymbol.isPresent()) {
                DBFunctionSymbol functionSymbol = optionalSymbol.get();
                if (functionSymbol instanceof DBBooleanFunctionSymbol)
                    return (DBBooleanFunctionSymbol) functionSymbol;
                else
                    throw new IllegalArgumentException(nameInDialect + " is known not to be a boolean function symbol");
            }

            DBBooleanFunctionSymbol symbol = createRegularBooleanFunctionSymbol(canonicalName, arity);
            notPredefinedBooleanFunctionTable.put(canonicalName, arity, symbol);
            return symbol;
        }
    }

    @Override
    public DBFunctionSymbol getDBCase(int arity, boolean doOrderingMatter) {
        if ((arity < 3) || (arity % 2 == 0))
            throw new IllegalArgumentException("Arity of a CASE function symbol must be odd and >= 3");

        return doOrderingMatter
                ? caseMapWithOrder.computeIfAbsent(arity, a -> createDBCase(arity, true))
                : caseMapWithoutOrder.computeIfAbsent(arity, a -> createDBCase(arity, false));

    }

    @Override
    public DBBooleanFunctionSymbol getDBBooleanCase(int arity, boolean doOrderingMatter) {
        if ((arity < 3) || (arity % 2 == 0))
            throw new IllegalArgumentException("Arity of a CASE function symbol must be odd and >= 3");

        return doOrderingMatter
                ? booleanCaseMapWithOrder.computeIfAbsent(arity, a -> createDBBooleanCase(arity, true))
                : booleanCaseMapWithoutOrder.computeIfAbsent(arity, a -> createDBBooleanCase(arity, false));
    }

    @Override
    public DBIfElseNullFunctionSymbol getDBIfElseNull() {
        return ifElseNullFunctionSymbol;
    }

    @Override
    public DBBooleanFunctionSymbol getDBBooleanIfElseNull() {
        return booleanIfElseNullFunctionSymbol;
    }

    @Override
    public DBStrictEqFunctionSymbol getDBStrictEquality(int arity) {
        if (arity < 2)
            throw new IllegalArgumentException("Arity of a strict equality must be >= 2");

        return strictEqMap
                .computeIfAbsent(arity, a -> createDBStrictEquality(arity));
    }

    @Override
    public DBBooleanFunctionSymbol getDBStrictNEquality(int arity) {
        if (arity < 2)
            throw new IllegalArgumentException("Arity of a strict equality must be >= 2");

        return strictNEqMap
                .computeIfAbsent(arity, a -> createDBStrictNEquality(arity));
    }

    @Override
    public DBBooleanFunctionSymbol getDBNonStrictNumericEquality() {
        return nonStrictNumericEqOperator;
    }

    @Override
    public DBBooleanFunctionSymbol getDBNonStrictStringEquality() {
        return nonStrictStringEqOperator;
    }

    @Override
    public DBBooleanFunctionSymbol getDBNonStrictDatetimeEquality() {
        return nonStrictDatetimeEqOperator;
    }

    @Override
    public DBBooleanFunctionSymbol getDBNonStrictDateEquality() {
        return nonStrictDateEqOperator;
    }

    @Override
    public DBBooleanFunctionSymbol getDBNonStrictDefaultEquality() {
        return nonStrictDefaultEqOperator;
    }

    @Override
    public DBBooleanFunctionSymbol getDBNumericInequality(InequalityLabel inequalityLabel) {
        return numericInequalityMap
                .computeIfAbsent(inequalityLabel, this::createNumericInequality);
    }

    @Override
    public DBBooleanFunctionSymbol getDBBooleanInequality(InequalityLabel inequalityLabel) {
        return booleanInequalityMap
                .computeIfAbsent(inequalityLabel, this::createBooleanInequality);
    }

    @Override
    public DBBooleanFunctionSymbol getDBStringInequality(InequalityLabel inequalityLabel) {
        return stringInequalityMap
                .computeIfAbsent(inequalityLabel, this::createStringInequality);
    }

    @Override
    public DBBooleanFunctionSymbol getDBDatetimeInequality(InequalityLabel inequalityLabel) {
        return datetimeInequalityMap
                .computeIfAbsent(inequalityLabel, this::createDatetimeInequality);
    }

    @Override
    public DBBooleanFunctionSymbol getDBDateInequality(InequalityLabel inequalityLabel) {
        return dateInequalityMap
                .computeIfAbsent(inequalityLabel, this::createDateInequality);
    }

    @Override
    public DBBooleanFunctionSymbol getDBDefaultInequality(InequalityLabel inequalityLabel) {
        return defaultInequalityMap
                .computeIfAbsent(inequalityLabel, this::createDefaultInequality);
    }

    @Override
    public DBBooleanFunctionSymbol getDBStartsWith() {
        return dbStartsWithFunctionSymbol;
    }

    @Override
    public DBBooleanFunctionSymbol getDBEndsWith() {
        return dbEndsWithFunctionSymbol;
    }

    @Override
    public DBFunctionSymbol getR2RMLIRISafeEncode() {
        return r2rmlIRISafeEncodeFunctionSymbol;
    }

    @Override
    public DBNotFunctionSymbol getDBNot() {
        return dbNotFunctionSymbol;
    }

    @Override
    public DBFunctionSymbol getDBCoalesce(int arity) {
        if (arity < 1)
            throw new IllegalArgumentException("Minimal arity is 1");

        return coalesceMap
                .computeIfAbsent(arity, (this::createCoalesceFunctionSymbol));
    }

    @Override
    public FalseOrNullFunctionSymbol getFalseOrNullFunctionSymbol(int arity) {
        return falseOrNullMap
                .computeIfAbsent(arity, (this::createFalseOrNullFunctionSymbol));
    }

    @Override
    public TrueOrNullFunctionSymbol getTrueOrNullFunctionSymbol(int arity) {
        return trueOrNullMap
                .computeIfAbsent(arity, (this::createTrueOrNullFunctionSymbol));
    }

    @Override
    public DBBooleanFunctionSymbol getDBContains() {
        return containsFunctionSymbol;
    }

    @Override
    public DBBooleanFunctionSymbol getDBLike() {
        return dbLikeFunctionSymbol;
    }

    @Override
    public DBFunctionSymbol getDBStrBefore() {
        return strBeforeFunctionSymbol;
    }

    @Override
    public DBFunctionSymbol getDBStrAfter() {
        return strAfterFunctionSymbol;
    }

    @Override
    public DBFunctionSymbol getDBMd5() {
        return md5FunctionSymbol;
    }

    @Override
    public DBFunctionSymbol getDBSha1() {
        return sha1FunctionSymbol;
    }

    @Override
    public DBFunctionSymbol getDBSha256() {
        return sha256FunctionSymbol;
    }

    @Override
    public DBFunctionSymbol getDBSha512() {
        return sha512FunctionSymbol;
    }

    @Override
    public DBMathBinaryOperator getDBMathBinaryOperator(String dbMathOperatorName, DBTermType dbNumericType) {
        // Mutable tables are not thread-safe
        synchronized (binaryMathTable) {
            DBMathBinaryOperator existingOperator = binaryMathTable.get(dbMathOperatorName, dbNumericType);
            if (existingOperator != null) {
                return existingOperator;
            }

            DBMathBinaryOperator newOperator = createDBBinaryMathOperator(dbMathOperatorName, dbNumericType);
            binaryMathTable.put(dbMathOperatorName, dbNumericType, newOperator);
            return newOperator;
        }
    }

    @Override
    public DBMathBinaryOperator getUntypedDBMathBinaryOperator(String dbMathOperatorName) {
        DBMathBinaryOperator existingOperator = untypedBinaryMathMap.get(dbMathOperatorName);
        if (existingOperator != null) {
            return existingOperator;
        }

        DBMathBinaryOperator newOperator = createUntypedDBBinaryMathOperator(dbMathOperatorName);
        untypedBinaryMathMap.put(dbMathOperatorName, newOperator);
        return newOperator;
    }

    @Override
    public DBFunctionSymbol getAbs(DBTermType dbTermType) {
        DBFunctionSymbol existingFunctionSymbol = absMap.get(dbTermType);
        if (existingFunctionSymbol != null)
            return existingFunctionSymbol;
        DBFunctionSymbol dbFunctionSymbol = createAbsFunctionSymbol(dbTermType);
        absMap.put(dbTermType, dbFunctionSymbol);
        return dbFunctionSymbol;
    }

    @Override
    public DBFunctionSymbol getCeil(DBTermType dbTermType) {
        DBFunctionSymbol existingFunctionSymbol = ceilMap.get(dbTermType);
        if (existingFunctionSymbol != null)
            return existingFunctionSymbol;
        DBFunctionSymbol dbFunctionSymbol = createCeilFunctionSymbol(dbTermType);
        ceilMap.put(dbTermType, dbFunctionSymbol);
        return dbFunctionSymbol;
    }

    @Override
    public DBFunctionSymbol getFloor(DBTermType dbTermType) {
        DBFunctionSymbol existingFunctionSymbol = floorMap.get(dbTermType);
        if (existingFunctionSymbol != null)
            return existingFunctionSymbol;
        DBFunctionSymbol dbFunctionSymbol = createFloorFunctionSymbol(dbTermType);
        floorMap.put(dbTermType, dbFunctionSymbol);
        return dbFunctionSymbol;
    }

    @Override
    public DBFunctionSymbol getRound(DBTermType dbTermType) {
        DBFunctionSymbol existingFunctionSymbol = roundMap.get(dbTermType);
        if (existingFunctionSymbol != null)
            return existingFunctionSymbol;
        DBFunctionSymbol dbFunctionSymbol = createRoundFunctionSymbol(dbTermType);
        roundMap.put(dbTermType, dbFunctionSymbol);
        return dbFunctionSymbol;
    }

    @Override
    public DBFunctionSymbol getDBYearFromDatetime() {
        return yearFromDatetimeFunctionSymbol;
    }

    @Override
    public DBFunctionSymbol getDBYearFromDate() {
        return yearFromDateFunctionSymbol;
    }

    @Override
    public DBFunctionSymbol getDBMonthFromDatetime() {
        return monthFromDatetimeFunctionSymbol;
    }

    @Override
    public DBFunctionSymbol getDBMonthFromDate() {
        return monthFromDateFunctionSymbol;
    }

    @Override
    public DBFunctionSymbol getDBDayFromDatetime() {
        return dayFromDatetimeFunctionSymbol;
    }

    @Override
    public DBFunctionSymbol getDBDayFromDate() {
        return dayFromDateFunctionSymbol;
    }

    @Override
    public DBFunctionSymbol getDBHours() {
        return hoursFunctionSymbol;
    }

    @Override
    public DBFunctionSymbol getDBMinutes() {
        return minutesFunctionSymbol;
    }

    @Override
    public DBFunctionSymbol getDBSeconds() {
        return secondsFunctionSymbol;
    }

    @Override
    public DBFunctionSymbol getDBTz() {
        return tzFunctionSymbol;
    }

    @Override
    public DBFunctionSymbol getTypedNullFunctionSymbol(DBTermType termType) {
        return typeNullMap
                .computeIfAbsent(termType, this::createTypeNullFunctionSymbol);
    }

    @Override
    public DBFunctionSymbol getDBCount(int arity, boolean isDistinct) {
        if (arity > 1) {
            throw new IllegalArgumentException("COUNT is 0-ary or unary");
        }
        return countTable.get(arity, isDistinct);
    }

    @Override
    public DBFunctionSymbol getNullIgnoringDBSum(DBTermType dbType, boolean isDistinct) {
        Function<DBTermType, DBFunctionSymbol> creationFct = t -> createDBSum(dbType, isDistinct);

        return isDistinct
                ? distinctSumMap.computeIfAbsent(dbType, creationFct)
                : regularSumMap.computeIfAbsent(dbType, creationFct);
    }

    /**
     * By default, we assume that the DB sum complies to the semantics of a null-ignoring sum.
     */
    @Override
    public DBFunctionSymbol getDBSum(DBTermType dbType, boolean isDistinct) {
        return getNullIgnoringDBSum(dbType, isDistinct);
    }

    @Override
    public DBFunctionSymbol getNullIgnoringDBAvg(DBTermType dbType, boolean isDistinct) {
        Function<DBTermType, DBFunctionSymbol> creationFct = t -> createDBAvg(dbType, isDistinct);

        return isDistinct
                ? distinctAvgMap.computeIfAbsent(dbType, creationFct)
                : regularAvgMap.computeIfAbsent(dbType, creationFct);
    }

    @Override
    public DBFunctionSymbol getDBMin(DBTermType dbType) {
        return minMap
                .computeIfAbsent(dbType, t -> createDBMin(t));
    }

    @Override
    public DBFunctionSymbol getDBMax(DBTermType dbType) {
        return maxMap
                .computeIfAbsent(dbType, t -> createDBMax(t));
    }

    @Override
    public DBFunctionSymbol getDBIntIndex(int nbValues) {
        // TODO: cache it
        return new DBIntIndexFunctionSymbolImpl(dbIntegerType, rootDBType, nbValues);
    }

    protected abstract DBFunctionSymbol createDBCount(boolean isUnary, boolean isDistinct);
    protected abstract DBFunctionSymbol createDBSum(DBTermType termType, boolean isDistinct);
    protected abstract DBFunctionSymbol createDBAvg(DBTermType termType, boolean isDistinct);
    protected abstract DBFunctionSymbol createDBMin(DBTermType termType);
    protected abstract DBFunctionSymbol createDBMax(DBTermType termType);

    protected abstract DBTypeConversionFunctionSymbol createDateTimeNormFunctionSymbol(DBTermType dbDateTimestampType);
    protected abstract DBTypeConversionFunctionSymbol createBooleanNormFunctionSymbol(DBTermType booleanType);
    protected abstract DBTypeConversionFunctionSymbol createDateTimeDenormFunctionSymbol(DBTermType timestampType);
    protected abstract DBTypeConversionFunctionSymbol createBooleanDenormFunctionSymbol();

    protected DBBooleanFunctionSymbol createLikeFunctionSymbol() {
        return new DBLikeFunctionSymbolImpl(dbBooleanType, rootDBType);
    }

    protected DBIfElseNullFunctionSymbol createRegularIfElseNull() {
        return new DefaultDBIfElseNullFunctionSymbol(dbBooleanType, rootDBType);
    }

    protected DBBooleanFunctionSymbol createStrStartsFunctionSymbol() {
        return new DefaultDBStrStartsWithFunctionSymbol(rootDBType, dbStringType);
    }

    protected DBBooleanFunctionSymbol createStrEndsFunctionSymbol() {
        return new DefaultDBStrEndsWithFunctionSymbol(
                rootDBType, dbStringType);
    }

    /**
     * Can be overridden
     */
    protected DBMathBinaryOperator createDBBinaryMathOperator(String dbMathOperatorName, DBTermType dbNumericType)
        throws UnsupportedOperationException {
        switch (dbMathOperatorName) {
            case SPARQL.NUMERIC_MULTIPLY:
                return createMultiplyOperator(dbNumericType);
            case SPARQL.NUMERIC_DIVIDE:
                return createDivideOperator(dbNumericType);
            case SPARQL.NUMERIC_ADD:
                return createAddOperator(dbNumericType);
            case SPARQL.NUMERIC_SUBSTRACT:
                return createSubstractOperator(dbNumericType);
            default:
                throw new UnsupportedOperationException("The math operator " + dbMathOperatorName + " is not supported");
        }
    }

    protected DBMathBinaryOperator createUntypedDBBinaryMathOperator(String dbMathOperatorName) throws UnsupportedOperationException {
        switch (dbMathOperatorName) {
            case SPARQL.NUMERIC_MULTIPLY:
                return createUntypedMultiplyOperator();
            case SPARQL.NUMERIC_DIVIDE:
                return createUntypedDivideOperator();
            case SPARQL.NUMERIC_ADD:
                return createUntypedAddOperator();
            case SPARQL.NUMERIC_SUBSTRACT:
                return createUntypedSubstractOperator();
            default:
                throw new UnsupportedOperationException("The untyped math operator " + dbMathOperatorName + " is not supported");
        }
    }

    protected DBBooleanFunctionSymbol createContainsFunctionSymbol() {
        return new DBContainsFunctionSymbolImpl(rootDBType, dbBooleanType, this::serializeContains);
    }

    protected DBFunctionSymbol createStrBeforeFunctionSymbol() {
        return new DBStrBeforeFunctionSymbolImpl(dbStringType, rootDBType, this::serializeStrBefore);
    }

    protected DBFunctionSymbol createStrAfterFunctionSymbol() {
        return new DBStrAfterFunctionSymbolImpl(dbStringType, rootDBType, this::serializeStrAfter);
    }

    protected FalseOrNullFunctionSymbol createFalseOrNullFunctionSymbol(int arity) {
        return new FalseOrNullFunctionSymbolImpl(arity, dbBooleanType);
    }

    protected TrueOrNullFunctionSymbol createTrueOrNullFunctionSymbol(int arity) {
        return new TrueOrNullFunctionSymbolImpl(arity, dbBooleanType);
    }

    protected DBFunctionSymbol createMD5FunctionSymbol() {
        return new DBHashFunctionSymbolImpl("DB_MD5", rootDBType, dbStringType, this::serializeMD5);
    }

    protected DBFunctionSymbol createSHA1FunctionSymbol() {
        return new DBHashFunctionSymbolImpl("DB_SHA1", rootDBType, dbStringType, this::serializeSHA1);
    }

    protected DBFunctionSymbol createSHA256FunctionSymbol() {
        return new DBHashFunctionSymbolImpl("DB_SHA256", rootDBType, dbStringType, this::serializeSHA256);
    }

    protected DBFunctionSymbol createSHA512FunctionSymbol() {
        return new DBHashFunctionSymbolImpl("DB_SHA512", rootDBType, dbStringType, this::serializeSHA512);
    }

    protected DBFunctionSymbol createYearFromDatetimeFunctionSymbol() {
        return new UnaryDBFunctionSymbolWithSerializerImpl("DB_YEAR_FROM_DATETIME", rootDBType, dbIntegerType, false,
                this::serializeYearFromDatetime);
    }

    protected DBFunctionSymbol createYearFromDateFunctionSymbol() {
        return new UnaryDBFunctionSymbolWithSerializerImpl("DB_YEAR_FROM_DATE", rootDBType, dbIntegerType, false,
                this::serializeYearFromDate);
    }

    protected DBFunctionSymbol createMonthFromDatetimeFunctionSymbol() {
        return new UnaryDBFunctionSymbolWithSerializerImpl("DB_MONTH_FROM_DATETIME", rootDBType, dbIntegerType, false,
                this::serializeMonthFromDatetime);
    }

    protected DBFunctionSymbol createMonthFromDateFunctionSymbol() {
        return new UnaryDBFunctionSymbolWithSerializerImpl("DB_MONTH_FROM_DATE", rootDBType, dbIntegerType, false,
                this::serializeMonthFromDate);
    }

    protected DBFunctionSymbol createDayFromDatetimeFunctionSymbol() {
        return new UnaryDBFunctionSymbolWithSerializerImpl("DB_DAY_FROM_DATE", rootDBType, dbIntegerType, false,
                this::serializeDayFromDatetime);
    }

    protected DBFunctionSymbol createDayFromDateFunctionSymbol() {
        return new UnaryDBFunctionSymbolWithSerializerImpl("DB_DAY_FROM_DATE", rootDBType, dbIntegerType, false,
                this::serializeDayFromDate);
    }

    protected DBFunctionSymbol createHoursFunctionSymbol() {
        return new UnaryDBFunctionSymbolWithSerializerImpl("DB_HOURS", rootDBType, dbIntegerType, false,
                this::serializeHours);
    }

    protected DBFunctionSymbol createMinutesFunctionSymbol() {
        return new UnaryDBFunctionSymbolWithSerializerImpl("DB_MINUTES", rootDBType, dbIntegerType, false,
                this::serializeMinutes);
    }

    protected DBFunctionSymbol createSecondsFunctionSymbol() {
        return new UnaryDBFunctionSymbolWithSerializerImpl("DB_SECONDS", rootDBType, dbDecimalType, false,
                this::serializeSeconds);
    }

    protected DBFunctionSymbol createTzFunctionSymbol() {
        return new UnaryDBFunctionSymbolWithSerializerImpl("DB_TZ", rootDBType, dbStringType, false,
                this::serializeTz);
    }

    protected abstract DBMathBinaryOperator createMultiplyOperator(DBTermType dbNumericType);
    protected abstract DBMathBinaryOperator createDivideOperator(DBTermType dbNumericType);
    protected abstract DBMathBinaryOperator createAddOperator(DBTermType dbNumericType) ;
    protected abstract DBMathBinaryOperator createSubstractOperator(DBTermType dbNumericType);

    protected abstract DBMathBinaryOperator createUntypedMultiplyOperator();
    protected abstract DBMathBinaryOperator createUntypedDivideOperator();
    protected abstract DBMathBinaryOperator createUntypedAddOperator();
    protected abstract DBMathBinaryOperator createUntypedSubstractOperator();

    protected abstract DBBooleanFunctionSymbol createNonStrictNumericEquality();
    protected abstract DBBooleanFunctionSymbol createNonStrictStringEquality();
    protected abstract DBBooleanFunctionSymbol createNonStrictDatetimeEquality();
    protected abstract DBBooleanFunctionSymbol createNonStrictDateEquality();
    protected abstract DBBooleanFunctionSymbol createNonStrictDefaultEquality();

    protected abstract DBBooleanFunctionSymbol createNumericInequality(InequalityLabel inequalityLabel);
    protected abstract DBBooleanFunctionSymbol createBooleanInequality(InequalityLabel inequalityLabel);
    protected abstract DBBooleanFunctionSymbol createStringInequality(InequalityLabel inequalityLabel);
    protected abstract DBBooleanFunctionSymbol createDatetimeInequality(InequalityLabel inequalityLabel);
    protected abstract DBBooleanFunctionSymbol createDateInequality(InequalityLabel inequalityLabel);
    protected abstract DBBooleanFunctionSymbol createDefaultInequality(InequalityLabel inequalityLabel);

    /**
     * Can be overridden
     */
    protected String canonicalizeRegularFunctionSymbolName(String nameInDialect) {
        return nameInDialect.toUpperCase();
    }

    protected abstract DBFunctionSymbol createRegularUntypedFunctionSymbol(String nameInDialect, int arity);

    protected abstract DBBooleanFunctionSymbol createRegularBooleanFunctionSymbol(String nameInDialect, int arity);

    protected abstract DBTypeConversionFunctionSymbol createSimpleCastFunctionSymbol(DBTermType targetType);

    protected abstract DBTypeConversionFunctionSymbol createSimpleCastFunctionSymbol(DBTermType inputType,
                                                                                     DBTermType targetType);

    protected abstract DBFunctionSymbol createDBCase(int arity, boolean doOrderingMatter);
    protected abstract DBBooleanFunctionSymbol createDBBooleanCase(int arity, boolean doOrderingMatter);

    protected abstract DBFunctionSymbol createCoalesceFunctionSymbol(int arity);

    protected DBBooleanFunctionSymbol createDBBooleanIfElseNull() {
        return new BooleanDBIfElseNullFunctionSymbolImpl(dbBooleanType);
    }

    protected abstract DBStrictEqFunctionSymbol createDBStrictEquality(int arity);

    protected abstract DBBooleanFunctionSymbol createDBStrictNEquality(int arity);

    protected abstract DBNotFunctionSymbol createDBNotFunctionSymbol(DBTermType dbBooleanType);

    protected abstract DBFunctionSymbol createR2RMLIRISafeEncode();

    protected abstract DBFunctionSymbol createAbsFunctionSymbol(DBTermType dbTermType);
    protected abstract DBFunctionSymbol createCeilFunctionSymbol(DBTermType dbTermType);
    protected abstract DBFunctionSymbol createFloorFunctionSymbol(DBTermType dbTermType);
    protected abstract DBFunctionSymbol createRoundFunctionSymbol(DBTermType dbTermType);

    protected DBFunctionSymbol createTypeNullFunctionSymbol(DBTermType termType) {
        return new SimplifiableTypedNullFunctionSymbol(termType);
    }


    protected abstract String serializeContains(ImmutableList<? extends ImmutableTerm> terms,
                                     Function<ImmutableTerm, String> termConverter,
                                     TermFactory termFactory);

    protected abstract String serializeStrBefore(ImmutableList<? extends ImmutableTerm> terms,
                                                 Function<ImmutableTerm, String> termConverter,
                                                 TermFactory termFactory);

    protected abstract String serializeStrAfter(ImmutableList<? extends ImmutableTerm> terms,
                                                 Function<ImmutableTerm, String> termConverter,
                                                 TermFactory termFactory);

    protected abstract String serializeMD5(ImmutableList<? extends ImmutableTerm> terms,
                                           Function<ImmutableTerm, String> termConverter,
                                           TermFactory termFactory);

    protected abstract String serializeSHA1(ImmutableList<? extends ImmutableTerm> terms,
                                           Function<ImmutableTerm, String> termConverter,
                                           TermFactory termFactory);

    protected abstract String serializeSHA256(ImmutableList<? extends ImmutableTerm> terms,
                                            Function<ImmutableTerm, String> termConverter,
                                            TermFactory termFactory);

    protected abstract String serializeSHA512(ImmutableList<? extends ImmutableTerm> terms,
                                            Function<ImmutableTerm, String> termConverter,
                                            TermFactory termFactory);

    protected abstract String serializeYearFromDatetime(ImmutableList<? extends ImmutableTerm> terms,
                                            Function<ImmutableTerm, String> termConverter,
                                            TermFactory termFactory);

    protected abstract String serializeYearFromDate(ImmutableList<? extends ImmutableTerm> terms,
                                                    Function<ImmutableTerm, String> termConverter,
                                                    TermFactory termFactory);

    protected abstract String serializeMonthFromDatetime(ImmutableList<? extends ImmutableTerm> terms,
                                            Function<ImmutableTerm, String> termConverter,
                                            TermFactory termFactory);

    protected abstract String serializeMonthFromDate(ImmutableList<? extends ImmutableTerm> terms,
                                                         Function<ImmutableTerm, String> termConverter,
                                                         TermFactory termFactory);

    protected abstract String serializeDayFromDatetime(ImmutableList<? extends ImmutableTerm> terms,
                                            Function<ImmutableTerm, String> termConverter,
                                            TermFactory termFactory);

    protected abstract String serializeDayFromDate(ImmutableList<? extends ImmutableTerm> terms,
                                                       Function<ImmutableTerm, String> termConverter,
                                                       TermFactory termFactory);

    protected abstract String serializeHours(ImmutableList<? extends ImmutableTerm> terms,
                                            Function<ImmutableTerm, String> termConverter,
                                            TermFactory termFactory);

    protected abstract String serializeMinutes(ImmutableList<? extends ImmutableTerm> terms,
                                            Function<ImmutableTerm, String> termConverter,
                                            TermFactory termFactory);

    protected abstract String serializeSeconds(ImmutableList<? extends ImmutableTerm> terms,
                                            Function<ImmutableTerm, String> termConverter,
                                            TermFactory termFactory);

    protected abstract String serializeTz(ImmutableList<? extends ImmutableTerm> terms,
                                               Function<ImmutableTerm, String> termConverter,
                                               TermFactory termFactory);


    @Override
    public DBTypeConversionFunctionSymbol getConversion2RDFLexicalFunctionSymbol(DBTermType inputType, RDFTermType rdfTermType) {
        return Optional.of(rdfTermType)
                .filter(t -> t instanceof RDFDatatype)
                .map(t -> (RDFDatatype) t)
                .flatMap(t -> Optional.ofNullable(normalizationTable.get(inputType, t)))
                // Fallback to simple cast
                .orElseGet(() -> getDBCastFunctionSymbol(inputType, dbStringType));
    }

    @Override
    public DBTypeConversionFunctionSymbol getConversionFromRDFLexical2DBFunctionSymbol(DBTermType targetDBType,
                                                                                       RDFTermType rdfTermType) {
        return Optional.of(rdfTermType)
                .filter(t -> t instanceof RDFDatatype)
                .map(t -> (RDFDatatype) t)
                .flatMap(t -> Optional.ofNullable(deNormalizationTable.get(targetDBType, t)))
                // Fallback to simple cast
                .orElseGet(() -> getDBCastFunctionSymbol(dbStringType, targetDBType));
    }

}
