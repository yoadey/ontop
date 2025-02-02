package it.unibz.inf.ontop.answering.reformulation.generation.dialect;

import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.answering.reformulation.generation.dialect.impl.OracleSQLDialectAdapter;
import it.unibz.inf.ontop.answering.reformulation.generation.dialect.impl.SQL99DialectAdapter;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Test how the Oracle SQL dialect adapter behaves with long variable names (for top variables).
 */
public class OracleLongVarNameTest {

    private SQLDialectAdapter defaultAdapter = new SQL99DialectAdapter();
    private SQLDialectAdapter oracleAdapter = new OracleSQLDialectAdapter();
    private static final String veryLongSignatureVarName = "veryVeryVeryVeryVeryVeryVeryVeryVeryVeryVeryVerylongVarName";
    private static final String limitSignatureVarName = "v23456789012345678901234";
    private static final String defaultSuffix = "Suffix";
    private static final ImmutableSet<String> emptyVarSet = ImmutableSet.of();


    @Test
    public void testDefaultAdapter() {
        String veryLongVarName = defaultAdapter.nameTopVariable(veryLongSignatureVarName + defaultSuffix, emptyVarSet);
        assertEquals(veryLongVarName, defaultAdapter.sqlQuote(veryLongSignatureVarName + defaultSuffix));
    }

    @Test
    public void testOracleOneShot() {
        String veryLongVarName = oracleAdapter.nameTopVariable(veryLongSignatureVarName + defaultSuffix, emptyVarSet);
        assertTrue(veryLongVarName.length() <= OracleSQLDialectAdapter.NAME_MAX_LENGTH);
        //assertTrue(veryLongVarName.contains(defaultSuffix));
        assertTrue(veryLongVarName.contains("veryVery"));
        assertEquals( "\"veryVeryVeryVeryVeryVeryVer0\"", veryLongVarName);
    }

    @Test
    public void testOracleTenSimilarVars() {
        Set<String> assignedVars = new HashSet<>();
        int createdVarNb = (int) Math.pow(10, OracleSQLDialectAdapter.NAME_NUMBER_LENGTH);
        for(int i = 0; i < createdVarNb; i++) {
            assignedVars.add(oracleAdapter.nameTopVariable(veryLongSignatureVarName + defaultSuffix, assignedVars));
        }
        assertEquals(assignedVars.size(), createdVarNb);
    }

    @Test(expected = RuntimeException.class)
    public void testOracleTooMuchSimilarVars() {
        Set<String> assignedVars = new HashSet<>();
        int createdVarNb = (int) (Math.pow(10, OracleSQLDialectAdapter.NAME_NUMBER_LENGTH) + 1);
        for(int i = 0; i < createdVarNb; i++) {
            assignedVars.add(oracleAdapter.nameTopVariable(veryLongSignatureVarName + defaultSuffix, assignedVars));
        }
    }

    @Test
    public void testOracleMaxNonModifiedVarName() {
        String limitVarName = oracleAdapter.nameTopVariable(limitSignatureVarName + defaultSuffix, emptyVarSet);
        assertEquals(limitVarName, oracleAdapter.sqlQuote(limitSignatureVarName + defaultSuffix));
    }

}
