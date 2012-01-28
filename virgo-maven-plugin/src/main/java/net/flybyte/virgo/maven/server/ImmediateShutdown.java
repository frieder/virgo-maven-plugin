package net.flybyte.virgo.maven.server;

import java.io.IOException;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import net.flybyte.virgo.maven.BaseMojo;

/**
 * Shuts a running Eclipse Virgo instance down immediately.
 * 
 * @goal immediateShutdown
 * @requiresProject true
 * 
 * @author Frieder Heugel
 * 
 */
public class ImmediateShutdown extends BaseMojo {

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			// check whether or not a JMX port has been specified in the start arguments
			checkForJMXPort();
			logger.info("Prepare immediate shutdown of Eclipse Virgo");
			MBeanServerConnection connection = getConnection();
			ObjectName name = new ObjectName(BaseMojo.MBEAN_SHUTDOWN);
			logger.info("Shutting down server instance immediately");
			// invoke the undeploy method of the Deployer MBean
			connection.invoke(name, "immediateShutdown", null, null);
		} catch (Exception e) {
			throw new MojoFailureException(stackTrace2String(e));
		} finally {
			try {
				closeConnector();
			} catch (IOException e) {
				throw new MojoFailureException(stackTrace2String(e));
			}
		}
	}

}
