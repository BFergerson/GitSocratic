package io.gitsocratic.api.administration.builder

import io.gitsocratic.command.config.ImportMode
import io.gitsocratic.command.impl.AddLocalRepo
import io.gitsocratic.command.impl.AddRemoteRepo

/**
 * Used to construct the 'add-remote-repo' command via API.
 *
 * @version 0.2.1
 * @since 0.2
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
class ParseRemoteRepoCommandBuilder {

    private String repoName
    private boolean parallelProcessing = AddLocalRepo.defaultParallelProcessing

    ParseRemoteRepoCommandBuilder repoName(String repoName) {
        this.repoName = repoName
        return this
    }

    ParseRemoteRepoCommandBuilder parallelProcessing(boolean parallelProcessing) {
        this.parallelProcessing = parallelProcessing
        return this
    }

    AddRemoteRepo build() {
        return new AddRemoteRepo(repoName, ImportMode.PARSE, parallelProcessing)
    }
}
