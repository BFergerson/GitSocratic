package io.gitsocratic.api.administration.builder.init

import io.gitsocratic.api.administration.builder.InitCommandBuilder
import io.gitsocratic.command.impl.init.Babelfish

/**
 * Used to construct the 'init babelfish' command via API.
 *
 * @version 0.2.1
 * @since 0.2
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
class InitBabelfishCommandBuilder extends InitCommandBuilder<InitBabelfishCommandBuilder, Babelfish> {

    private String babelfishVersion = Babelfish.defaultBabelfishVersion

    InitBabelfishCommandBuilder babelfishVersion(String babelfishVersion) {
        this.babelfishVersion = babelfishVersion
        return this
    }

    @Override
    Babelfish build() {
        return new Babelfish(babelfishVersion, verbose, useServicePorts)
    }
}
