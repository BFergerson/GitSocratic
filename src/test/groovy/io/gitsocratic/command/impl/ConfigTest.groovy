package io.gitsocratic.command.impl

import io.gitsocratic.api.SocraticAPI
import io.gitsocratic.command.SocraticCommandTest
import io.gitsocratic.command.config.ConfigOption
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull

class ConfigTest extends SocraticCommandTest {

    @Test
    void "default config command"() {
        def command = SocraticAPI.administration()
                .config().build()
        def result = command.execute()
        assertNotNull(result.configuration)
    }

    @Test
    void "get config command"() {
        def command = SocraticAPI.administration()
                .config(ConfigOption.grakn_host).build()
        def result = command.execute()
        assertEquals(ConfigOption.grakn_host, result.getOption())
        assertEquals("localhost", result.getValue())
    }

    @Test
    void "set config command"() {
        def command = SocraticAPI.administration()
                .config(ConfigOption.grakn_host, "127.0.0.1").build()
        def result = command.execute()
        assertEquals(ConfigOption.grakn_host, result.getOption())
        assertEquals("localhost", result.getOldValue())
        assertEquals("127.0.0.1", result.getNewValue())

        command = SocraticAPI.administration()
                .config(ConfigOption.grakn_host, "localhost").build()
        result = command.execute()
        assertEquals(ConfigOption.grakn_host, result.getOption())
        assertEquals("127.0.0.1", result.getOldValue())
        assertEquals("localhost", result.getNewValue())
    }
}