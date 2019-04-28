package io.gitsocratic.command.result

import groovy.transform.TupleConstructor

/**
 * 'add-remote-repo' command responses returned when triggered via API.
 *
 * @version 0.2
 * @since 0.2
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
@TupleConstructor
class AddRemoteRepoCommandResult extends AddLocalRepoCommandResult {

    AddRemoteRepoCommandResult(int status) {
        super(status)
    }
}
