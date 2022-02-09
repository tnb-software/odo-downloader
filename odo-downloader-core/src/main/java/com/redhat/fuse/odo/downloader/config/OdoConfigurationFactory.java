package com.redhat.fuse.odo.downloader.config;

import java.util.ServiceLoader;

public abstract class OdoConfigurationFactory {
	public static OdoConfiguration getConfiguration() {
		return ServiceLoader.load(OdoConfiguration.class).findFirst().get();
	}
}
