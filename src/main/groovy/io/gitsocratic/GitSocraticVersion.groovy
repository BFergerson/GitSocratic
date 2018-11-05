package io.gitsocratic

import com.codebrig.phenomena.Phenomena
import picocli.CommandLine

/**
 * todo: description
 *
 * @version 0.1
 * @since 0.1
 * @author <a href="mailto:brandon.fergerson@codebrig.com">Brandon Fergerson</a>
 */
class GitSocraticVersion implements CommandLine.IVersionProvider {

    private static ResourceBundle buildBundle = ResourceBundle.getBundle("gitsocratic_build")

    @Override
    String[] getVersion() throws Exception {
        def dockerVersion = GitSocraticCLI.dockerClient.versionCmd().exec().apiVersion
        def strList = new ArrayList<String>()
        strList.add("GitSocratic")
        strList.add(" CLI:\t\t" + buildBundle.getString("version") + " [Build: " + buildBundle.getString("build_date") + "]")
        strList.add(" Docker: \t" + dockerVersion)
        strList.add(" Phenomena:\t" + Phenomena.PHENOMENA_VERSION + " [Schema: OmniSRC_Omnilingual_Schema-1.0]")
        strList.add(" Babelfish:\t" + buildBundle.getString("babelfish_version"))
        strList.add(" Grakn:\t\t" + buildBundle.getString("grakn_version"))
        return strList.toArray(new String[0])
    }
}
