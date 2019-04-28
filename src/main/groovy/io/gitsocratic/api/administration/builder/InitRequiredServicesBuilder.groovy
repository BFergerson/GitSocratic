package io.gitsocratic.api.administration.builder

import io.gitsocratic.command.impl.Init
import io.gitsocratic.command.impl.init.Babelfish
import io.gitsocratic.command.impl.init.Grakn

/**
 * Used to construct the 'init' command via API.
 *
 * @version 0.2
 * @since 0.2
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
class InitRequiredServicesBuilder {

    private String babelfishVersion = Babelfish.defaultBabelfishVersion
    private String graknVersion = Grakn.defaultGraknVersion
    private boolean verbose = Init.defaultVerbose

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

    RequiredServices build() {
        return new RequiredServices(babelfishVersion, graknVersion, verbose)
    }

    static class RequiredServices extends Init {
        final String babelfishVersion
        final String graknVersion
        final boolean verbose

        RequiredServices(String babelfishVersion, String graknVersion, boolean verbose) {
            this.babelfishVersion = babelfishVersion
            this.graknVersion = graknVersion
            this.verbose = verbose
        }
    }
}
