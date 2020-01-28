package io.gitsocratic.api.administration.builder.init

import io.gitsocratic.api.administration.builder.InitCommandBuilder
import io.gitsocratic.command.impl.init.ApacheSkyWalking

/**
 * Used to construct the 'init apache_skywalking' command via API.
 *
 * @version 0.2.1
 * @since 0.2
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
class InitApacheSkyWalkingCommandBuilder extends InitCommandBuilder<InitApacheSkyWalkingCommandBuilder, ApacheSkyWalking> {

    private String skywalkingVersion = ApacheSkyWalking.defaultApacheSkyWalkingVersion

    InitApacheSkyWalkingCommandBuilder skywalkingVersion(String skywalkingVersion) {
        this.skywalkingVersion = skywalkingVersion
        return this
    }

    @Override
    ApacheSkyWalking build() {
        return new ApacheSkyWalking(skywalkingVersion, verbose, useServicePorts)
    }
}
