package io.gitsocratic.api.administration

import io.gitsocratic.api.administration.builder.AddLocalRepoCommandBuilder
import io.gitsocratic.api.administration.builder.AddRemoteRepoCommandBuilder
import io.gitsocratic.api.administration.builder.ConfigCommandBuilder
import io.gitsocratic.command.config.ConfigOption
import io.gitsocratic.command.impl.Init

/**
 * Contains the commands which modify the GitSocratic environment.
 *
 * @version 0.2
 * @since 0.2
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
class AdministrationAPI {

    def init() {
        return Init.builder()
                .graknVersion(Init.defaultGraknVersion)
                .babelfishVersion(Init.defaultBabelfishVersion)
                .initGrakn(Init.defaultInitGrakn)
                .initBabelfish(Init.defaultInitBabelfish)
                .verbose(Init.defaultVerbose)
    }

    ConfigCommandBuilder.DisplayValues config() {
        return new ConfigCommandBuilder.DisplayValues()
    }

    ConfigCommandBuilder.GetValue config(ConfigOption option) {
        return new ConfigCommandBuilder.GetValue(option)
    }

    ConfigCommandBuilder.SetValue config(ConfigOption option, boolean value) {
        return new ConfigCommandBuilder.SetValue(option, Boolean.toString(value))
    }

    ConfigCommandBuilder.SetValue config(ConfigOption option, String value) {
        return new ConfigCommandBuilder.SetValue(option, value)
    }

    AddLocalRepoCommandBuilder addLocalRepo() {
        return new AddLocalRepoCommandBuilder()
    }

    AddRemoteRepoCommandBuilder addRemoteRepo() {
        return new AddRemoteRepoCommandBuilder()
    }
}
