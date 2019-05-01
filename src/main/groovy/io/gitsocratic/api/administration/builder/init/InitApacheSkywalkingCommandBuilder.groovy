package io.gitsocratic.api.administration.builder.init

import io.gitsocratic.api.administration.builder.InitCommandBuilder
import io.gitsocratic.command.impl.init.ApacheSkywalking

/**
 * Used to construct the 'init apache_skywalking' command via API.
 *
 * @version 0.2
 * @since 0.2
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
class InitApacheSkywalkingCommandBuilder extends InitCommandBuilder<InitApacheSkywalkingCommandBuilder, ApacheSkywalking> {

    private String skywalkingVersion = ApacheSkywalking.defaultApacheSkywalkingVersion

    InitApacheSkywalkingCommandBuilder skywalkingVersion(String skywalkingVersion) {
        this.skywalkingVersion = skywalkingVersion
        return this
    }

    @Override
    ApacheSkywalking build() {
        return new ApacheSkywalking(skywalkingVersion, verbose)
    }
}
