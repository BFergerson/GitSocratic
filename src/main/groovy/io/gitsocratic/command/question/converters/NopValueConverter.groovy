package io.gitsocratic.command.question.converters

import io.gitsocratic.command.question.QuestionValueConverter

/**
 * Performs no-operation on user-input value.
 *
 * @version 0.2.1
 * @since 0.1
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
class NopValueConverter implements QuestionValueConverter {
    @Override
    String convert(String value) {
        return value
    }
}
