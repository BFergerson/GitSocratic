package io.gitsocratic.command.question

import groovy.transform.Canonical

/**
 * todo: description
 *
 * @version 0.2
 * @since 0.1
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
@Canonical
class QuestionValue {

    String variable
    QuestionValueConverter valueConverter

    QuestionValue(String variable, QuestionValueConverter valueConverter) {
        this.variable = variable
        this.valueConverter = valueConverter
    }
}
