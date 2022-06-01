package org.jboss.fuse.odo.downloader.util;

import static org.jboss.fuse.odo.downloader.config.OdoConfiguration.ODO_ARCHIVE_ENTRY;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ZipUnarchiver implements FileUnarchiver {
	@Override
	public String extension() {
		return "zip";
	}

	@Override
	public File extract(File path, String entryName) throws IOException {
		final File out = File.createTempFile("odo", ".zip");
		final ZipFile zipFile = new ZipFile(path);
		final ZipArchiveEntry archiveEntry = zipFile.getEntry(entryName);
		if(archiveEntry == null) {
			throw new IOException(entryName + " not found in the archive, please configure " + ODO_ARCHIVE_ENTRY + " properly");
		}
		try (
			 InputStream inputStream = zipFile.getInputStream(archiveEntry);
			 FileOutputStream fos = new FileOutputStream(out)) {
			inputStream.transferTo(fos);
		}
		zipFile.close();
		path.delete();
		return out;
	}
}
