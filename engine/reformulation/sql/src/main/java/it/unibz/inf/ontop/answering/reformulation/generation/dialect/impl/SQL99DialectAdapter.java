package it.unibz.inf.ontop.answering.reformulation.generation.dialect.impl;

/*
 * #%L
 * ontop-reformulation-core
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

import it.unibz.inf.ontop.answering.reformulation.generation.dialect.SQLDialectAdapter;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.model.term.DBConstant;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.utils.R2RMLIRISafeEncoder;

import java.sql.Types;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

public class SQL99DialectAdapter implements SQLDialectAdapter {

    private Pattern quotes = Pattern.compile("[\"`\\['].*[\"`\\]']");

    protected  final String ENCODE_FOR_URI_START, ENCODE_FOR_URI_END;

    public SQL99DialectAdapter() {
        StringBuilder sb1 = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        for (Entry<String, String> e : R2RMLIRISafeEncoder.TABLE.entrySet()) {
            sb1.append("REPLACE(");
            String value = e.getValue();
            String encode = e.getKey();
            sb2.append(", ").append(getSQLLexicalFormString(value))
                    .append(", ").append(getSQLLexicalFormString(encode))
                    .append(")");

        }
        ENCODE_FOR_URI_START = sb1.toString();
        ENCODE_FOR_URI_END = sb2.toString();
    }

    @Override
    public String escapedSingleQuote() {
        return "''";
    }

    @Override
    public String iriSafeEncode(String str) {
        return ENCODE_FOR_URI_START + str + ENCODE_FOR_URI_END;
    }


    @Override
    public String dateNow() {
        return "CURRENT_TIMESTAMP()";

    }

    @Override
    public String dateYear(String str) {
        return String.format("EXTRACT(YEAR FROM %s)", str);
    }

    @Override
    public String dateDay(String str) {
        return String.format("EXTRACT(DAY FROM %s)", str);
    }

    @Override
    public String dateHours(String str) {
        return String.format("EXTRACT(HOUR FROM %s)", str);
    }

    @Override
    public String dateMonth(String str) {
        return String.format("EXTRACT(MONTH FROM %s)", str);
    }

    @Override
    public String rand() {
        return "RAND()";
    }

    @Override
    public String dateMinutes(String str) {
        return String.format("EXTRACT(MINUTE FROM %s)", str);
    }

    @Override
    public String dateSeconds(String str) {
        return String.format("EXTRACT(SECOND FROM %s)", str);
    }

    @Override
    public String dateTZ(String str) {
        throw new UnsupportedOperationException("TZ is not supported in this dialect.");
    }


    @Override
    public String SHA256(String str) {
        throw new UnsupportedOperationException("SHA256 is not supported in this dialect.");
    }

    @Override
    public String SHA1(String str) {
        throw new UnsupportedOperationException("SHA1 is not supported in this dialect.");
    }

    @Override
    public String SHA512(String str) {
        throw new UnsupportedOperationException("SHA512 is not supported in this dialect.");
    }

    @Override
    public String MD5(String str) {
        throw new UnsupportedOperationException("MD5 is not supported in this dialect.");
    }

    @Override
    public String strUuid() {
        throw new UnsupportedOperationException("strUUID is not supported in this dialect.");
    }

    @Override
    public String uuid() {
        throw new UnsupportedOperationException("UUID is not supported in this dialect.");
    }

    @Override
    public String ceil() {
        return "CEIL(%s)";
    }

    @Override
    public String round() {
        return "ROUND(%s)";
    }

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

    @Override
    public String strUcase(String str) {
        return String.format("UPPER(%s)", str);
    }

    @Override
    public String strStartsOperator() {
        return "SUBSTRING(%1$s, 1, LENGTH(%2$s)) LIKE %2$s";
    }

    @Override
    public String strEndsOperator() {
        return "RIGHT(%1$s, LENGTH(%2$s)) LIKE %2$s";
    }

    @Override
    public String strContainsOperator() {
        return "CHARINDEX(%2$s,%1$s) > 0";
    }

    @Override
    public String strBefore(String str, String before) {
        return String.format("LEFT(%s,CHARINDEX(%s,%s)-1)", str, before, str);
    }

    @Override
    public String strAfter(String str, String after) {
//		sign return 1 if positive number, 0 if 0, and -1 if negative number
//		it will return everything after the value if it is present or it will return an empty string if it is not present
        return String.format("SUBSTRING(%s,CHARINDEX(%s,%s) + LENGTH(%s), SIGN(CHARINDEX(%s,%s)) * LENGTH(%s))",
                str, after, str, after, after, str, str);
    }

    @Override
    public String strLcase(String str) {
        return String.format("LOWER(%s)", str);
    }

    @Override
    public String strLength(String str) {
        return String.format("LENGTH(%s)", str);
    }

    @Override
    public String strSubstr(String str, String start, String end) {
        return String.format("SUBSTR(%s,%s,%s)", str, start, end);
    }

    @Override
    public String strSubstr(String str, String start) {
        return String.format("SUBSTR(%s,%s)", str, start);
    }

    @Override
    public String strReplace(String str, String oldstr, String newstr) {
        if (quotes.matcher(oldstr).matches()) {
            oldstr = oldstr.substring(1, oldstr.length() - 1); // remove the enclosing quotes
        }

        if (quotes.matcher(newstr).matches()) {
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

    @Override
    public String getClosingQuote() {
        return "\"";
    }

    /**
     * There is no standard for this part.
     * <p>
     * Arbitrary default implementation proposed
     * (may not work with many DB engines).
     */
    @Override
    public String sqlSlice(long limit, long offset) {
        if ((limit < 0) && (offset < 0)) {
            return "";
        } else if ((limit >= 0) && (offset >= 0)) {
            return String.format("LIMIT %d, %d", offset, limit);
        } else if (offset < 0) {
            return String.format("LIMIT %d", limit);
        }
        // Else -> (limit < 0)
        else {
            return String.format("OFFSET %d", offset);
        }
    }

    @Override
    public String sqlGroupBy(List<Variable> groupby, String viewname) {
        String sql = "GROUP BY ";
        boolean needComma = false;
        for (Variable v : groupby) {
            if (needComma) {
                sql += ", ";
            }
            //sql += sqlQualifiedColumn(viewname, v.getName());
            sql += String.format("\"%s\"", v.getName());
            needComma = true;
        }
        return sql;
    }

    @Override
    public String sqlCast(String value, int type) {
        String strType = null;

        switch (type) {
            case Types.VARCHAR:
                strType = "CHAR";
                break;
            case Types.BIT:
                strType = "BIT";
                break;
            case Types.TINYINT:
                strType = "TINYINT";
                break;
            case Types.SMALLINT:
                strType = "SMALLINT";
                break;
            case Types.INTEGER:
                strType = "INTEGER";
                break;
            case Types.BIGINT:
                strType = "BIGINT";
                break;
            case Types.FLOAT:
                strType = "FLOAT";
                break;
            case Types.REAL:
                strType = "REAL";
                break;
            case Types.DOUBLE:
                strType = "DOUBLE";
                break;
            case Types.NUMERIC:
                strType = "NUMERIC";
                break;
            case Types.DECIMAL:
                strType = "DECIMAL";
                break;
            case Types.CHAR:
                strType = "CHAR";
                break;
            case Types.LONGVARCHAR:
                strType = "LONGVARCHAR";
                break;
            case Types.DATE:
                strType = "DATE";
                break;
            case Types.TIME:
                strType = "TIME";
                break;
            case Types.TIMESTAMP:
                strType = "TIMESTAMP";
                break;
            case Types.BINARY:
                strType = "BINARY";
                break;
            case Types.VARBINARY:
                strType = "VARBINARY";
                break;
            case Types.LONGVARBINARY:
                strType = "LONGVARBINARY";
                break;
            case Types.NULL:
                strType = "NULL";
                break;
            case Types.OTHER:
                strType = "OTHER";
                break;
            case Types.JAVA_OBJECT:
                strType = "JAVA_OBJECT";
                break;
            case Types.DISTINCT:
                strType = "DISTINCT";
                break;
            case Types.STRUCT:
                strType = "STRUCT";
                break;
            case Types.ARRAY:
                strType = "ARRAY";
                break;
            case Types.BLOB:
                strType = "BLOB";
                break;
            case Types.CLOB:
                strType = "CLOB";
                break;
            case Types.REF:
                strType = "REF";
                break;
            case Types.DATALINK:
                strType = "DATALINK";
                break;
            case Types.BOOLEAN:
                strType = "BOOLEAN";
                break;
            case Types.ROWID:
                strType = "ROWID";
                break;
            case Types.NCHAR:
                strType = "NCHAR";
                break;
            case Types.NVARCHAR:
                strType = "NVARCHAR";
                break;
            case Types.LONGNVARCHAR:
                strType = "LONGNVARCHAR";
                break;
            case Types.NCLOB:
                strType = "NCLOB";
                break;
            case Types.SQLXML:
                strType = "SQLXML";
                break;


            default:
                throw new RuntimeException("Unsupported SQL type");
        }
        return "CAST(" + value + " AS " + strType + ")";
    }

    @Override
    public String sqlRegex(String columnname, String pattern, boolean caseinSensitive, boolean multiLine, boolean dotAllMode) {

        if (quotes.matcher(pattern).matches()) {
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
    public String nameTopVariable(String signatureVariableName, Set<String> sqlVariableNames) {
        return sqlQuote(buildDefaultName("", signatureVariableName, ""));
    }

    @Override
    public String nameView(String prefix, String tableName, String suffix, Collection<RelationID> views) {
        return sqlQuote(buildDefaultName(prefix, tableName, suffix));
    }

    @Override
    public String ifElseNull(String condition, String valueIfTrue) {
        return "CASE WHEN " + condition + " THEN " + valueIfTrue + "\n"
                + "ELSE NULL END ";
    }

    @Override
    public String getNullConstant() {
        return "NULL";
    }

    @Override
    public String render(DBConstant constant) {
        DBTermType dbType = constant.getType();

        switch (dbType.getCategory()) {
            case INTEGER:
            case DECIMAL:
            case FLOAT_DOUBLE:
                // TODO: handle the special case of not-a-number!
            case BOOLEAN:
                return constant.getValue();
            default:
                return getSQLLexicalFormString(constant.getValue());
        }
    }

    /**
     * Concatenates the strings.
     * Default way to name a variable or a view.
     * <p>
     * Returns an UNQUOTED string.
     */
    protected final String buildDefaultName(String prefix, String intermediateName, String suffix) {
        return prefix + intermediateName + suffix;
    }

    @Override
    public String getDummyTable() {
        // TODO: check whether this inherited implementation from JDBCUtilities is OK
        return "SELECT 1";
    }

    @Override
    public Optional<String> getTrueTable() {
        return Optional.empty();
    }

    /**
     * By default, quotes and escapes isolated single quotes
     */
    @Override
    public String getSQLLexicalFormString(String constant) {

        return "'" + constant.replaceAll("(?<!')'(?!')", escapedSingleQuote()) + "'";
    }

    @Override
    public String getSQLLexicalFormBoolean(boolean value) {
        // TODO: check whether this implementation inherited from JDBCUtility is correct
        return value ? "TRUE" : "FALSE";
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


}
