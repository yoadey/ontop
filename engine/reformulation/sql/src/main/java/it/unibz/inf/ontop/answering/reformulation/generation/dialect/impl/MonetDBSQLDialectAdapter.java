package it.unibz.inf.ontop.answering.reformulation.generation.dialect.impl;

import java.sql.Types;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by elem on 22/06/15.
 */
public class MonetDBSQLDialectAdapter extends SQL99DialectAdapter {
    private Pattern quotes = Pattern.compile("[\"`\\['].*[\"`\\]']");

    @Override
    public String strConcat(String[] strings) {
        if (strings.length == 0)
            throw new IllegalArgumentException("Cannot concatenate 0 strings");

        if (strings.length == 1)
            return strings[0];

        StringBuilder sql = new StringBuilder();

        sql.append(String.format("(%s", strings[0]));
        for (int i = 1; i < strings.length; i++) {
            sql.append(String.format(" || %s", strings[i]));
        }
        sql.append(")");
        return sql.toString();
    }

    public String strReplace(String str, char oldchar, char newchar) {
        return String.format("REPLACE(%s, '%s', '%s')", str, oldchar, newchar);
    }

    @Override
    public String strReplace(String str, String oldstr, String newstr) {
        if(quotes.matcher(oldstr).matches() ) {
            oldstr = oldstr.substring(1, oldstr.length() - 1); // remove the enclosing quotes
        }

        if(quotes.matcher(newstr).matches() ) {
            newstr = newstr.substring(1, newstr.length() - 1);
        }
        return String.format("REPLACE(%s, '%s', '%s')", str, oldstr, newstr);
    }


    @Override
    public String sqlQualifiedColumn(String tablename, String columnname) {
        // TODO: This should depend on whether the column name was quoted in the original sql query
        return String.format("%s.\"%s\"", tablename, columnname);
    }


    @Override
//	public String sqlTableName(String tablename, String viewname) {
//		return String.format("\"%s\" %s", tablename, viewname);
//	}

	/*Now we use the table name given by the user,
	  and we assume that it includes the quotes if needed*/
    public String sqlTableName(String tablename, String viewname) {
        return String.format("%s %s", tablename, viewname);
    }

    @Override
    public String sqlQuote(String name) {
        //TODO: This should depend on quotes in the sql in the mappings
        return String.format("\"%s\"", name);
//		return name;
    }

    /**
     * There is no standard for this part.
     *
     * Arbitrary default implementation proposed
     * (may not work with many DB engines).
     */
    @Override
    public String sqlSlice(long limit, long offset) {
        if ((limit < 0) && (offset < 0)) {
            return "";
        }
        else if ((limit >= 0) && (offset >= 0)) {
            return String.format("LIMIT %d OFFSET %d", offset, limit);
        }
        else if (offset < 0) {
            return String.format("LIMIT %d", limit);
        }
        // Else -> (limit < 0)
        else {
            return String.format("OFFSET %d", offset);
        }
    }

    @Override
    public String sqlCast(String value, int type) {
        String strType = null;
        if (type == Types.VARCHAR) {
            strType = "STRING";
        } else {
            throw new RuntimeException("Unsupported SQL type");
        }
        return "CAST(" + value + " AS " + strType + ")";
    }

    @Override
    public String sqlRegex(String columnname, String pattern, boolean caseinSensitive, boolean multiLine, boolean dotAllMode) {

        if(quotes.matcher(pattern).matches() ) {
            pattern = pattern.substring(1, pattern.length() - 1); // remove the
            // enclosing
            // quotes
        }
        //we use % wildcards to search for a string that contains and not only match the pattern
        if (caseinSensitive) {
            return " LOWER(" + columnname + ") LIKE " + "'%"
                    + pattern.toLowerCase() + "%'";
        }
        return columnname + " LIKE " + "'%" + pattern + "%'";
    }

    @Override
    public String getDummyTable() {
        // TODO: check whether this inherited implementation from JDBCUtilities is OK
        return "SELECT 1";
    }

    @Override
    public String getSQLLexicalFormString(String constant) {
        return "'" + constant + "'";
    }

    @Override
    public String getSQLLexicalFormBoolean(boolean value) {
        // TODO: check whether this implementation inherited from JDBCUtility is correct
        return value ? 	"TRUE" : "FALSE";
    }

    /***
     * Given an XSD dateTime this method will generate a SQL TIMESTAMP value.
     * The method will strip any fractional seconds found in the date time
     * (since we haven't found a nice way to support them in all databases). It
     * will also normalize the use of Z to the timezome +00:00 and last, if the
     * database is H2, it will remove all timezone information, since this is
     * not supported there.
     *
     */
    @Override
    public String getSQLLexicalFormDatetime(String v) {
        // TODO: check whether this implementation inherited from JDBCUtility is correct

        String datetime = v.replace('T', ' ');
        int dotlocation = datetime.indexOf('.');
        int zlocation = datetime.indexOf('Z');
        int minuslocation = datetime.indexOf('-', 10); // added search from 10th pos, because we need to ignore minuses in date
        int pluslocation = datetime.indexOf('+');
        StringBuilder bf = new StringBuilder(datetime);
        if (zlocation != -1) {
			/*
			 * replacing Z by +00:00
			 */
            bf.replace(zlocation, bf.length(), "+00:00");
        }

        if (dotlocation != -1) {
			/*
			 * Stripping the string from the presicion that is not supported by
			 * SQL timestamps.
			 */
            // TODO we need to check which databases support fractional
            // sections (e.g., oracle,db2, postgres)
            // so that when supported, we use it.
            int endlocation = Math.max(zlocation, Math.max(minuslocation, pluslocation));
            if (endlocation == -1) {
                endlocation = datetime.length();
            }
            bf.replace(dotlocation, endlocation, "");
        }
        bf.insert(0, "'");
        bf.append("'");

        return bf.toString();
    }

    @Override
    public String getSQLLexicalFormDatetimeStamp(String v) {
        // TODO: check whether this implementation inherited from JDBCUtility is correct

        String datetime = v.replace('T', ' ');
        int dotlocation = datetime.indexOf('.');
        int zlocation = datetime.indexOf('Z');
        int minuslocation = datetime.indexOf('-', 10); // added search from 10th pos, because we need to ignore minuses in date
        int pluslocation = datetime.indexOf('+');
        StringBuilder bf = new StringBuilder(datetime);
        if (zlocation != -1) {
			/*
			 * replacing Z by +00:00
			 */
            bf.replace(zlocation, bf.length(), "+00:00");
        }

        if (dotlocation != -1) {
			/*
			 * Stripping the string from the presicion that is not supported by
			 * SQL timestamps.
			 */
            // TODO we need to check which databases support fractional
            // sections (e.g., oracle,db2, postgres)
            // so that when supported, we use it.
            int endlocation = Math.max(zlocation, Math.max(minuslocation, pluslocation));
            if (endlocation == -1) {
                endlocation = datetime.length();
            }
            bf.replace(dotlocation, endlocation, "");
        }
        bf.insert(0, "'");
        bf.append("'");

        return bf.toString();
    }

    @Override
    public String nameTopVariable(String variableName, Set<String> sqlVariableNames) {
        return sqlQuote(variableName);
    }

}
