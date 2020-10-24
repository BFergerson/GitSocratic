//package io.gitsocratic.integration
//
//import com.sourceplusplus.api.client.SourceCoreClient
//import com.sourceplusplus.api.model.integration.ConnectionType
//import com.sourceplusplus.api.model.integration.IntegrationConnection
//import com.sourceplusplus.api.model.integration.IntegrationInfo
//import io.gitsocratic.api.SocraticAPI
//import io.gitsocratic.command.result.InitDockerCommandResult
//import org.junit.Test
//
//import static org.junit.Assert.*
//
//class SPPLinkTest {
//
//    @Test
//    void testActivateIntegration() {
//        def initSkyWalking = SocraticAPI.administration().initApacheSkyWalking()
//                .build().execute(true)
//        assertEquals(0, initSkyWalking.status)
//
//        def initSpp = SocraticAPI.administration().initSourcePlusPlus()
//                .link("Apache_SkyWalking")
//                .build().execute(true) as InitDockerCommandResult
//        assertEquals(0, initSpp.status)
//
//        def sppBinding = initSpp.portBindings.get("8080/tcp")[0]
//        def sppHost = sppBinding.substring(0, sppBinding.indexOf(":"))
//        if (sppHost == "0.0.0.0") {
//            sppHost = "localhost"
//        }
//        def sppPort = sppBinding.substring(sppBinding.indexOf(":") + 1) as int
//
//        Thread.sleep(5000)
//        def coreClient = new SourceCoreClient(sppHost, sppPort)
//        coreClient.info({
//            if (it.succeeded()) {
//                //no active integrations
//                assertTrue(it.result().activeIntegrations().isEmpty())
//
//                //activate integration
//                def integrationInfo = coreClient.updateIntegrationInfo(IntegrationInfo.builder()
//                        .id("apache_skywalking").enabled(true)
//                        .putConnections(ConnectionType.REST, IntegrationConnection.builder().host("Apache_SkyWalking").port(12800).build())
//                        .putConnections(ConnectionType.gRPC, IntegrationConnection.builder().host("Apache_SkyWalking").port(11800).build())
//                        .build())
//                assertNotNull(integrationInfo)
//                assertTrue(integrationInfo.enabled())
//
//                coreClient.info({
//                    if (it.succeeded()) {
//                        assertFalse(it.result().activeIntegrations().isEmpty())
//                    } else {
//                        fail(it.cause().getMessage())
//                    }
//                })
//            } else {
//                fail(it.cause().getMessage())
//            }
//        })
//    }
//}
