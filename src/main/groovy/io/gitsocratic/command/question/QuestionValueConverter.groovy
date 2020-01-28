package io.gitsocratic.command.question

/**
 * Used to convert user-input values for source code question variables.
 *
 * @version 0.2.1
 * @since 0.1
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
interface QuestionValueConverter {
    String convert(String value)
}
