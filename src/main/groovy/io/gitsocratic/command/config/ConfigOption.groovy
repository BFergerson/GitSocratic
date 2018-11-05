package io.gitsocratic.command.config

import io.gitsocratic.GitSocraticCLI

/**
 * todo: description
 *
 * @version 0.1
 * @since 0.1
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
enum ConfigOption {

    use_docker_grakn("true"),
    docker_host("localhost"),
    docker_port("2376"),
    grakn_host("localhost"),
    grakn_port("48555"),
    grakn_keyspace("grakn"),
    use_docker_babelfish("true"),
    babelfish_host("localhost"),
    babelfish_port("9432")

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
                if (!GitSocraticCLI.getConfigFile().exists()) {
                    GitSocraticCLI.getConfigFile().createNewFile()
                }
                input = new FileInputStream(GitSocraticCLI.getConfigFile())
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
            output = new FileOutputStream(GitSocraticCLI.configFile)
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
