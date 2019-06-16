package io.gitsocratic.command.result

/**
 * todo: this
 *
 * @version 0.2
 * @since 0.2
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
class InitDockerCommandResult extends InitCommandResult {

    final String containerName
    final Map<String, String[]> portBindings

    InitDockerCommandResult(String containerName, Map<String, String[]> portBindings) {
        super(0)
        this.containerName = containerName
        this.portBindings = Objects.requireNonNull(portBindings)
    }

    InitDockerCommandResult(String containerName, int status) {
        super(status)
        this.containerName = containerName
        this.portBindings = new HashMap<>()
    }
}
