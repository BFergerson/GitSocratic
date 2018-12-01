package io.gitsocratic.command.question

import org.codehaus.groovy.runtime.StackTraceUtils
import org.junit.Assert
import org.junit.Test

class SourceQuestionTest {

    @Test
    void "how many [language] methods are named [name]"() {
        def question = SourceQuestion.toSourceQuestion("how many java methods are named main")
        Assert.assertEquals(currentMethodName, question.formattedQuestion)
    }

    @Test
    void "how many [language] methods total"() {
        def question = SourceQuestion.toSourceQuestion("how many go methods total")
        Assert.assertEquals(currentMethodName, question.formattedQuestion)
    }

    @Test
    void "how many methods are named [name]"() {
        def question = SourceQuestion.toSourceQuestion("how many methods are named main")
        Assert.assertEquals(currentMethodName, question.formattedQuestion)
    }

    @Test
    void "how many methods are named like [name]"() {
        def question = SourceQuestion.toSourceQuestion("how many methods are named like main")
        Assert.assertEquals(currentMethodName, question.formattedQuestion)
    }

    @Test
    void "how many methods total"() {
        def question = SourceQuestion.toSourceQuestion("how many methods total")
        Assert.assertEquals(currentMethodName, question.formattedQuestion)
    }

    @Test
    void "what are the [limit] most complex [language] methods"() {
        def question = SourceQuestion.toSourceQuestion("what are the 5 most complex ruby methods")
        Assert.assertEquals(currentMethodName, question.formattedQuestion)
    }

    @Test
    void "what are the [limit] most complex methods"() {
        def question = SourceQuestion.toSourceQuestion("what are the 10 most complex methods")
        Assert.assertEquals(currentMethodName, question.formattedQuestion)
    }

    @Test
    void "what is the most complex [language] method"() {
        def question = SourceQuestion.toSourceQuestion("what is the most complex python method")
        Assert.assertEquals(currentMethodName, question.formattedQuestion)
    }

    @Test
    void "what is the most complex method"() {
        def question = SourceQuestion.toSourceQuestion("what is the most complex method")
        Assert.assertEquals(currentMethodName, question.formattedQuestion)
    }

    private static String getCurrentMethodName() {
        def marker = new Throwable()
        return StackTraceUtils.sanitize(marker).stackTrace[1].methodName
    }
}
