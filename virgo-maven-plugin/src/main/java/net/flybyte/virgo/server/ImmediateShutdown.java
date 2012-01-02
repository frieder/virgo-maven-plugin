package net.flybyte.virgo.server;

import java.io.IOException;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import net.flybyte.virgo.BaseMojo;

/**
 * Stops a running Eclipse Virgo instance immediately. Check the <a href=
 * "http://virgo-opengrok.springsource.org/xref/virgo/org.eclipse.virgo.kernel/org.eclipse.virgo.kernel.core/src/main/java/org/eclipse/virgo/kernel/core/Shutdown.java"
 * >Virgo sourcecode</a> for more information.
 * 
 * @goal immediateShutdown
 * @requiresProject false
 * 
 * @author Frieder Heugel
 * 
 */
public class ImmediateShutdown extends BaseMojo {

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			logger.info("Prepare shutdown of Eclipse Virgo");
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
