package io.gitsocratic.integration

import io.gitsocratic.api.SocraticAPI
import io.gitsocratic.client.GraknClient
import io.gitsocratic.command.config.ConfigOption
import org.junit.Test

import static org.junit.Assert.assertEquals

class BasicQuestionsTest {

    private final GraknClient graknClient = new GraknClient()

    @Test
    void askBasicQuestions_sameProgram() {
        graknClient.resetKeyspace()
        //todo: reset config
        assertEquals(0, SocraticAPI.administration().init().build().execute().status)

        def addRepo = SocraticAPI.administration().addRemoteRepo()
                .repoName("bfergerson/same-program").build().execute()
        assertEquals(0, addRepo.status)

        def question1 = SocraticAPI.knowledge().question()
                .question("how many java methods are named main").build().execute()
        assertEquals(0, question1.status)
        assertEquals(1, question1.answer)

        def question2 = SocraticAPI.knowledge().question()
                .question("how many go methods total").build().execute()
        assertEquals(0, question2.status)
        assertEquals(1, question2.answer)

//        def question3 = SocraticAPI.knowledge().question()
//                .question("how many methods are named main").build().execute()
//        assertEquals(0, question3.status)
//        assertEquals(6, question3.answer)

//        def question4 = SocraticAPI.knowledge().question()
//                .question("how many methods are named like main").build().execute()
//        assertEquals(0, question4.status)
//        assertEquals(6, question4.answer)

        def question5 = SocraticAPI.knowledge().question()
                .question("how many methods total").build().execute()
        assertEquals(0, question5.status)
        assertEquals(6, question5.answer)
    }

    @Test
    void askBasicComplexityQuestions_methodComplexity() {
        graknClient.resetKeyspace()
        //todo: reset config
        assertEquals(true, SocraticAPI.administration()
                .config(ConfigOption.cyclomatic_complexity, true).build().execute().newValue)
        assertEquals(0, SocraticAPI.administration().init().build().execute().status)

        def addRepo = SocraticAPI.administration().addRemoteRepo()
                .repoName("bfergerson/method-complexity").build().execute()
        assertEquals(0, addRepo.status)

        def question1 = SocraticAPI.knowledge().question()
                .question("what is the most complex method").build().execute()
        assertEquals(0, question1.status)
        assertEquals("ComplexMethods.mostComplexMethod()", question1.answer)

        def question2 = SocraticAPI.knowledge().question()
                .question("what is the most complex java method").build().execute()
        assertEquals(0, question2.status)
        assertEquals("ComplexMethods.mostComplexMethod()", question2.answer)

        def question3 = SocraticAPI.knowledge().question()
                .question("what are the 2 most complex methods").build().execute()
        assertEquals(0, question3.status)
        assertEquals(["ComplexMethods.mostComplexMethod()",
                      "ComplexMethods.complexMethod()"], question3.answer)

        def question4 = SocraticAPI.knowledge().question()
                .question("what are the 2 most complex java methods").build().execute()
        assertEquals(0, question4.status)
        assertEquals(["ComplexMethods.mostComplexMethod()",
                      "ComplexMethods.complexMethod()"], question4.answer)
    }
}
