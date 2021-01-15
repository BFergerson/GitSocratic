package io.gitsocratic.api.administration

import io.gitsocratic.api.administration.builder.ProcessLocalRepoCommandBuilder
import io.gitsocratic.api.administration.builder.ProcessRemoteRepoCommandBuilder
import io.gitsocratic.api.administration.builder.ConfigCommandBuilder
import io.gitsocratic.api.administration.builder.InitRequiredServicesBuilder
import io.gitsocratic.api.administration.builder.init.InitApacheSkyWalkingCommandBuilder
import io.gitsocratic.api.administration.builder.init.InitBabelfishCommandBuilder
import io.gitsocratic.api.administration.builder.init.InitGraknCommandBuilder
import io.gitsocratic.api.administration.builder.init.InitSourcePlusPlusCommandBuilder
import io.gitsocratic.command.config.ConfigOption

/**
 * Contains the commands which modify the GitSocratic environment.
 *
 * @version 0.2.1
 * @since 0.2
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
class AdministrationAPI {

    InitRequiredServicesBuilder init() {
        return new InitRequiredServicesBuilder()
    }

    InitApacheSkyWalkingCommandBuilder initApacheSkyWalking() {
        return new InitApacheSkyWalkingCommandBuilder()
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

    ProcessLocalRepoCommandBuilder processLocalRepo() {
        return new ProcessLocalRepoCommandBuilder()
    }

    ProcessRemoteRepoCommandBuilder processRemoteRepo() {
        return new ProcessRemoteRepoCommandBuilder()
    }
}
