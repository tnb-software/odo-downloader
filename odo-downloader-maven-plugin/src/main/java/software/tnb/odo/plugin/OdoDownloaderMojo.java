package software.tnb.odo.plugin;

import org.apache.commons.lang3.SystemUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.InstantiationStrategy;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import software.tnb.odo.downloader.Downloader;
import software.tnb.odo.downloader.config.OdoConfiguration;

import java.io.IOException;
import java.util.Properties;

@Mojo(name = "download", defaultPhase = LifecyclePhase.GENERATE_RESOURCES,
		requiresOnline = true, requiresProject = false, instantiationStrategy = InstantiationStrategy.SINGLETON)
public class OdoDownloaderMojo extends AbstractMojo {

	private Downloader downloader = new Downloader();

	@Parameter(property = "odo.client.auto", defaultValue = "true")
	private boolean clientAuto;

	@Parameter
	private Properties odoProperties;

	@Override
	public void execute() throws MojoExecutionException {
		try {
			if (clientAuto) {
				discoverCurrentClient();
			}
			//manual setting always overrides properties
			if (odoProperties != null) {
				setSystemProperties(odoProperties);
			}
			getLog().debug("configuration used : ");
			String result = downloader.downloadOdoBinary();
			getLog().debug("file saved on : " + result);
		} catch (IOException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}

	private void setSystemProperties(Properties odoProperties) {
		odoProperties.entrySet().stream()
				.filter(entry -> entry.getKey().toString().startsWith(OdoConfiguration.CONFIG_PREFIX))
				.forEach(entry -> {
					System.setProperty((String) entry.getKey(), (String) entry.getValue());
				});
	}

	/**
	 * Set configuration properly for current environment.
	 */
	private void discoverCurrentClient() {
		if (SystemUtils.IS_OS_WINDOWS) {
			System.setProperty(OdoConfiguration.ODO_CLIENT_OS, "windows");
		} else if (SystemUtils.IS_OS_MAC) {
			System.setProperty(OdoConfiguration.ODO_CLIENT_OS, "darwin");
		} else if (SystemUtils.IS_OS_ZOS || "s390x".equals(SystemUtils.OS_ARCH) || SystemUtils.OS_VERSION.contains("s390x")) {
			System.setProperty(OdoConfiguration.ODO_CLIENT_ARCH, "s390x");
		} else if ("ppc64le".equals(SystemUtils.OS_ARCH) || SystemUtils.OS_VERSION.contains("ppc64le")) {
			System.setProperty(OdoConfiguration.ODO_CLIENT_ARCH, "ppc64le");
		}
	}

}
