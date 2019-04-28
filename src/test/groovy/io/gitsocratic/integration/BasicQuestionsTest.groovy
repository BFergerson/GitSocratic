package io.gitsocratic.integration

import grakn.core.concept.answer.Numeric
import io.gitsocratic.api.SocraticAPI
import io.gitsocratic.command.config.ConfigOption
import org.junit.Test

import static org.junit.Assert.assertEquals

class BasicQuestionsTest {

    @Test
    void askBasicQuestions_sameProgram() {
        SocraticAPI.administration().shutdownActiveServices()
        assertEquals(true, SocraticAPI.administration()
                .config(ConfigOption.individual_semantic_roles, true).build().execute().newValue)
        assertEquals(true, SocraticAPI.administration()
                .config(ConfigOption.actual_semantic_roles, true).build().execute().newValue)
        assertEquals(0, SocraticAPI.administration().init().graknVersion("1.5.1").build().execute().status)

        def addRepo = SocraticAPI.administration().addRemoteRepo()
                .repoName("bfergerson/same-program").build().execute()
        assertEquals(0, addRepo.status)

        def question1 = SocraticAPI.knowledge().question()
                .question("how many java methods are named main").build().execute()
        assertEquals(0, question1.status)
        assertEquals(1, (question1.answer.get(0) as Numeric).number().longValue())

        def question2 = SocraticAPI.knowledge().question()
                .question("how many go methods total").build().execute()
        assertEquals(0, question2.status)
        assertEquals(1, (question2.answer.get(0) as Numeric).number().longValue())

        def question3 = SocraticAPI.knowledge().question()
                .question("how many methods are named main").build().execute()
        assertEquals(0, question3.status)
        assertEquals(2, (question3.answer.get(0) as Numeric).number().longValue())

        def question4 = SocraticAPI.knowledge().question()
                .question("how many methods are named like main").build().execute()
        assertEquals(0, question4.status)
        assertEquals(2, (question4.answer.get(0) as Numeric).number().longValue())

        def question5 = SocraticAPI.knowledge().question()
                .question("how many methods total").build().execute()
        assertEquals(0, question5.status)
        assertEquals(2, (question5.answer.get(0) as Numeric).number().longValue())
    }
}
