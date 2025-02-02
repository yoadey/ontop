package it.unibz.inf.ontop.injection;

/**
 * Helper
 *
 * See CoreSingletons for the motivation
 *
 */
public interface MappingCoreSingletons {

    CoreSingletons getCoreSingletons();
    OptimizationSingletons getOptimizationSingletons();

    /**
     * For building Mapping, PrefixManager and so on
     */
    SpecificationFactory getSpecificationFactory();

    /**
     * For building MappingWithProvenance
     */
    ProvenanceMappingFactory getProvenanceMappingFactory();

    /**
     * TODO: shall we remove it?
     */
    TargetQueryParserFactory getTargetQueryParserFactory();

    // TODO: complete

}
