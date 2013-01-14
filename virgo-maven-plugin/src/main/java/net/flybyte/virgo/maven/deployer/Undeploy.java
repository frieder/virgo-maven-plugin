package net.flybyte.virgo.maven.deployer;

import java.io.IOException;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import net.flybyte.virgo.maven.BaseMojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Undeploys an already deployed maven artefact from Eclipse Virgo via the deployer MBean. Check the <a href=
 * "http://virgo-opengrok.springsource.org/xref/virgo/org.eclipse.virgo.kernel/org.eclipse.virgo.kernel.deployer/src/main/java/org/eclipse/virgo/kernel/deployer/Deployer.java"
 * >Virgo sourcecode</a> for more information.
 * 
 * @goal undeploy
 * @requiresProject true
 * 
 * @author Frieder Heugel
 *
 */
public class Undeploy extends BaseMojo {

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (!"pom".equals(getPackaging())) {
            try {
                logger.info("Start undeploying the artefact from Eclipse Virgo");
                MBeanServerConnection connection = getConnection();
                // get the Deployer MBean and set up the arguments
                logger.info("Add bundle with symbolic name '" + getSymbolicName() + "' and version '" + getOsgiVersion() + "' to the argument list");
                ObjectName name = new ObjectName(BaseMojo.MBEAN_DEPLOYER);
                Object[] params = {getSymbolicName(), getOsgiVersion()};
                String[] signature = {"java.lang.String", "java.lang.String"};
                logger.info("Undeploy bundle");
                // invoke the undeploy method of the Deployer MBean
                connection.invoke(name, "undeploy", params, signature);
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

}
