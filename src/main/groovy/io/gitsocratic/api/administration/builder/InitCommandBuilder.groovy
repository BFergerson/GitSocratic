package io.gitsocratic.api.administration.builder

import io.gitsocratic.command.impl.Init

/**
 * Used to construct the 'init <service>' command via API.
 *
 * @version 0.2.1
 * @since 0.2
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
abstract class InitCommandBuilder<C, S> {

    protected boolean verbose = Init.defaultVerbose
    protected boolean useServicePorts = Init.defaultUseServicePorts

    C verbose(boolean verbose) {
        this.verbose = verbose
        return (C) this
    }

    C useServicePorts(boolean useServicePorts) {
        this.useServicePorts = useServicePorts
        return (C) this
    }

    abstract S build()
}
