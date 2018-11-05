package io.gitsocratic

/**
 * todo: description
 *
 * @version 0.1
 * @since 0.1
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
enum GitSocraticService {

    babelfish("bblfshd"), grakn("grakn-docker-entrypoint")

    private String command

    GitSocraticService(String command) {
        this.command = command
    }

    String getCommand() {
        return command
    }
}
