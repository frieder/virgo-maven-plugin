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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

/**
 * This class acts as a base class used to initialize all the parameters and configuration settings
 * needed for executing the both groups of mojo implementations (server and deployer).
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
	 * The root directory of the Virgo installation.
	 * 
	 * @parameter property="virgoRoot" expression="${virgoRoot}"
	 * @required
	 */
	private File virgoRoot;
	/**
	 * The location of the truststore used by Virgo. Instead of definining this property in the pom
	 * file it is also possible to use a VM argument <code>-Djavax.net.ssl.trustStore</code> when
	 * executing the Maven goal. In case this property has neither been set via VM arguments nor in
	 * the POM configuration the virgo root directory will be used and a relative path
	 * <code>/config/keystore</code> will be added to it.
	 * 
	 * @parameter property="truststoreLocation" expression="${javax.net.ssl.trustStore}"
	 */
	private File truststoreLocation;
	/**
	 * The service url of the JMX management server.
	 * 
	 * @parameter property="serviceUrl" default-value=
	 *            "service:jmx:rmi://localhost:9875/jndi/rmi://localhost:9875/jmxrmi"
	 */
	private String serviceUrl;
	/**
	 * The username used to connect to Virgo via JMX.
	 * 
	 * @parameter property="user" default-value="admin"
	 */
	private String user;
	/**
	 * The password used to connect to Virgo via JMX.
	 * 
	 * @parameter property="password" default-value="springsource"
	 */
	private String password;
	private JMXConnector connector = null;
	private MBeanServerConnection connection = null;

	public abstract void execute() throws MojoExecutionException, MojoFailureException;

	/*
	 *  helper methods **********************************************
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
		// exist
		String trustStoreArgument = System.getProperty("javax.net.ssl.trustStore");
		if (trustStoreArgument == null || !new File(trustStoreArgument).exists()
				|| !new File(trustStoreArgument).isFile()) {
			if (truststoreLocation == null || !truststoreLocation.exists()) {
				truststoreLocation = new File(virgoRoot, "config/keystore");
				if (!truststoreLocation.exists()) {
					throw new IOException("Cannot find a keystore file");
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
	 *  getter/setter methods ***************************************
	 */

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

}
