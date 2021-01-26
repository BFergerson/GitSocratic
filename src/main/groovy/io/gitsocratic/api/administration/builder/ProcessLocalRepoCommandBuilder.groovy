package io.gitsocratic.api.administration.builder

import io.gitsocratic.command.config.ImportMode
import io.gitsocratic.command.impl.AddLocalRepo

/**
 * Used to construct the 'add-local-repo' command via API.
 *
 * @version 0.2.1
 * @since 0.2
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
class ProcessLocalRepoCommandBuilder {

    private File repoLocation
    private boolean parallelProcessing = AddLocalRepo.defaultParallelProcessing

    ProcessLocalRepoCommandBuilder repoLocation(File repoLocation) {
        this.repoLocation = repoLocation
        return this
    }

    ProcessLocalRepoCommandBuilder parallelProcessing(boolean parallelProcessing) {
        this.parallelProcessing = parallelProcessing
        return this
    }

    AddLocalRepo build() {
        return new AddLocalRepo(repoLocation, ImportMode.PROCESS, parallelProcessing)
    }
}
