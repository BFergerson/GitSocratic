package io.gitsocratic.command.question

import com.google.common.base.Charsets
import com.google.common.io.Resources
import io.gitsocratic.command.question.converters.NopValueConverter
import io.gitsocratic.command.question.converters.SourceLanguageQualifiedNameValue

/**
 * todo: description
 *
 * @version 0.2
 * @since 0.1
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
enum SourceQuestion {

    how_many_x_methods_are_named_x("how many [language] methods are named [name]", "(?:how many )([^\\s]+)( methods are named )([^\\s]+)",
            [new QuestionValue("language", new SourceLanguageQualifiedNameValue()),
             new QuestionValue("name", new NopValueConverter())]),
    how_many_x_methods_total("how many [language] methods total", "(?:how many )([^\\s]+)(?: methods total)",
            [new QuestionValue("language", new SourceLanguageQualifiedNameValue())]),
    how_many_methods_are_named_x("how many methods are named [name]", "(?:how many methods are named )([^\\s]+)",
            [new QuestionValue("name", new NopValueConverter())]),
    how_many_methods_are_named_like_x("how many methods are named like [name]", "(?:how many methods are named like )([^\\s]+)",
            [new QuestionValue("name", new NopValueConverter())]),
    how_many_methods_total("how many methods total"),
    what_are_the_x_most_complex_x_methods("what are the [limit] most complex [language] methods", "(?:what are the )([^\\s]+)(?: most complex )([^\\s]+)( methods)",
            [new QuestionValue("limit", new NopValueConverter()),
             new QuestionValue("language", new SourceLanguageQualifiedNameValue())]),
    what_are_the_x_most_complex_methods("what are the [limit] most complex methods", "(?:what are the )([^\\s]+)(?: most complex methods)",
            [new QuestionValue("limit", new NopValueConverter())]),
    what_is_the_most_complex_x_method("what is the most complex [language] method", "(?:what is the most complex )([^\\s]+)( method)",
            [new QuestionValue("language", new SourceLanguageQualifiedNameValue())]),
    what_is_the_most_complex_method("what is the most complex method")

    private final String formattedQuestion
    private String matchRegex
    private String userQuestion
    private final Map<String, QuestionValueConverter> valueConverters = new LinkedHashMap<>()

    SourceQuestion(String formattedQuestion) {
        this.formattedQuestion = formattedQuestion
    }

    SourceQuestion(String formattedQuestion, String matchRegex) {
        this.formattedQuestion = formattedQuestion
        this.matchRegex = matchRegex
    }

    SourceQuestion(String formattedQuestion, String matchRegex, List<QuestionValue> questionValues) {
        this.formattedQuestion = formattedQuestion
        this.matchRegex = matchRegex
        questionValues.each {
            this.valueConverters.put(it.variable, it.valueConverter)
        }
    }

    boolean isMatch(String question) {
        if (matchRegex != null) {
            return (question =~ matchRegex).matches()
        }
        return question == formattedQuestion
    }

    String getFormattedQuestion() {
        return formattedQuestion
    }

    String getQuery() {
        def questionQuery = Resources.toString(Resources.getResource("queries/questions/" + formattedQuestion + ".gql"), Charsets.UTF_8)
        if (matchRegex != null) {
            def matches = userQuestion =~ matchRegex
            int matchIndex = 1
            valueConverters.each {
                def value = matches[0][matchIndex++] as String
                if (value == " methods are named ") {
                    //todo: fix this; because I don't understand how regex matchers work apparently
                    value = matches[0][matchIndex] as String
                }
                questionQuery = questionQuery.replace("<" + it.key + ">", it.value.convert(value))
            }
        }
        return questionQuery
    }

    static SourceQuestion toSourceQuestion(String question) {
        def sourceQuestion
        values().each {
            if (it.isMatch(question)) {
                sourceQuestion = it
                sourceQuestion.userQuestion = question
            }
        }
        if (sourceQuestion != null) {
            return sourceQuestion
        }
        throw new IllegalArgumentException("Unknown question: " + question)
    }
}
