package net.flybyte.virgo.maven.deployer;

import java.io.IOException;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import net.flybyte.virgo.maven.BaseMojo;

/**
 * Refresh any bundle with the given symbolic name and version and any bundles cloned from a bundle
 * with the given symbolic name and version. Check the <a href=
 * "http://virgo-opengrok.springsource.org/xref/virgo/org.eclipse.virgo.kernel/org.eclipse.virgo.kernel.deployer/src/main/java/org/eclipse/virgo/kernel/deployer/Deployer.java"
 * >Virgo sourcecode</a> for more information.
 * 
 * @goal refreshBundle
 * @requiresProject true
 * 
 * @author Frieder Heugel
 */
public class RefreshBundle extends BaseMojo {

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			logger.info("Start refreshing bundle");
			MBeanServerConnection connection = getConnection();
			// get the Deployer MBean and set up the arguments
			logger.info("Add bundle with symbolic name '" + getSymbolicName() + "' and version '"
					+ getOsgiVersion() + "' to the argument list");
			ObjectName name = new ObjectName(BaseMojo.MBEAN_DEPLOYER);
			Object[] params = { getSymbolicName(), getOsgiVersion() };
			String[] signature = { "java.lang.String", "java.lang.String" };
			logger.info("Refresh bundle");
			// invoke the undeploy method of the Deployer MBean
			connection.invoke(name, "refreshBundle", params, signature);
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
