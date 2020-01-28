package io.gitsocratic.command.result

import groovy.transform.TupleConstructor

/**
 * 'question' command responses returned when triggered via API.
 *
 * @version 0.2.1
 * @since 0.2
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
@TupleConstructor
class QuestionCommandResult {
    final int status
    final Object answer
    final long queryTimeMs
}
