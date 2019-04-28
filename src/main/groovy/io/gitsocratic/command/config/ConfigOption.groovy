package io.gitsocratic.command.config

import io.gitsocratic.SocraticCLI

/**
 * Represents the configuration options which GitSocratic supports.
 *
 * @version 0.2
 * @since 0.1
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
enum ConfigOption {

    //environment
    use_docker_grakn("true"),
    docker_host("localhost"),
    docker_port("2376"),
    grakn_host("localhost"),
    grakn_port("48555"),
    grakn_keyspace("grakn"),
    use_docker_babelfish("true"),
    babelfish_host("localhost"),
    babelfish_port("9432"),
    use_docker_apache_skywalking("true"),
    apache_skywalking_host("localhost"),
    apache_skywalking_port("12800"),
    use_docker_source_plus_plus("true"),
    source_plus_plus_host("localhost"),
    source_plus_plus_port("8080"),

    //phenomena
    source_schema("full"),
    individual_semantic_roles("false"),
    actual_semantic_roles("false"),

    //observers - dependence
    identifier_access("false"),
    method_call("false"),

    //observers - metric
    cyclomatic_complexity("false")

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
