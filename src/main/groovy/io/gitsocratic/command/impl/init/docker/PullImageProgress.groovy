package io.gitsocratic.command.impl.init.docker

import com.github.dockerjava.api.command.PullImageResultCallback
import com.github.dockerjava.api.model.PullResponseItem

/**
 * Used to track the progress of Docker image download.
 *
 * @version 0.2.1
 * @since 0.2
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
class PullImageProgress extends PullImageResultCallback {

    private Set<String> seenStatuses = new HashSet<>()
    private boolean verbose = false
    private final PrintWriter out

    PullImageProgress(PrintWriter out) {
        this.out = out
    }

    @Override
    void onNext(PullResponseItem item) {
        super.onNext(item)

        def status
        if (item.id == null) {
            status = item.status
        } else {
            status = "Id: " + item.id + " - Status: " + item.status
        }
        if (!seenStatuses.contains(status)) {
            out.println " " + status
            seenStatuses.add(status)
        }
        if (item.progress != null && verbose) out.println " Id: " + item.id + " - Progress: " + item.progress
    }
}