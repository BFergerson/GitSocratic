package io.gitsocratic.command.impl

import groovy.transform.ToString
import groovy.util.logging.Slf4j
import io.gitsocratic.command.config.ConfigOption
import io.gitsocratic.command.result.ConfigCommandResult
import io.vertx.core.json.JsonObject
import picocli.CommandLine

import java.util.concurrent.Callable

/**
 * Represents the `config` command.
 * Used to configure GitSocratic.
 *
 * @version 0.2.1
 * @since 0.1
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
@Slf4j
@ToString(includePackage = false, includeNames = true)
@CommandLine.Command(name = "config",
        description = "Configure GitSocratic",
        mixinStandardHelpOptions = true,
        descriptionHeading = "%nDescription:%n%n",
        parameterListHeading = "%nParameters:%n",
        optionListHeading = "%nOptions:%n")
class Config<T extends ConfigCommandResult> implements Callable<Integer> {

    @CommandLine.Parameters(arity = "0..1", index = "0", description = "The option to get/set")
    ConfigOption option

    @CommandLine.Parameters(arity = "0..1", index = "1", description = "The value to set the option")
    String value

    @Override
    Integer call() throws Exception {
        executeCommand(true)
        return 0
    }

    T execute() throws Exception {
        return executeCommand(false)
    }

    private T executeCommand(boolean outputLogging) throws Exception {
        if (option == null) {
            def configuration = new JsonObject()
            ConfigOption.values().each {
                configuration.put(it.name(), it.value)
            }

            if (outputLogging) {
                log.info "GitSocratic Configuration: " + configuration.encodePrettily()
            }
            return (T) new ConfigCommandResult.DisplayValues(configuration)
        } else {
            if (value == null || value.isEmpty()) {
                value = option.getValue() //get config option
                if (outputLogging) log.info value

                if (value.toLowerCase() == "true" || value.toLowerCase() == "false") {
                    return (T) new ConfigCommandResult.GetValue(option, Boolean.parseBoolean(value))
                } else {
                    return (T) new ConfigCommandResult.GetValue(option, value)
                }
            } else {
                def oldValue = option.getValue()
                option.setValue(value) //set config option

                if (oldValue.toLowerCase() == "true" || oldValue.toLowerCase() == "false") {
                    return (T) new ConfigCommandResult.SetValue(option,
                            Boolean.parseBoolean(oldValue), Boolean.parseBoolean(value))
                } else {
                    return (T) new ConfigCommandResult.SetValue(option, oldValue, value)
                }
            }
        }
    }
}
