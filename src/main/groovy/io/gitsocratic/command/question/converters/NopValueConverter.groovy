package io.gitsocratic.command.question.converters

import io.gitsocratic.command.question.QuestionValueConverter

/**
 * todo: description
 *
 * @version 0.1
 * @since 0.1
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
class NopValueConverter implements QuestionValueConverter {
    @Override
    String convert(String value) {
        return value
    }
}
