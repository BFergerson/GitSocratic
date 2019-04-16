package io.gitsocratic.api.administration

import groovy.transform.TupleConstructor
import io.gitsocratic.command.config.ConfigOption
import io.gitsocratic.command.impl.Config
import io.gitsocratic.command.impl.Init
import io.gitsocratic.command.result.ConfigCommandResult

class AdministrationAPI {

    def init() {
        return Init.builder()
                .graknVersion(Init.defaultGraknVersion)
                .babelfishVersion(Init.defaultBabelfishVersion)
                .initGrakn(Init.defaultInitGrakn)
                .initBabelfish(Init.defaultInitBabelfish)
                .verbose(Init.defaultVerbose)
    }

    DisplayValuesBuilder config() {
        return new DisplayValuesBuilder()
    }

    GetValueBuilder config(ConfigOption option) {
        return new GetValueBuilder(option)
    }


    SetValueBuilder config(ConfigOption option, boolean value) {
        return new SetValueBuilder(option, Boolean.toString(value))
    }

    SetValueBuilder config(ConfigOption option, String value) {
        return new SetValueBuilder(option, value)
    }

    static class DisplayValuesBuilder {

        Config<ConfigCommandResult.DisplayValues> build() {
            return new Config()
        }
    }

    @TupleConstructor
    static class GetValueBuilder {

        ConfigOption option

        Config<ConfigCommandResult.GetValue> build() {
            return new Config(option: option)
        }
    }

    @TupleConstructor
    static class SetValueBuilder {

        ConfigOption option
        String value

        Config<ConfigCommandResult.SetValue> build() {
            return new Config(option: option, value: value)
        }
    }
}
