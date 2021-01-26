package io.gitsocratic.command.config

import groovy.util.logging.Slf4j
import io.gitsocratic.SocraticCLI

/**
 * Represents the configuration options which GitSocratic supports.
 *
 * @version 0.2.1
 * @since 0.1
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
@Slf4j
enum ConfigOption {

    //environment
    docker_host("localhost"),
    docker_port("2376"),
    // - grakn
    use_docker_grakn("true"),
    docker_grakn_hostname(null),
    grakn_host("localhost"),
    grakn_port("1729"),
    grakn_keyspace("grakn"),
    // - babelfish
    use_docker_babelfish("true"),
    docker_babelfish_hostname(null),
    babelfish_host("localhost"),
    babelfish_port("9432"),
    // - apache skywalking
    use_docker_apache_skywalking("true"),
    docker_apache_skywalking_hostname(null),
    apache_skywalking_host("localhost"),
    apache_skywalking_grpc_port("11800"),
    apache_skywalking_rest_port("12800"),
    // - source++
    use_docker_source_plus_plus("true"),
    docker_source_plus_plus_hostname(null),
    source_plus_plus_host("localhost"),
    source_plus_plus_port("8080"),

    //base phenomena
    source_schema("full"),
    semantic_roles("true"),

    //observers - dependence
    identifier_access("false"),
    method_call("false"),

    //observers - metric
    cyclomatic_complexity("true")

    public final String defaultValue

    ConfigOption(String defaultValue) {
        this.defaultValue = defaultValue
    }

    private static Properties configProperties

    private static void loadConfigIfNecessary() {
        if (configProperties == null) {
            configProperties = new Properties()
            InputStream input = null
            try {
                if (!SocraticCLI.getConfigFile().exists()) {
                    SocraticCLI.getConfigFile().createNewFile()
                } else {
                    log.info "Loading configuration: " + SocraticCLI.getConfigFile()
                }
                input = new FileInputStream(SocraticCLI.getConfigFile())
                configProperties.load(input)
            } catch (IOException ex) {
                throw new RuntimeException(ex)
            } finally {
                if (input != null) {
                    try {
                        input.close()
                    } catch (IOException e) {
                        throw new RuntimeException(e)
                    }
                }
            }
        }
    }

    void setValue(String value) {
        loadConfigIfNecessary()
        if (value == null) {
            configProperties.remove(name())
        } else {
            configProperties.put(name(), value)
        }

        OutputStream output = null
        try {
            output = new FileOutputStream(SocraticCLI.configFile)
            configProperties.store(output, null)
        } catch (IOException io) {
            io.printStackTrace()
        } finally {
            if (output != null) {
                try {
                    output.close()
                } catch (IOException e) {
                    throw new RuntimeException(e)
                }
            }
        }
    }

    boolean hasValue() {
        loadConfigIfNecessary()
        return configProperties.containsKey(name())
    }

    String getValue() {
        loadConfigIfNecessary()
        return configProperties.getOrDefault(name(), defaultValue)
    }
}
