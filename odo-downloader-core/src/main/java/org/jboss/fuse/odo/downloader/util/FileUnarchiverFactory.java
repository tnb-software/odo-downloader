package org.jboss.fuse.odo.downloader.util;

import org.jboss.fuse.odo.downloader.config.OdoConfiguration;

import java.io.IOException;
import java.util.ServiceLoader;

public abstract class FileUnarchiverFactory {
	public static FileUnarchiver getUnarchiver(OdoConfiguration configuration) throws IOException {
		return ServiceLoader.load(FileUnarchiver.class).stream()
				.filter(instance -> configuration.archiveExt().toLowerCase().contains(instance.get().extension()))
				.findFirst().orElseThrow(() -> new IOException("no file unarchiver found for extension " + configuration.archiveExt())).get();
	}
}
