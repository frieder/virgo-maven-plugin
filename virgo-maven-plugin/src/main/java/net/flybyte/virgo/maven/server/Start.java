package net.flybyte.virgo.maven.server;

import java.io.File;

import net.flybyte.virgo.maven.BaseMojo;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Starts an Eclipse Virgo instance by executing the provided startup script.
 * 
 * @goal start
 * @requiresProject true
 * 
 * @author Frieder Heugel
 */
public class Start extends BaseMojo {

	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			logger.info("Prepare startup of Eclipse Virgo");
			DefaultExecutor executor = new DefaultExecutor();
			File workingDir = new File(getVirgoRoot(), "bin");
			logger.info("Virgo root directory: " + getVirgoRoot());
			logger.info("Working directory: " + workingDir);
			executor.setWorkingDirectory(workingDir);
			// check os
			CommandLine cmdLine = determineStartScript();
			// process arguments
			processArguments(cmdLine);
			DefaultExecuteResultHandler handler = new DefaultExecuteResultHandler();
			logger.info("Starting Eclipse Virgo ...");
			executor.execute(cmdLine, handler);
			logger.debug("Delay setting found, wait for " + getDelayAfterStart() + "ms");
			handler.waitFor(getDelayAfterStart());
		} catch (Exception e) {
			throw new MojoFailureException(
					"An exception occurred while executing the startup script", e);
		}
	}

	/*
	 * Check for OS and return corresponding startup script
	 */
	private CommandLine determineStartScript() {
		CommandLine cmdLine = null;
		if (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0) { // windows
			logger.info("Windows operating system found, use startup.bat script");
			cmdLine = new CommandLine("cmd.exe");
			cmdLine.addArgument("/c");
			cmdLine.addArgument("start");
			cmdLine.addArgument("startup.bat");
		} else { // unix/mac/solaris
			logger.info("Non-windows operating system found, use startup.sh script");
			cmdLine = new CommandLine("sh");
			cmdLine.addArgument("startup.sh");
		}
		return cmdLine;
	}

	/*
	 * Process the given start arguments
	 */
	private void processArguments(CommandLine cmdLine) {
		logger.info("Parsing for start arguments to pass on");
		boolean jmxParam = false;
		for (String argument : getStartParams()) {
			if (jmxParam) { // JMX port number
				setJmxPort(argument.trim());
				logger.debug("JMX port found: " + getJmxPort());
				jmxParam = false;
			}
			if ("-jmxport".equalsIgnoreCase(argument.trim())) {
				logger.debug("JMX argument found, expect next argument to be a valid port number");
				jmxParam = true;
			}
			logger.info("Adding start argument: " + argument);
			cmdLine.addArgument(argument);
		}
	}

}
