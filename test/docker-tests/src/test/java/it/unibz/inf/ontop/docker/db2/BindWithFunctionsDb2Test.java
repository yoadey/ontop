package it.unibz.inf.ontop.docker.db2;

/*
 * #%L
 * ontop-quest-owlapi
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


import it.unibz.inf.ontop.docker.AbstractBindTestWithFunctions;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OWLConnection;
import org.junit.AfterClass;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to test if functions on Strings and Numerics in SPARQL are working properly.
 *
 */

public class BindWithFunctionsDb2Test extends AbstractBindTestWithFunctions {
	private static final String owlfile = "/db2/bind/sparqlBind.owl";
	private static final String obdafile = "/db2/bind/sparqlBindDb2.obda";
    private static final String propertiesfile = "/db2/bind/db2-smallbooks.properties";

    private static OntopOWLReasoner REASONER;
    private static OWLConnection CONNECTION;

    public BindWithFunctionsDb2Test() throws OWLOntologyCreationException {
        super(createReasoner(owlfile, obdafile, propertiesfile));
        REASONER = getReasoner();
        CONNECTION = getConnection();
    }

    @AfterClass
    public static void after() throws OWLException {
        CONNECTION.close();
        REASONER.dispose();
    }

    @Ignore("Not yet supported")
    @Test
    @Override
    public void testHash() {
    }

    @Ignore("Not yet supported")
    @Test
    @Override
    public void testUuid() {
    }

    @Ignore("Not yet supported")
    @Test
    @Override
    public void testStrUuid() {
    }

    @Override
    protected List<String> getDivideExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"21.2500000000000000000000000\"^^xsd:decimal");
        expectedValues.add("\"11.5000000000000000000000000\"^^xsd:decimal");
        expectedValues.add("\"16.7500000000000000000000000\"^^xsd:decimal");
        expectedValues.add("\"5.0000000000000000000000000\"^^xsd:decimal");
        return expectedValues;
    }

    @Ignore("Not yet supported")
    @Test
    @Override
    public void testTZ() {
    }


    @Override
    protected List<String> getAbsExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"8.5000\"^^xsd:decimal");
        expectedValues.add("\"5.7500\"^^xsd:decimal");
        expectedValues.add("\"6.7000\"^^xsd:decimal");
        expectedValues.add("\"1.5000\"^^xsd:decimal");
        return expectedValues;
    }

    @Override
    protected List<String> getRoundExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\".00, 43.00\"^^xsd:string");
        expectedValues.add("\".00, 23.00\"^^xsd:string");
        expectedValues.add("\".00, 34.00\"^^xsd:string");
        expectedValues.add("\".00, 10.00\"^^xsd:string");
        return expectedValues;
    }

    @Override
    protected List<String> getYearExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"2014\"^^xsd:integer");
        expectedValues.add("\"2011\"^^xsd:integer");
        expectedValues.add("\"2015\"^^xsd:integer");
        expectedValues.add("\"1970\"^^xsd:integer");

        return expectedValues;
    }

    @Override
    protected List<String> getSecondsExpectedValues() {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.add("\"52.000000\"^^xsd:decimal");
        expectedValues.add("\"0.000000\"^^xsd:decimal");
        expectedValues.add("\"6.000000\"^^xsd:decimal");
        expectedValues.add("\"0.000000\"^^xsd:decimal");

        return expectedValues;
    }
}
