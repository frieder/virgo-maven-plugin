package net.flybyte.virgo.maven;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * An extended base class that initializes some more properties that are only relevant to the
 * deployer implementations.
 * 
 * @author Frieder Heugel
 * 
 */
public abstract class ExtendedBaseMojo extends BaseMojo {
	/**
	 * The location of the target folder.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 * @readonly
	 */
	protected String path;
	/**
	 * The artifact's final name (excluding the extension)
	 * 
	 * @parameter expression="${project.build.finalName}"
	 * @required
	 * @readonly
	 */
	protected String finalArtefact;
	/**
	 * The symbolic name of the bundle. In case one is using something different than
	 * <code>${project.groupId}.${project.artifactId}</code> for the symbolic name when using the
	 * Maven bundle plugin or Bundlor Maven plugin change the value of this property accordingly.
	 * 
	 * @parameter property="symbolicName" default-value="${project.groupId}.${project.artifactId}"
	 */
	private String symbolicName;
	/**
	 * An OSGi compatible version of the project version. Default value used for transformation is
	 * <code>${project.version}</code>. Once the getter method is called it will transform the first
	 * -(hyphen) it can find into a .(dot).
	 * 
	 * @parameter property=osgiVersion default-value="${project.version}"
	 */
	private String osgiVersion;
	/**
	 * Defineds whether or not the artifact should be recovered after a server restart.
	 * 
	 * @parameter property="recoverable" default-value="true"
	 */
	private boolean recoverable;

	public abstract void execute() throws MojoExecutionException, MojoFailureException;

	/*
	 * helper methods **********************************************
	 */

	/**
	 * This method identifies the complete path of the artefact (including its extension) and
	 * returns a file object. <code>${project.build.finalName}</code> doesn't provide the extension
	 * and therefore this method is provided.
	 * 
	 * @return A file object pointing to the location of the artefact on the file system.
	 */
	public File getArtefactFile() {
		File[] files = new File(path).listFiles();
		for (File file : files) {
			if (file.isFile() && file.getName().startsWith(finalArtefact)) {
				return file;
			}
		}
		return null;
	}

	/*
	 * getter/setter methods ***************************************
	 */

	public String getSymbolicName() {
		return symbolicName;
	}

	public void setSymbolicName(String symbolicName) {
		this.symbolicName = symbolicName;
	}

	/**
	 * This method is returning ${project.version} as an OSGi version number. Therefore it will try
	 * to replace the first - (hypthen) it can find with a . (dot). E.g. a project version
	 * 1.0.0-SNAPSHOT will be transformed to 1.0.0.SNAPSHOT.
	 * 
	 * @return
	 */
	public String getOsgiVersion() {
		// replace the first - with .
		return osgiVersion.replaceFirst("-", ".");
	}

	public void setOsgiVersion(String osgiVersion) {
		this.osgiVersion = osgiVersion;
	}

	public boolean isRecoverable() {
		return recoverable;
	}

	public void setRecoverable(boolean recoverable) {
		this.recoverable = recoverable;
	}

}
