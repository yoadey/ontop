package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.injection.OntopMappingSettings;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.ontology.ClassifiedTBox;
import it.unibz.inf.ontop.spec.ontology.Ontology;
import it.unibz.inf.ontop.spec.OBDASpecification;
import it.unibz.inf.ontop.spec.mapping.transformer.*;
import it.unibz.inf.ontop.spec.ontology.impl.OntologyBuilderImpl;
import org.apache.commons.rdf.api.RDF;

import java.util.Optional;

import static it.unibz.inf.ontop.injection.OntopModelSettings.CardinalityPreservationMode.LOOSE;

public class DefaultMappingTransformer implements MappingTransformer {

    private final MappingVariableNameNormalizer mappingNormalizer;
    private final MappingSaturator mappingSaturator;
    private final ABoxFactIntoMappingConverter factConverter;
    private final MappingMerger mappingMerger;
    private final OntopMappingSettings settings;
    private final MappingSameAsInverseRewriter sameAsInverseRewriter;
    private final SpecificationFactory specificationFactory;
    private final RDF rdfFactory;
    private MappingDistinctTransformer mappingDistinctTransformer;

    @Inject
    private DefaultMappingTransformer(MappingVariableNameNormalizer mappingNormalizer,
                                      MappingSaturator mappingSaturator,
                                      ABoxFactIntoMappingConverter inserter,
                                      MappingMerger mappingMerger,
                                      OntopMappingSettings settings,
                                      MappingSameAsInverseRewriter sameAsInverseRewriter,
                                      SpecificationFactory specificationFactory,
                                      RDF rdfFactory,
                                      MappingDistinctTransformer mappingDistinctTransformer) {
        this.mappingNormalizer = mappingNormalizer;
        this.mappingSaturator = mappingSaturator;
        this.factConverter = inserter;
        this.mappingMerger = mappingMerger;
        this.settings = settings;
        this.sameAsInverseRewriter = sameAsInverseRewriter;
        this.specificationFactory = specificationFactory;
        this.rdfFactory = rdfFactory;
        this.mappingDistinctTransformer = mappingDistinctTransformer;
    }

    @Override
    public OBDASpecification transform(Mapping mapping, DBMetadata dbMetadata, Optional<Ontology> ontology) {
        if (ontology.isPresent()) {
            Mapping factsAsMapping = factConverter.convert(ontology.get().abox(),
                    settings.isOntologyAnnotationQueryingEnabled());
            Mapping mappingWithFacts = mappingMerger.merge(mapping, factsAsMapping);
            return createSpecification(mappingWithFacts, dbMetadata, ontology.get().tbox());
        }
        else {
            ClassifiedTBox emptyTBox = OntologyBuilderImpl.builder(rdfFactory).build().tbox();
            return createSpecification(mapping, dbMetadata, emptyTBox);
        }
    }

    OBDASpecification createSpecification(Mapping mapping, DBMetadata dbMetadata, ClassifiedTBox tbox) {
        Mapping sameAsOptimizedMapping = sameAsInverseRewriter.rewrite(mapping);
        Mapping saturatedMapping = mappingSaturator.saturate(sameAsOptimizedMapping, dbMetadata, tbox);
        Mapping normalizedMapping = mappingNormalizer.normalize(saturatedMapping);

        // Don't insert the distinct if the cardinality preservation is set to LOOSE
        Mapping finalMapping = settings.getCardinalityPreservationMode() == LOOSE
                ? normalizedMapping
                : mappingDistinctTransformer.addDistinct(normalizedMapping);

        return specificationFactory.createSpecification(finalMapping, dbMetadata, tbox);
    }
}
