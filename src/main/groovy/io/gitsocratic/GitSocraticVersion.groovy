package io.gitsocratic

import com.codebrig.phenomena.Phenomena
import picocli.CommandLine

/**
 * Used to display the current version of GitSocratic and the services it uses.
 *
 * @version 0.2
 * @since 0.1
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
class GitSocraticVersion implements CommandLine.IVersionProvider {

    private static ResourceBundle buildBundle = ResourceBundle.getBundle("gitsocratic_build")

    @Override
    String[] getVersion() throws Exception {
        def dockerVersion = SocraticCLI.dockerClient.versionCmd().exec().apiVersion
        def versionList = new ArrayList<String>()
        versionList.add("GitSocratic")
        versionList.add(" CLI:\t\t" + buildBundle.getString("version")
                + " [Build: " + buildBundle.getString("build_date") + "]")
        versionList.add(" Docker: \t" + dockerVersion)
        versionList.add(" Phenomena:\t" + Phenomena.PHENOMENA_VERSION + " [Schema: OmniSRC_Omnilingual_Schema]")
        versionList.add(" Babelfish:\t" + buildBundle.getString("babelfish_version"))
        versionList.add(" Grakn:\t\t" + buildBundle.getString("grakn_version"))
        return versionList.toArray(new String[0])
    }
}
