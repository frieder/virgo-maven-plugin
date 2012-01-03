package net.flybyte.virgo.maven.deployer;

import java.io.IOException;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import net.flybyte.virgo.maven.BaseMojo;
import net.flybyte.virgo.maven.ExtendedBaseMojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Deploys a maven artefact (bundle) at runtime to Eclipse Virgo via the deployer MBean. Check the <a href=
 * "http://virgo-opengrok.springsource.org/xref/virgo/org.eclipse.virgo.kernel/org.eclipse.virgo.kernel.deployer/src/main/java/org/eclipse/virgo/kernel/deployer/Deployer.java"
 * >Virgo sourcecode</a> for more information.
 * 
 * @goal deploy
 * @requiresProject true
 * 
 * @author Frieder Heugel
 */
public class Deploy extends ExtendedBaseMojo {

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			logger.info("Start deploying the artefact to Eclipse Virgo");
			MBeanServerConnection connection = getConnection();
			// get the Deployer MBean and set up the arguments
			String artefactPath = getArtefactFile().getAbsolutePath().replaceAll("\\\\", "/");
			logger.info("Add " + artefactPath + " to the argument list");
			ObjectName name = new ObjectName(BaseMojo.MBEAN_DEPLOYER);
			Object[] params  = { "file:///" + artefactPath, isRecoverable() };
			String[] signature = { "java.lang.String", "boolean" };
			logger.info("Deploy artifact");
			// invoke the deploy method of the Deployer MBean
			connection.invoke(name, "deploy", params, signature);
		}  catch (Exception e) {
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
