package io.gitsocratic.command.impl

import io.gitsocratic.api.SocraticAPI
import io.gitsocratic.command.SocraticCommandTest
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertFalse

class InitTest extends SocraticCommandTest {

    @Test
    void "default init command"() {
        def command = SocraticAPI.administration().init().build()
        assertEquals("1.5.2", command.graknVersion)
        assertEquals("v2.12.1-drivers", command.babelfishVersion)
        assertFalse(command.verbose)
    }
}
