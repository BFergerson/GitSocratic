package io.gitsocratic.command

import io.gitsocratic.api.SocraticAPI
import io.gitsocratic.command.config.ConfigOption

import java.util.concurrent.atomic.AtomicBoolean

import static org.junit.Assert.assertEquals

abstract class SocraticCommandTest {

    private static final AtomicBoolean setupEnvironment = new AtomicBoolean()

    SocraticCommandTest() {
        if (!setupEnvironment.getAndSet(true)) {
            assertEquals(false, SocraticAPI.administration()
                    .config(ConfigOption.use_docker_grakn, false).build().execute().newValue)
            assertEquals(false, SocraticAPI.administration()
                    .config(ConfigOption.use_docker_babelfish, false).build().execute().newValue)
            //assertEquals(0, SocraticAPI.administration().init().build().execute().status)
        }
    }
}
