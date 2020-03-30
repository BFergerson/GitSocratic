package io.gitsocratic.command.question

import org.junit.Test

import static org.junit.Assert.assertEquals

class SourceQuestionTest {

    @Test
    void howManyXMethodsAreNamedX() {
        def questionLiteral = "how many [language] methods are named [name]"
        def question = SourceQuestion.toSourceQuestion("how many java methods are named main")
        assertEquals(questionLiteral, question.formattedQuestion)
    }

    @Test
    void howManyXMethodsTotal() {
        def questionLiteral = "how many [language] methods total"
        def question = SourceQuestion.toSourceQuestion("how many go methods total")
        assertEquals(questionLiteral, question.formattedQuestion)
    }

    @Test
    void howManyMethodsAreNamedX() {
        def questionLiteral = "how many methods are named [name]"
        def question = SourceQuestion.toSourceQuestion("how many methods are named main")
        assertEquals(questionLiteral, question.formattedQuestion)
    }

    @Test
    void howManyMethodsAreNamedLikeX() {
        def questionLiteral = "how many methods are named like [name]"
        def question = SourceQuestion.toSourceQuestion("how many methods are named like main")
        assertEquals(questionLiteral, question.formattedQuestion)
    }

    @Test
    void howManyMethodsTotal() {
        def questionLiteral = "how many methods total"
        def question = SourceQuestion.toSourceQuestion("how many methods total")
        assertEquals(questionLiteral, question.formattedQuestion)
    }

    @Test
    void whatAreTheXMostComplexXMethods() {
        def questionLiteral = "what are the [limit] most complex [language] methods"
        def question = SourceQuestion.toSourceQuestion("what are the 5 most complex ruby methods")
        assertEquals(questionLiteral, question.formattedQuestion)
    }

    @Test
    void whatAreTheXMostComplexMethods() {
        def questionLiteral = "what are the [limit] most complex methods"
        def question = SourceQuestion.toSourceQuestion("what are the 10 most complex methods")
        assertEquals(questionLiteral, question.formattedQuestion)
    }

    @Test
    void whatIsTheMostComplexXMethod() {
        def questionLiteral = "what is the most complex [language] method"
        def question = SourceQuestion.toSourceQuestion("what is the most complex python method")
        assertEquals(questionLiteral, question.formattedQuestion)
    }

    @Test
    void whatIsTheMostComplexMethod() {
        def questionLiteral = "what is the most complex method"
        def question = SourceQuestion.toSourceQuestion("what is the most complex method")
        assertEquals(questionLiteral, question.formattedQuestion)
    }
}
