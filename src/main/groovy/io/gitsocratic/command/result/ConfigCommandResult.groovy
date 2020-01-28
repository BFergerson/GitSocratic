package io.gitsocratic.command.result

import groovy.transform.TupleConstructor
import io.gitsocratic.command.config.ConfigOption
import io.vertx.core.json.JsonObject

/**
 * 'config' command responses returned when triggered via API.
 *
 * @version 0.2.1
 * @since 0.2
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
abstract class ConfigCommandResult {

    @TupleConstructor
    static class SetValue extends ConfigCommandResult {
        final ConfigOption option
        final Object oldValue
        final Object newValue
    }

    @TupleConstructor
    static class GetValue extends ConfigCommandResult {
        final ConfigOption option
        final Object value
    }

    @TupleConstructor
    static class DisplayValues extends ConfigCommandResult {
        final JsonObject configuration
    }
}
