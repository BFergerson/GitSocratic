package io.gitsocratic.command.result

/**
 * todo: this
 *
 * @version 0.2
 * @since 0.2
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
class InitDockerCommandResult extends InitCommandResult {

    final Map<String, String[]> portBindings

    InitDockerCommandResult(Map<String, String[]> portBindings) {
        super(0)
        this.portBindings = Objects.requireNonNull(portBindings)
    }

    InitDockerCommandResult(int status) {
        super(status)
        portBindings = new HashMap<>()
    }
}
