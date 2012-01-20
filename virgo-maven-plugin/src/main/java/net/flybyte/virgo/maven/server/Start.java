package net.flybyte.virgo.maven.server;

import java.io.File;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.lang.StringUtils;
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
	/**
	 * An array of start arguments used when starting the virgo instance
	 * 
	 * @parameter
	 */
	private String[] startParams;
	/**
	 * A delay value after which the goal will finish. The time must be provided in milliseconds.
	 * This may be useful if someone is trying to startup a server and wants to deploy a bundle
	 * afterwards in an automated way.
	 * 
	 * @parameter default-value=1
	 */
	private int delayAfterStart;

	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			logger.info("Prepare startup of Eclipse Virgo");
			DefaultExecutor executor = new DefaultExecutor();
			File workingDir = new File(virgoRoot, "bin");
			logger.info("Virgo root directory: " + virgoRoot);
			logger.info("Working directory: " + workingDir);
			executor.setWorkingDirectory(workingDir);
			// check os
			CommandLine cmdLine = null;
			if (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0) { // windows
				logger.info("Windows operating system found, use startup.bat script");
				cmdLine = new CommandLine("cmd");
				cmdLine.addArgument("/c");
				cmdLine.addArgument("start");
				cmdLine.addArgument("startup.bat");
			} else { // unix/mac/solaris
				logger.info("Non-windows operating system found, use startup.sh script");
				cmdLine = new CommandLine("sh");
				cmdLine.addArgument("startup.sh");
			}
			logger.info("Parsing for boot arguments to pass on");
			for (String argument : startParams) {
				logger.info("Adding start argument: " + argument);
				cmdLine.addArgument(argument);
			}
			DefaultExecuteResultHandler handler = new DefaultExecuteResultHandler();
			logger.info("Starting Eclipse Virgo ...");
			executor.execute(cmdLine, handler);
			handler.waitFor(delayAfterStart);
		} catch (Exception e) {
			throw new MojoFailureException(
					"An exception occurred while executing the startup script", e);
		}
	}

	/*
	 * getter/setter methods ***************************************
	 */

	public File getVirgoRoot() {
		return virgoRoot;
	}

	public void setVirgoRoot(File virgoRoot) {
		this.virgoRoot = virgoRoot;
	}

	public String[] getStartParams() {
		return startParams;
	}

	public void setStartParams(String[] startParams) {
		this.startParams = startParams;
	}

	public int getDelayAfterStart() {
		return delayAfterStart;
	}

	public void setDelayAfterStart(int delayAfterStart) {
		this.delayAfterStart = delayAfterStart;
	}

}
