package io.gitsocratic.api.administration.builder.init

import io.gitsocratic.api.administration.builder.InitCommandBuilder
import io.gitsocratic.command.impl.init.SourcePlusPlus

/**
 * Used to construct the 'init source_plus_plus' command via API.
 *
 * @version 0.2.1
 * @since 0.2
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
class InitSourcePlusPlusCommandBuilder extends InitCommandBuilder<InitSourcePlusPlusCommandBuilder, SourcePlusPlus> {

    private String sppVersion = SourcePlusPlus.defaultSourcePlusPlusVersion
    private Set<String> links = new HashSet<>()

    InitSourcePlusPlusCommandBuilder sppVersion(String sppVersion) {
        this.sppVersion = sppVersion
        return this
    }

    InitSourcePlusPlusCommandBuilder link(String containerName) {
        links.add(containerName)
        return this
    }

    @Override
    SourcePlusPlus build() {
        def spp = new SourcePlusPlus(sppVersion, verbose, useServicePorts)
        links.each { spp.linkContainer(it) }
        return spp
    }
}
