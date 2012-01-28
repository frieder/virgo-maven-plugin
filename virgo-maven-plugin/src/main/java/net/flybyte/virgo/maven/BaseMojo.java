package net.flybyte.virgo.maven;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.Context;

import net.flybyte.virgo.maven.helper.MavenVersionNumberConverter;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

/**
 * This class acts as a base class used to initialize all the parameters and configuration settings needed for
 * executing most of the mojo implementations.
 * 
 * @author Frieder Heugel
 */
public abstract class BaseMojo extends AbstractMojo {
	/**
	 * The identifier for the Virgo Shutdown MBean
	 */
	public static final String MBEAN_SHUTDOWN = "org.eclipse.virgo.kernel:type=Shutdown";
	/**
	 * The identifier for the Virgo Deployer MBean
	 */
	public static final String MBEAN_DEPLOYER = "org.eclipse.virgo.kernel:category=Control,type=Deployer";
	protected Log logger = getLog();
	/**
	 * The root directory of the Virgo installation. Instead of defining this property in the pom file it is
	 * also possible to use a VM argument <code>-Dvirgo.root</code> when executing the Maven goal.
	 * 
	 * @parameter property="virgoRoot" expression="${virgo.root}"
	 * @required
	 */
	private File virgoRoot;
	private int jmxPort = 9875;
	/**
	 * An array of start arguments used when starting the virgo instance. Instead of defining this property in
	 * the pom file it is also possible to use a VM argument <code>-Dvirgo.startparameter</code> when
	 * executing the Maven goal. The start arguments have to be separated by comma.
	 * 
	 * @parameter expression="${virgo.startparameter}
	 */
	private String[] startParams;
	/**
	 * A delay value after which the goal will finish. The time must be provided in milliseconds. This may be
	 * useful if someone is trying to startup a server and wants to deploy a bundle afterwards in an automated
	 * way. Instead of defining this property in the pom file it is also possible to use a VM argument
	 * <code>-Dvirgo.startdelay</code> when executing the Maven goal.
	 * 
	 * @parameter default-value=0 expression="${virgo.startdelay}
	 */
	private int delayAfterStart;
	/**
	 * The location of the truststore used by Virgo. Instead of definining this property in the pom file it is
	 * also possible to use a VM argument <code>-Djavax.net.ssl.trustStore</code> when executing the Maven
	 * goal. In case this property has neither been set via VM arguments nor in the POM configuration the
	 * virgo root directory will be used and a relative path <code>/{configuration|config}/keystore</code>
	 * will be added to it.
	 * 
	 * @parameter property="truststoreLocation" expression="${javax.net.ssl.trustStore}"
	 */
	private File truststoreLocation;
	/**
	 * The service url of the JMX management server. <code>%d</code> will be replaced by the defined JMX port
	 * or 9875 in case no JMX port has been specified. Instead of defining this property in the pom file it is
	 * also possible to use a VM argument <code>-Dvirgo.serviceurl</code> when executing the Maven goal.
	 * 
	 * @parameter property="serviceUrl" default-value=
	 *            "service:jmx:rmi://localhost:%d/jndi/rmi://localhost:%d/jmxrmi"
	 *            expression="${virgo.serviceurl}
	 */
	private String serviceUrl;
	/**
	 * The username used to connect to Virgo via JMX. Instead of defining this property in the pom file it is
	 * also possible to use a VM argument <code>-Dvirgo.admin</code> when executing the Maven goal.
	 * 
	 * @parameter property="user" default-value="admin" expression="${virgo.admin}"
	 */
	private String user;
	/**
	 * The password used to connect to Virgo via JMX. Instead of defining this property in the pom file it is
	 * also possible to use a VM argument <code>-Dvirgo.password</code> when executing the Maven goal.
	 * 
	 * @parameter property="password" default-value="springsource" expression="${virgo.password}"
	 */
	private String password;
	/**
	 * The location of the target folder.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 * @readonly
	 */
	protected String path;
	/**
	 * The artifact's final name (excluding the extension).
	 * 
	 * @parameter expression="${project.build.finalName}"
	 * @required
	 * @readonly
	 */
	protected String finalArtefact;
	/**
	 * The type of the artifact.
	 * 
	 * @parameter expression="${project.packaging}"
	 * @required
	 * @readonly
	 */
	private String packaging;
	/**
	 * The symbolic name of the bundle. In case one is using something different than
	 * <code>${project.groupId}.${project.artifactId}</code> for the symbolic name the value of this property
	 * has to be changed accordingly. Instead of defining this property in the pom file it is also possible to
	 * use a VM argument <code>-Dvirgo.symbolicname</code> when executing the Maven goal.
	 * 
	 * @parameter property="symbolicName" default-value="${project.groupId}.${project.artifactId}"
	 *            expression="${virgo.symbolicname}
	 */
	private String symbolicName;
	/**
	 * An OSGi compatible version of the project version. In case that no OSGi version has been specified it
	 * will transform ${project.version} into a valid OSGi version. Instead of defining this property in the
	 * pom file it is also possible to use a VM argument <code>-Dvirgo.osgiversion</code> when executing the
	 * Maven goal.
	 * 
	 * @parameter property=osgiVersion default-value="${project.version}" expression="${virgo.osgiversion}
	 */
	private String osgiVersion;
	/**
	 * Defines whether or not the artifact should be recovered after a server restart. Instead of defining
	 * this property in the pom file it is also possible to use a VM argument <code>-Dvirgo.recoverable</code>
	 * when executing the Maven goal.
	 * 
	 * @parameter property="recoverable" default-value="true" expression="${virgo.recoverable}
	 */
	private boolean recoverable;

	private JMXConnector connector = null;
	private MBeanServerConnection connection = null;

	public abstract void execute() throws MojoExecutionException, MojoFailureException;

	/*
	 * helper methods **********************************************
	 */

	/**
	 * This method can be used to get an active MBeanServerConnection object.
	 * 
	 * @return A MBeanServerConnection object
	 * @throws IOException
	 */
	public MBeanServerConnection getConnection() throws IOException {
		if (connection != null) {
			return connection;
		}
		// check whether or not the location to the truststore has been provided and does actually
		// exist, for that first check the system property
		String trustStoreSystemProperty = System.getProperty("javax.net.ssl.trustStore");
		if (trustStoreSystemProperty == null || !new File(trustStoreSystemProperty).exists()
				|| !new File(trustStoreSystemProperty).isFile()) {
			// next check the truststore location setting
			if (truststoreLocation == null || !truststoreLocation.exists() || !truststoreLocation.isFile()) {
				// if non of the checks before apply fall back
				truststoreLocation = new File(virgoRoot, "configuration/keystore");
				if (!truststoreLocation.exists()) {
					truststoreLocation = new File(virgoRoot, "config/keystore");
					if (!truststoreLocation.exists()) {
						throw new IOException("Cannot find a keystore file");
					}
				}
			}
			// set path to the truststore location
			System.setProperty("javax.net.ssl.trustStore", truststoreLocation.getAbsolutePath());
		}
		// create a service url
		logger.info("Create new service URL: " + getServiceUrl());
		JMXServiceURL url = new JMXServiceURL(getServiceUrl());
		// define the user credentials
		Map<String, Object> envMap = new HashMap<String, Object>();
		envMap.put("jmx.remote.credentials", new String[] { getUser(), getPassword() });
		envMap.put(Context.SECURITY_PRINCIPAL, getUser());
		envMap.put(Context.SECURITY_CREDENTIALS, getPassword());
		// get a connector and establish a connection
		logger.info("Create a new JMX connector (user = '" + getUser() + "')");
		connector = JMXConnectorFactory.connect(url, envMap);
		logger.info("Establish a connection to the defined MBean server");
		connection = connector.getMBeanServerConnection();
		return connection;
	}

	/**
	 * Closes the JMXConnector object.
	 * 
	 * @throws IOException
	 */
	public void closeConnector() throws IOException {
		if (connector != null) {
			connector.close();
		}
	}

	/**
	 * This method identifies the complete path of the artefact (including its extension) and returns a file
	 * object. <code>${project.build.finalName}</code> doesn't provide the extension and therefore this method
	 * is provided. Currently it's checking if a file with extension jar, war or wab does exist.
	 * 
	 * @return A file object pointing to the location of the artefact on the file system.
	 * @throws MojoFailureException
	 */
	public File getArtefactFile() throws MojoFailureException {
		String ext = "jar";
		if ("jar".equalsIgnoreCase(packaging) || "bundle".equals(packaging)) {
			// do nothing, default applies
		} else if ("war".equalsIgnoreCase(packaging)) {
			ext = "war";
		} else if ("virgo-plan".equalsIgnoreCase(packaging)) {
			ext = "plan";
		} else if ("virgo-par".equalsIgnoreCase(packaging)) {
			ext = "par";
		} else if ("virgo-property".equalsIgnoreCase(packaging)) {
			ext = "properties";
			throw new MojoFailureException("", new UnsupportedOperationException(
					"The Maven packaging type 'virgo-property' is currently not supported."));
		} else {
			throw new MojoFailureException("The given Maven packaging type is currently not supported");
		}
		File artefact = new File(path, finalArtefact + "." + ext);
		if (artefact != null && artefact.exists() && artefact.isFile()) {
			return artefact;
		} else {
			return null;
		}
	}

	/**
	 * Process start arguments to check whether or not a JMX port has been specified. In case a JMX port has
	 * been found it will be set.
	 */
	protected void checkForJMXPort() {
		logger.info("Parsing the start arguments for a JMX port");
		boolean jmxParam = false;
		for (String argument : getStartParams()) {
			if (jmxParam) { // JMX port number
				setJmxPort(argument.trim());
				logger.debug("JMX port found: " + getJmxPort());
				return;
			}
			if ("-jmxport".equalsIgnoreCase(argument.trim())) {
				logger.debug("JMX argument found, expect next argument to be a valid port number");
				jmxParam = true;
			}
		}
	}

	/**
	 * Transforms a stack trace into a String object.
	 * 
	 * @param throwable
	 * @return The strack trace payload as a String object.
	 */
	public static String stackTrace2String(Throwable throwable) {
		final Writer result = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(result);
		throwable.printStackTrace(printWriter);
		return result.toString();
	}

	/*
	 * getter/setter methods ***************************************
	 */

	/**
	 * Returns the Virgo root direcotry
	 * 
	 * @return A file object pointing to the Virgo root directory
	 */
	public File getVirgoRoot() {
		return virgoRoot;
	}

	/**
	 * Sets the Virgo root directory via the POM configuration.
	 * 
	 * @param virgoRoot
	 */
	public void setVirgoRoot(File virgoRoot) {
		this.virgoRoot = virgoRoot;
	}

	/**
	 * Sets the Virgo root directory via the commandline as a system property
	 * 
	 * @param virgoRoot
	 */
	public void setVirgoRoot(String virgoRoot) {
		this.virgoRoot = new File(virgoRoot);
	}

	/**
	 * Returns the JMX port
	 * 
	 * @return
	 */
	public int getJmxPort() {
		return jmxPort;
	}

	/**
	 * Sets the JMX port. In case the port number is not valid the existing port number will remain active.
	 * 
	 * @param jmxPort
	 */
	public void setJmxPort(int jmxPort) {
		if (jmxPort >= 1 && jmxPort <= 65565) {
			this.jmxPort = jmxPort;
		} else {
			getLog().warn(
					"The given JMX port (" + jmxPort + ") is not a valid port number, use current value "
							+ this.jmxPort);
		}
	}

	/**
	 * Sets the JMX port. In case the port number is not valid the existing port number will remain active.
	 * 
	 * @param jmxPort
	 */
	public void setJmxPort(String jmxPort) {
		try {
			setJmxPort(Integer.parseInt(jmxPort));
		} catch (NumberFormatException e) {
			getLog().warn(
					"The given JMX port (" + jmxPort + ") is not a number, use current value " + this.jmxPort);
		}
	}

	/**
	 * Returns all the defined parameter that should be applied to the startup of the Virgo server
	 * 
	 * @return
	 */
	public String[] getStartParams() {
		return startParams;
	}

	/**
	 * Sets the start parameter via the POM configuration.
	 * 
	 * @param startParams
	 */
	public void setStartParams(String[] startParams) {
		this.startParams = startParams;
	}

	/**
	 * Sets the start parameter via the commandline as a system property.
	 * 
	 * @param startParams
	 */
	public void setStartParams(String startParams) {
		this.startParams = startParams.split(",");
	}

	/**
	 * Returns the delay value in ms the Maven goal should wait until it will finish the current action.
	 * 
	 * @return
	 */
	public int getDelayAfterStart() {
		return delayAfterStart;
	}

	/**
	 * Sets the delay value in ms the Maven goal should wait until it will finish the current action.
	 * 
	 * @param delayAfterStart
	 */
	public void setDelayAfterStart(int delayAfterStart) {
		this.delayAfterStart = delayAfterStart;
	}

	/**
	 * 
	 * @param truststoreLocation
	 */
	public void setTruststoreLocation(File truststoreLocation) {
		this.truststoreLocation = truststoreLocation;
		// TODO
	}

	/**
	 * Returns the service URL used to connect to the MBean server.
	 * 
	 * @return
	 */
	public String getServiceUrl() {
		return String.format(serviceUrl, jmxPort, jmxPort);
	}

	/**
	 * Set the service URL used to connect to the MBean server.
	 * 
	 * @param serviceUrl
	 */
	public void setServiceUrl(String serviceUrl) {
		this.serviceUrl = serviceUrl;
	}

	/**
	 * Get the user name used to connect to the MBean server.
	 * 
	 * @return
	 */
	public String getUser() {
		return user;
	}

	/**
	 * Set the user name used to connect to the MBean server.
	 * 
	 * @param user
	 */
	public void setUser(String user) {
		this.user = user;
	}

	/**
	 * Get the password used to connect to the MBean server.
	 * 
	 * @return
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Set the password used to connect to the MBean server.
	 * 
	 * @param password
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Return the location of the target folder as a string.
	 * 
	 * @return
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Return the name of the final artefact.
	 * 
	 * @return
	 */
	public String getFinalArtefact() {
		return finalArtefact;
	}

	/**
	 * Return the packaging type.
	 * 
	 * @return
	 */
	public String getPackaging() {
		return packaging;
	}

	/**
	 * Set the packaging type. Do not call this method directly.
	 * 
	 * @param packaging
	 */
	public void setPackaging(String packaging) {
		this.packaging = packaging;
	}

	/**
	 * Get the symbolic name.
	 * 
	 * @return
	 */
	public String getSymbolicName() {
		return symbolicName;
	}

	/**
	 * Set the symbolic name.
	 * 
	 * @param symbolicName
	 */
	public void setSymbolicName(String symbolicName) {
		this.symbolicName = symbolicName;
	}

	/**
	 * Get the OSGi version. It will use Bundlor's MavenVersionNumberConverter class to provide a valid OSGi
	 * version.
	 * 
	 * @return
	 */
	public String getOsgiVersion() {
		return MavenVersionNumberConverter.convertToOsgi(osgiVersion);
	}

	/**
	 * Get the version as defined by the user or the artefact's version in case no OSGi version has been
	 * specified. There is no quarantee that this version is a valid OSGi version.
	 * 
	 * @return
	 */
	public String getOriginalVersion() {
		return osgiVersion;
	}

	/**
	 * Set the OSGi version.
	 * 
	 * @param osgiVersion
	 */
	public void setOsgiVersion(String osgiVersion) {
		if (osgiVersion != null && osgiVersion.trim().length() > 0) {
			this.osgiVersion = osgiVersion;
		}
	}

	/**
	 * Returns whether or not an artefact is recoverable after a server restart.
	 * 
	 * @return
	 */
	public boolean isRecoverable() {
		return recoverable;
	}

	/**
	 * Define whether or not the artefact is recoverable after a server restart.
	 * 
	 * @param recoverable
	 */
	public void setRecoverable(boolean recoverable) {
		this.recoverable = recoverable;
	}

	/**
	 * Returns the JMX connector object.
	 * 
	 * @return
	 */
	public JMXConnector getConnector() {
		return connector;
	}

}
