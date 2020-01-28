package io.gitsocratic.api.administration.builder.init

import io.gitsocratic.api.administration.builder.InitCommandBuilder
import io.gitsocratic.command.impl.init.Grakn

/**
 * Used to construct the 'init grakn' command via API.
 *
 * @version 0.2.1
 * @since 0.2
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
class InitGraknCommandBuilder extends InitCommandBuilder<InitGraknCommandBuilder, Grakn> {

    private String graknVersion = Grakn.defaultGraknVersion

    InitGraknCommandBuilder graknVersion(String graknVersion) {
        this.graknVersion = graknVersion
        return this
    }

    @Override
    Grakn build() {
        return new Grakn(graknVersion, verbose, useServicePorts)
    }
}
