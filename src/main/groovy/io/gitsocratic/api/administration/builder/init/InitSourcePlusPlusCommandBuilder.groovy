package io.gitsocratic.api.administration.builder.init

import io.gitsocratic.api.administration.builder.InitCommandBuilder
import io.gitsocratic.command.impl.init.SourcePlusPlus

/**
 * Used to construct the 'init source_plus_plus' command via API.
 *
 * @version 0.2
 * @since 0.2
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
class InitSourcePlusPlusCommandBuilder extends InitCommandBuilder<InitSourcePlusPlusCommandBuilder, SourcePlusPlus> {

    private String sppVersion

    InitSourcePlusPlusCommandBuilder sppVersion(String sppVersion) {
        this.sppVersion = sppVersion
        return this
    }

    @Override
    SourcePlusPlus build() {
        return new SourcePlusPlus(sppVersion, verbose)
    }
}
