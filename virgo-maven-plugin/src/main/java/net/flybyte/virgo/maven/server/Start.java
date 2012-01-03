package net.flybyte.virgo.maven.server;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

/**
 * Starts an Eclipse Virgo instance by executing the provided startup script.
 * 
 * @goal start
 * @requiresProject true
 * 
 * @author Frieder Heugel
 * 
 */
public class Start extends AbstractMojo {
	private Log logger = getLog();
	/**
	 * The root directory of the Virgo installation.
	 * 
	 * @parameter property="virgoRoot" expression="${virgoRoot}"
	 * @required
	 */
	private File virgoRoot;

	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			logger.info("Prepare startup of Eclipse Virgo");
			String startupCmd = "cmd.exe /c start";
			String startupScript = "bin/startup.bat";
			if (System.getProperty("os.name").toLowerCase().indexOf("win") < 0) {
				startupScript = "bin/startup.sh";
				startupCmd = "sh";
			}
			File startup = new File(getVirgoRoot(), startupScript);
			if (startup == null || !startup.exists() || !startup.isFile()) {
				throw new MojoFailureException("Cannot find the startup script");
			}
			logger.info("Startup script found at " + startup.getAbsolutePath()
					+ ", start server instance");
			Runtime.getRuntime().exec(startupCmd + " " + startup.getAbsolutePath());
		} catch (IOException e) {
			throw new MojoFailureException("An exception occurred while executing the startup script", e);
		}
	}

	/*
	 *  getter/setter methods ***************************************
	 */

	public File getVirgoRoot() {
		return virgoRoot;
	}

	public void setVirgoRoot(File virgoRoot) {
		this.virgoRoot = virgoRoot;
	}

}
