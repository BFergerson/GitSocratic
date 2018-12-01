package io.gitsocratic.command.impl

import ai.grakn.graql.answer.Value
import io.gitsocratic.client.GraknClient
import io.gitsocratic.command.question.SourceQuestion
import picocli.CommandLine

import java.util.concurrent.Callable

/**
 * todo: description
 *
 * @version 0.2
 * @since 0.1
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
@CommandLine.Command(name = "question",
        description = "Execute single source code question",
        mixinStandardHelpOptions = true,
        descriptionHeading = "%nDescription:%n%n",
        parameterListHeading = "%nParameters:%n",
        optionListHeading = "%nOptions:%n")
class Question implements Callable<Integer> {

    @CommandLine.Parameters(index = "0", converter = QuestionTypeConverter.class)
    private SourceQuestion question

    @Override
    Integer call() throws Exception {
        def startTime = System.currentTimeMillis()
        def graknClient = new GraknClient()
        def query = question.query
        try {
            println "# Question: " + question.formattedQuestion
            println "# Result:"
            def result = graknClient.executeQuery(query)
            if (result.get(0) instanceof Value) {
                def number = (result.get(0) as Value).number()
                println number
            } else {
                println result
            }
            println "# Query time: " + (System.currentTimeMillis() - startTime) + "ms"
            return 0
        } finally {
            graknClient.close()
        }
    }

    static class QuestionTypeConverter implements CommandLine.ITypeConverter<SourceQuestion> {
        SourceQuestion convert(String value) throws Exception {
            return SourceQuestion.toSourceQuestion(value?.toLowerCase())
        }
    }
}
