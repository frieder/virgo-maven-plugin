package net.flybyte.virgo.maven.deployer;

import java.io.IOException;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import net.flybyte.virgo.maven.BaseMojo;
import net.flybyte.virgo.maven.ExtendedBaseMojo;

/**
 * Refresh a single module of the application which was deployed from the given URI via Eclipse
 * Virgo's MBean. Check the <a href=
 * "http://virgo-opengrok.springsource.org/xref/virgo/org.eclipse.virgo.kernel/org.eclipse.virgo.kernel.deployer/src/main/java/org/eclipse/virgo/kernel/deployer/Deployer.java"
 * >Virgo sourcecode</a> for more information.
 * 
 * @goal refresh
 * @requiresProject true
 * 
 * @author Frieder Heugel
 * 
 */
public class Refresh extends ExtendedBaseMojo {

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			logger.info("Start refreshing single module");
			MBeanServerConnection connection = getConnection();
			String artefactPath = getArtefactFile().getAbsolutePath().replaceAll("\\\\", "/");
			// get the Deployer MBean and set up the arguments
			logger.info("Add module with uri 'file:///" + artefactPath + "' and symbolic name '"
					+ getSymbolicName() + "' to the argument list");
			ObjectName name = new ObjectName(BaseMojo.MBEAN_DEPLOYER);
			Object[] params = { "file:///" + artefactPath, getOsgiVersion() };
			String[] signature = { "java.lang.String", "java.lang.String" };
			logger.info("Refresh module");
			// invoke the undeploy method of the Deployer MBean
			connection.invoke(name, "refresh", params, signature);
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
