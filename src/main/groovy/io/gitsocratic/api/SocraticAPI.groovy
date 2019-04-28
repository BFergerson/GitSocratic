package io.gitsocratic.api

import io.gitsocratic.api.administration.AdministrationAPI
import io.gitsocratic.api.knowledge.KnowledgeAPI

/**
 * Main entry point for the GitSocratic API.
 *
 * @version 0.2
 * @since 0.2
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
class SocraticAPI {

    static AdministrationAPI administration() {
        return new AdministrationAPI()
    }

    static KnowledgeAPI knowledge() {
        return new KnowledgeAPI()
    }
}
