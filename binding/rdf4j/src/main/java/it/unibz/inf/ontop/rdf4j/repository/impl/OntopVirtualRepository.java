package it.unibz.inf.ontop.rdf4j.repository.impl;

/*
 * #%L
 * ontop-quest-sesame
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import it.unibz.inf.ontop.answering.OntopQueryEngine;
import it.unibz.inf.ontop.answering.connection.OntopConnection;
import it.unibz.inf.ontop.answering.reformulation.input.RDF4JInputQueryFactory;
import it.unibz.inf.ontop.injection.OntopSystemConfiguration;
import it.unibz.inf.ontop.rdf4j.repository.OntopRepository;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.base.AbstractRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;

public class OntopVirtualRepository extends AbstractRepository implements OntopRepository {

    private static final Logger logger = LoggerFactory.getLogger(OntopVirtualRepository.class);

    // Temporary (dropped after initialization)
    @Nullable
    private OntopSystemConfiguration configuration;

    @Nullable
    private OntopQueryEngine queryEngine;
    private final RDF4JInputQueryFactory inputQueryFactory;

    public OntopVirtualRepository(OntopSystemConfiguration configuration) {
        this.configuration = configuration;
        inputQueryFactory = configuration.getInjector().getInstance(RDF4JInputQueryFactory.class);
    }

    /**
     * Returns a new RepositoryConnection.
     * <p>
     * (No repository connection sharing for the sake
     * of thread-safeness)
     */
    @Override
    public OntopRepositoryConnection getConnection() throws RepositoryException {
        if (!isInitialized()) {
            init();
        }

        try {
            return new OntopRepositoryConnection(this, getOntopConnection(), inputQueryFactory);
        } catch (Exception e) {
            logger.error("Error creating repo connection: " + e.getMessage());
            throw new RepositoryException(e);
        }
    }


    /**
     * This method leads to the reasoner being initialized (connecting to the database,
     * analyzing mappings, etc.). .
     */
    @Override
    protected void initializeInternal() throws RepositoryException {
        try {
            queryEngine = configuration.loadQueryEngine();
            queryEngine.connect();
            logger.info("Ontop virtual repository initialized successfully!");
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

    /**
     * Returns a connection which can be used to run queries over the repository
     * Before this method can be used, initialize() must be called once.
     */
    private OntopConnection getOntopConnection() throws RepositoryException {
        try {
            return queryEngine.getConnection();
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public boolean isWritable() throws RepositoryException {
        return false;
    }

    @Override
    protected void shutDownInternal() throws RepositoryException {
        try {
            queryEngine.close();
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public File getDataDir() {
        throw new RepositoryException("Ontop does not have a data directory");
    }

    @Override
    public ValueFactory getValueFactory() {
        // Gets a ValueFactory for this Repository.
        return SimpleValueFactory.getInstance();
    }

    @Override
    public void setDataDir(File arg0) {
        // Ignores it
    }

    @Override
    public void close() throws RepositoryException {
        this.shutDown();
    }
}
