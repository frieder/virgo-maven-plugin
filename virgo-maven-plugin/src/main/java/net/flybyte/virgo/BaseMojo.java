package net.flybyte.virgo;

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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

/**
 * This class acts as a base class used to initialize all the parameters and configuration settings
 * needed for executing the various mojo implementations.
 * 
 * @author Frieder Heugel
 */
public abstract class BaseMojo extends AbstractMojo {
	public static final String MBEAN_SHUTDOWN = "org.eclipse.virgo.kernel:type=Shutdown";
	public static final String MBEAN_DEPLOYER = "org.eclipse.virgo.kernel:category=Control,type=Deployer";
	public static final String MBEAN_CLASSLOADING = "org.eclipse.virgo.kernel:type=Classloading";
	public static final String MBEAN_LOGBACK = "";
	protected Log logger = getLog();
	/**
	 * The location of the target folder.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 * @readonly
	 */
	protected String path;
	/**
	 * The artifact's final name (excluding the extension)
	 * 
	 * @parameter expression="${project.build.finalName}"
	 * @required
	 * @readonly
	 */
	protected String finalArtefact;
	/**
	 * The symbolic name of the bundle. The default value is
	 * <code>${project.groupId}.${project.artifactId}</code>.
	 * 
	 * @parameter property="symbolicName" default-value="${project.groupId}.${project.artifactId}"
	 * @required
	 */
	private String symbolicName;
	/**
	 * An OSGi compatible version of the project version. Default value used for transformation is
	 * <code>${project.version}</code>.
	 * 
	 * @parameter property=osgiVersion default-value="${project.version}"
	 * @required
	 */
	private String osgiVersion;
	/**
	 * The root directory of the Virgo installation.
	 * 
	 * @parameter property="virgoRoot" expression="${virgoRoot}"
	 * @required
	 */
	private File virgoRoot;
	/**
	 * The location of the truststore used by Virgo. In case this property has neither been set via
	 * VM arguments nor in the POM configuration the virgo root directory will be used and a
	 * relative path <code>/config/keystore</code> will be added to the end of it.
	 * 
	 * @parameter property="truststoreLocation" expression="${javax.net.ssl.trustStore}"
	 */
	private File truststoreLocation;
	/**
	 * The service url of the JMX management server. Default value is
	 * <code>service:jmx:rmi://localhost:9875/jndi/rmi://localhost:9875/jmxrmi</code> .
	 * 
	 * @parameter property="serviceUrl" default-value=
	 *            "service:jmx:rmi://localhost:9875/jndi/rmi://localhost:9875/jmxrmi"
	 */
	private String serviceUrl;
	/**
	 * The username used to connect to Virgo via JMX. Default value is <code>admin</code>.
	 * 
	 * @parameter property="user" default-value="admin"
	 */
	private String user;
	/**
	 * The password used to connect to Virgo via JMX. Default value is <code>springsource</code>
	 * 
	 * @parameter property="password" default-value="springsource"
	 */
	private String password;
	/**
	 * Defineds whether or not the artifact should be recovered after a server restart. Default
	 * value is <code>true</code>.
	 * 
	 * @parameter property="recoverable" default-value="true"
	 */
	private boolean recoverable;
	/**
	 * This property is used for the startup of a new Virgo instance. In case the value is higher
	 * than 0 it will wait for the defined time (in ms) until it proceeds with the build.
	 * 
	 * @parameter property="startTimeout" expression="${startTimeout}" default-value="0"
	 */
	private int startTimeout;
	private JMXConnector connector = null;
	private MBeanServerConnection connection = null;

	public abstract void execute() throws MojoExecutionException, MojoFailureException;

	// helper methods **********************************************

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
		// check whether or not truststoreLocation has been provided and does actually exist
		if (truststoreLocation == null || !truststoreLocation.exists()) {
			truststoreLocation = new File(virgoRoot, "config/keystore");
			if (!truststoreLocation.exists()) {
				throw new IOException("Cannot find a keystore file");
			}
		}
		// set path to the truststore location
		System.setProperty("javax.net.ssl.trustStore", truststoreLocation.getAbsolutePath());
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
	 * Transforms a stack trace into a String object.
	 * 
	 * @param throwable
	 * @return
	 */
	public static String stackTrace2String(Throwable throwable) {
		final Writer result = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(result);
		throwable.printStackTrace(printWriter);
		return result.toString();
	}

	/**
	 * This method identifies the complete path of the artefact (including its extension) and
	 * returns a file object. <code>${project.build.finalName}</code> doesn't provide the extension
	 * and therefore this method is needed.
	 * 
	 * @return
	 */
	public File getArtefactFile() {
		File[] files = new File(path).listFiles();
		for (File file : files) {
			if (file.isFile() && file.getName().startsWith(finalArtefact)) {
				return file;
			}
		}
		return null;
	}

	// getter/setter methods ***************************************

	public String getSymbolicName() {
		return symbolicName;
	}

	public void setSymbolicName(String symbolicName) {
		this.symbolicName = symbolicName;
	}

	public String getOsgiVersion() {
		return osgiVersion;
	}

	public void setOsgiVersion(String osgiVersion) {
		// replace the first - with .
		this.osgiVersion = osgiVersion.replaceFirst("-", ".");
	}

	public File getVirgoRoot() {
		return virgoRoot;
	}

	public void setVirgoRoot(File virgoRoot) {
		this.virgoRoot = virgoRoot;
	}

	public File getTruststoreLocation() {
		return truststoreLocation;
	}

	public void setTruststoreLocation(File truststoreLocation) {
		this.truststoreLocation = truststoreLocation;
	}

	public String getServiceUrl() {
		return serviceUrl;
	}

	public void setServiceUrl(String serviceUrl) {
		this.serviceUrl = serviceUrl;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isRecoverable() {
		return recoverable;
	}

	public void setRecoverable(boolean recoverable) {
		this.recoverable = recoverable;
	}

	public int getStartTimeout() {
		return startTimeout;
	}

	public void setStartTimeout(int startTimeout) {
		this.startTimeout = startTimeout;
	}

}
