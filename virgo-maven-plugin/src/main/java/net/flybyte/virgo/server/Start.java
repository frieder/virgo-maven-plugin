package net.flybyte.virgo.server;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import net.flybyte.virgo.BaseMojo;

/**
 * Starts a Eclipse Virgo instance by executing the provided startup script.
 * 
 * @goal start
 * @requiresProject true
 * 
 * @author Frieder Heugel
 * 
 */
public class Start extends BaseMojo {

	@Override
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
			if (startup == null || !startup.exists()) {
				throw new MojoFailureException("Cannot find the startup script");
			}
			logger.info("Startup script found at " + startup.getAbsolutePath()
					+ ", start server instance");
			Runtime.getRuntime().exec(startupCmd + " " + startup.getAbsolutePath());
		} catch (Exception e) {
			throw new MojoFailureException(stackTrace2String(e));
		}
	}

}
