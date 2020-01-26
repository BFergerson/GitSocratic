package io.gitsocratic

/**
 * Services GitSocratic is able to initialize and control.
 *
 * @version 0.2
 * @since 0.1
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
enum GitSocraticService {

    apache_skywalking("bash docker-entrypoint.sh"),
    babelfish("/tini -- bblfshd"),
    grakn("./grakn-docker.sh"),
    source_plus_plus("source-core.sh")

    private String command

    GitSocraticService(String command) {
        this.command = command
    }

    String getCommand() {
        return command
    }
}
