package io.gitsocratic.api.administration.builder

import io.gitsocratic.command.impl.AddRemoteRepo

/**
 * Used to construct the 'add-remote-repo' command via API.
 *
 * @version 0.2
 * @since 0.2
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
class AddRemoteRepoCommandBuilder {

    private String repoName
    private boolean parallelProcessing = AddRemoteRepo.defaultParallelProcessing

    AddRemoteRepoCommandBuilder repoName(String repoName) {
        this.repoName = repoName
        return this
    }

    AddRemoteRepoCommandBuilder parallelProcessing(boolean parallelProcessing) {
        this.parallelProcessing = parallelProcessing
        return this
    }

    AddRemoteRepo build() {
        return new AddRemoteRepo(repoName, parallelProcessing)
    }
}
