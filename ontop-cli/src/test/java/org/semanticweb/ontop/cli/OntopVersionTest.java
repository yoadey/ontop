package org.semanticweb.ontop.cli;

import org.junit.Test;

public class OntopVersionTest {

    @Test
    public void testOntopQueryCMD (){
        String[] argv = {"--version"};
        Ontop.main(argv);
    }


}
