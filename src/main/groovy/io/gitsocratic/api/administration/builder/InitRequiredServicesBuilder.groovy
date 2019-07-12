package io.gitsocratic.api.administration.builder

import groovy.util.logging.Slf4j
import io.gitsocratic.command.impl.Init
import io.gitsocratic.command.impl.init.Babelfish
import io.gitsocratic.command.impl.init.Grakn
import io.gitsocratic.command.result.InitCommandResult

/**
 * Used to construct the 'init' command via API.
 *
 * @version 0.2
 * @since 0.2
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
@Slf4j
class InitRequiredServicesBuilder {

    private String babelfishVersion = Babelfish.defaultBabelfishVersion
    private String graknVersion = Grakn.defaultGraknVersion
    private boolean verbose = Init.defaultVerbose
    private boolean useServicePorts = Init.defaultUseServicePorts

    InitRequiredServicesBuilder babelfishVersion(String babelfishVersion) {
        this.babelfishVersion = babelfishVersion
        return this
    }

    InitRequiredServicesBuilder graknVersion(String graknVersion) {
        this.graknVersion = graknVersion
        return this
    }

    InitRequiredServicesBuilder verbose(boolean verbose) {
        this.verbose = verbose
        return this
    }

    InitRequiredServicesBuilder useServicePorts(boolean useServicePorts) {
        this.useServicePorts = useServicePorts
        return this
    }

    RequiredServices build() {
        return new RequiredServices(babelfishVersion, graknVersion, verbose, useServicePorts)
    }

    static class RequiredServices {
        final String babelfishVersion
        final String graknVersion
        final boolean verbose
        final boolean useServicePorts

        RequiredServices(String babelfishVersion, String graknVersion, boolean verbose, boolean useServicePorts) {
            this.babelfishVersion = babelfishVersion
            this.graknVersion = graknVersion
            this.verbose = verbose
            this.useServicePorts = useServicePorts
        }

        InitCommandResult execute() throws Exception {
            return execute(false)
        }

        InitCommandResult execute(boolean outputToStd) throws Exception {
            def input = new PipedInputStream()
            def output = new PipedOutputStream()
            input.connect(output)
            if (outputToStd) {
                Thread.startDaemon {
                    input.newReader().eachLine {
                        log.info it
                    }
                }
            }
            return execute(output)
        }

        InitCommandResult execute(PipedOutputStream output) throws Exception {
            //install base services: babelfish & grakn
            def status = new Babelfish(babelfishVersion, verbose, useServicePorts).execute(output)
            if (status.status == 0) {
                status = new Grakn(graknVersion, verbose, useServicePorts).execute(output)
            }
            return new InitCommandResult(status.status)
        }
    }
}
