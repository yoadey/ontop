package it.unibz.inf.ontop.answering.reformulation.generation.algebra;

import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.model.term.Variable;

/**
 * Already serialized
 *
 * See SQLAlgebraFactory for creating a new instance.
 */
public interface SQLSerializedQuery extends SQLExpression {

    String getSQLString();

    ImmutableMap<Variable, String> getColumnNames();
}
