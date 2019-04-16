package io.gitsocratic.api.knowledge

import io.gitsocratic.api.knowledge.builder.GraqlQueryBuilder
import io.gitsocratic.api.knowledge.builder.QuestionCommandBuilder

/**
 * Contains the commands which interrogate the GitSocratic knowledge base.
 *
 * @version 0.2
 * @since 0.2
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
class KnowledgeAPI {

    GraqlQueryBuilder graql() {
        return new GraqlQueryBuilder()
    }

    QuestionCommandBuilder question() {
        return new QuestionCommandBuilder()
    }
}
