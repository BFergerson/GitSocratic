package io.gitsocratic.command.impl

import io.gitsocratic.command.config.ConfigOption
import picocli.CommandLine

import java.util.concurrent.Callable

/**
 * todo: description
 *
 * @version 0.2
 * @since 0.1
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
@CommandLine.Command(name = "config",
        description = "Configure GitSocratic",
        mixinStandardHelpOptions = true,
        descriptionHeading = "%nDescription:%n%n",
        parameterListHeading = "%nParameters:%n",
        optionListHeading = "%nOptions:%n")
class Config implements Callable<Integer> {

    @CommandLine.Parameters(arity = "0..1", index = "0", description = "The option to get/set")
    private ConfigOption option

    @CommandLine.Parameters(arity = "0..1", index = "1", description = "The value to set the option")
    private String value

    @Override
    Integer call() throws Exception {
        if (option == null) {
            println "Configuration:"
            ConfigOption.values().each {
                println " " + it.name() + ": " + it.value
            }
        } else {
            if (value == null || value.isEmpty()) {
                println option.getValue() //get config option
            } else {
                option.setValue(value) //set config option
            }
        }
        return 0
    }
}
