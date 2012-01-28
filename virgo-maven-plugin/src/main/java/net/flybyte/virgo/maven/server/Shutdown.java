package net.flybyte.virgo.maven.server;

import java.io.IOException;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import net.flybyte.virgo.maven.BaseMojo;

/**
 * Shuts a running Eclipse Virgo instance down.
 * 
 * @goal shutdown
 * @requiresProject true
 * 
 * @author Frieder Heugel
 * 
 */
public class Shutdown extends BaseMojo {

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			logger.info("Prepare shutdown of Eclipse Virgo");
			MBeanServerConnection connection = getConnection();
			ObjectName name = new ObjectName(BaseMojo.MBEAN_SHUTDOWN);
			logger.info("Shutting down server instance");
			// invoke the undeploy method of the Deployer MBean
			connection.invoke(name, "shutdown", null, null);
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
