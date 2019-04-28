package io.gitsocratic.command.result

import grakn.core.concept.answer.ConceptMap
import groovy.transform.TupleConstructor

/**
 * 'question' command responses returned when triggered via API.
 *
 * @version 0.2
 * @since 0.2
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
@TupleConstructor
class QuestionCommandResult {
    final int status
    final List<ConceptMap> answer
    final long queryTimeMs
}
