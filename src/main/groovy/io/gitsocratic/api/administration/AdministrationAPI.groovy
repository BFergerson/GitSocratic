package io.gitsocratic.api.administration

import io.gitsocratic.api.administration.builder.AddLocalRepoCommandBuilder
import io.gitsocratic.api.administration.builder.AddRemoteRepoCommandBuilder
import io.gitsocratic.api.administration.builder.ConfigCommandBuilder
import io.gitsocratic.api.administration.builder.InitRequiredServicesBuilder
import io.gitsocratic.api.administration.builder.init.InitApacheSkywalkingCommandBuilder
import io.gitsocratic.api.administration.builder.init.InitBabelfishCommandBuilder
import io.gitsocratic.api.administration.builder.init.InitGraknCommandBuilder
import io.gitsocratic.api.administration.builder.init.InitSourcePlusPlusCommandBuilder
import io.gitsocratic.command.config.ConfigOption

/**
 * Contains the commands which modify the GitSocratic environment.
 *
 * @version 0.2
 * @since 0.2
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
class AdministrationAPI {

    InitRequiredServicesBuilder init() {
        return new InitRequiredServicesBuilder()
    }

    InitApacheSkywalkingCommandBuilder initApacheSkywalking() {
        return new InitApacheSkywalkingCommandBuilder()
    }

    InitBabelfishCommandBuilder initBabelfish() {
        return new InitBabelfishCommandBuilder()
    }

    InitGraknCommandBuilder initGrakn() {
        return new InitGraknCommandBuilder()
    }

    InitSourcePlusPlusCommandBuilder initSourcePlusPlus() {
        return new InitSourcePlusPlusCommandBuilder()
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
