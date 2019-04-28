package io.gitsocratic.command.result

import groovy.transform.TupleConstructor

/**
 * 'init' command responses returned when triggered via API.
 *
 * @version 0.2
 * @since 0.2
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
@TupleConstructor
class InitCommandResult {
    final int status
}
