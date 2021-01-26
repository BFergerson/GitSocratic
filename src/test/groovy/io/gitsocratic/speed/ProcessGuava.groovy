package io.gitsocratic.speed

import io.gitsocratic.api.SocraticAPI
import io.gitsocratic.client.GraknClient
import io.gitsocratic.command.config.ConfigOption
import org.junit.Ignore
import org.junit.Test

import static org.junit.Assert.assertEquals

class ProcessGuava {

    private final GraknClient graknClient = new GraknClient()

    @Ignore
    @Test
    void processGuava() {
        graknClient.resetKeyspace()
        //todo: reset config
        assertEquals(0, SocraticAPI.administration().init().build().execute(true).status)
        SocraticAPI.administration().config(ConfigOption.source_schema, "necessary,files,functions")
        SocraticAPI.administration().config(ConfigOption.cyclomatic_complexity, true)

//        def parseRepo = SocraticAPI.administration().parseRemoteRepo()
//                .repoName("google/guava").build().execute()
        //parse = 1min

        def addRepo = SocraticAPI.administration().processRemoteRepo()
                .repoName("google/guava").build().execute()
        assertEquals(0, addRepo.status)
    }
}
