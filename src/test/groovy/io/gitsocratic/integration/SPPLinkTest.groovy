package io.gitsocratic.integration

import com.sourceplusplus.api.client.SourceCoreClient
import com.sourceplusplus.api.model.integration.IntegrationConnection
import com.sourceplusplus.api.model.integration.IntegrationInfo
import io.gitsocratic.api.SocraticAPI
import io.gitsocratic.command.result.InitDockerCommandResult
import org.junit.Test

import static org.junit.Assert.*

class SPPLinkTest {

    @Test
    void test() {
        def initSkyWalking = SocraticAPI.administration().initApacheSkyWalking()
                .build().execute()
        assertEquals(0, initSkyWalking.status)

        def initSpp = SocraticAPI.administration().initSourcePlusPlus()
                .link("Apache_SkyWalking")
                .build().execute() as InitDockerCommandResult
        assertEquals(0, initSpp.status)

        def sppBinding = initSpp.portBindings.get("8080/tcp")[0]
        def sppHost = sppBinding.substring(0, sppBinding.indexOf(":"))
        if (sppHost == "0.0.0.0") {
            sppHost = "localhost"
        }
        def sppPort = sppBinding.substring(sppBinding.indexOf(":") + 1) as int

        Thread.sleep(2000)
        def coreClient = new SourceCoreClient(sppHost, sppPort)
        def integrationInfo = coreClient.updateIntegrationInfo(IntegrationInfo.builder()
                .id("apache_skywalking").enabled(true)
                .connection(IntegrationConnection.builder().host("Apache_SkyWalking").port(12800).build())
                .build())
        assertNotNull(integrationInfo)
        assertTrue(integrationInfo.enabled())
    }
}
