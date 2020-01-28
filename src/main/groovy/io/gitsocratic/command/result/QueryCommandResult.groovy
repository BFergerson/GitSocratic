package io.gitsocratic.command.result

import grakn.client.answer.ConceptMap
import groovy.transform.TupleConstructor

/**
 * 'query' command responses returned when triggered via API.
 *
 * @version 0.2.1
 * @since 0.2
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
abstract class QueryCommandResult {

    @TupleConstructor
    static class GraqlResponse {
        final int status
        final List<ConceptMap> answer
        final long queryTimeMs
    }
}
