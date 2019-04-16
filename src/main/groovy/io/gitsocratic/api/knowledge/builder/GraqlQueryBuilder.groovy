package io.gitsocratic.api.knowledge.builder

import io.gitsocratic.command.impl.query.Graql

/**
 * Used to construct the 'query graql' command via API.
 *
 * @version 0.2
 * @since 0.2
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
class GraqlQueryBuilder {

    private String query

    GraqlQueryBuilder query(String query) {
        this.query = query
        return this
    }

    Graql build() {
        return new Graql(query: query)
    }
}
