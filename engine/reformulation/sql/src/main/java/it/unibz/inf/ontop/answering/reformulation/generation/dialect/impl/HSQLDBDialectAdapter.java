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

import java.sql.Types;
import java.util.regex.Pattern;

public class HSQLDBDialectAdapter extends SQL99DialectAdapter {


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
	public String sqlCast(String value, int type) {
		String strType = null;
		if (type == Types.VARCHAR) {
			strType = "VARCHAR(400)";
		} else {
			throw new RuntimeException("Unsupported SQL type");
		}
		return "CAST(" + value + " AS " + strType + ")";
	}

	@Override
	public String sqlRegex(String columnname, String pattern, boolean caseinSensitive, boolean multiLine, boolean dotAllMode) {
        Pattern quotes = Pattern.compile("[\"`\\['].*[\"`\\]']");
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
	public String strUuid(){
		return "UUID()";
	}

	@Override
	public String uuid(){
		return strConcat(new String[]{"'urn:uuid:'","UUID()"});
	}

	@Override
	public String round() {
		return "ROUND(%s, 0)";
	}

	@Override
	public String strStartsOperator(){
		return "SUBSTRING(%1$s, 1, CHAR_LENGTH(%2$s)) LIKE %2$s";
	}

	@Override
	public String strEndsOperator(){
		return "RIGHT(%1$s, CHAR_LENGTH(%2$s)) LIKE %2$s";
	}

	@Override
	public String strContainsOperator(){
		return "INSTR(%1$s,%2$s) > 0";
	}

	@Override
	public String strBefore(String str, String before) {
		return String.format("LEFT(%s,INSTR(%s,%s)-1)", str, str, before);
	}

	@Override
	public String strAfter(String str, String after) {
//		sign return 1 if positive number, 0 if 0 and -1 if negative number
//		it will return everything after the value if it is present or it will return an empty string if it is not present
		return String.format("SUBSTRING(%s,LOCATE(%s,%s) + LENGTH(%s), SIGN(LOCATE(%s,%s)) * LENGTH(%s))",
				str, after, str , after , after, str, str);
	}

	@Override
	public String dateTZ(String str) {
		return strConcat(new String[] {String.format("EXTRACT(TIMEZONE_HOUR FROM %s)", str), "':'" , String.format("EXTRACT(TIMEZONE_MINUTE FROM %s) ",str)});
	}

	@Override
	public String getDummyTable() {
		// TODO: check whether it is OK --- this was the behaviour in JDBCUtility
		return "SELECT 1";
	}
	
	@Override
	public String getSQLLexicalFormString(String constant) {
		return "'" + constant + "'";
	}
	
	@Override 
	public String getSQLLexicalFormBoolean(boolean value) {
		// TODO: provide a correct implementation
		return value ? 	"1" : "0";
	}
	
	/***
	 * Given an XSD dateTime this method will generate a SQL TIMESTAMP value.
	 * The method will strip any fractional seconds found in the date time
	 * (since we haven't found a nice way to support them in all databases). It
	 * will also normalize the use of Z to the timezome +00:00 and last, if the
	 * database is H2, it will remove all timezone information, since this is
	 * not supported there.
	 * 
	 *
	 * @return
	 */
	@Override
	public String getSQLLexicalFormDatetime(String v) {
		// TODO: check whether this inherited implementation is OK
		
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
