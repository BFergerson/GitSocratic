package io.gitsocratic.api.administration.builder

import groovy.transform.TupleConstructor
import io.gitsocratic.command.config.ConfigOption
import io.gitsocratic.command.impl.Config
import io.gitsocratic.command.result.ConfigCommandResult

/**
 * Used to construct the 'config' command via API.
 *
 * @version 0.2.1
 * @since 0.2
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
class ConfigCommandBuilder {

    static class DisplayValues {

        Config<ConfigCommandResult.DisplayValues> build() {
            return new Config()
        }
    }

    @TupleConstructor
    static class GetValue {

        ConfigOption option

        Config<ConfigCommandResult.GetValue> build() {
            return new Config(option: option)
        }
    }

    @TupleConstructor
    static class SetValue {

        ConfigOption option
        String value

        Config<ConfigCommandResult.SetValue> build() {
            return new Config(option: option, value: value)
        }
    }
}
