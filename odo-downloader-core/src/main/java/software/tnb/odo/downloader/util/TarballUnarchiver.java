package software.tnb.odo.downloader.util;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import software.tnb.odo.downloader.config.OdoConfiguration;

public class TarballUnarchiver implements FileUnarchiver {
	@Override
	public String extension() {
		return "tar.gz";
	}

	@Override
	public File extract(File path, String entryName) throws IOException {
		final File out = File.createTempFile("odo", ".tar.gz");
		boolean found = false;
		try (InputStream fi = Files.newInputStream(path.toPath());
			 InputStream bi = new BufferedInputStream(fi);
			 InputStream gzi = new GzipCompressorInputStream(bi);
			 ArchiveInputStream archive = new TarArchiveInputStream(gzi);
			 FileOutputStream fos = new FileOutputStream(out)) {
			ArchiveEntry entry;
			while ((entry = archive.getNextEntry()) != null && !found) {
				found = entry.getName().equalsIgnoreCase(entryName);
				if (found) {
					archive.transferTo(fos);
				}
			}
		}
		path.delete();
		if (!found) {
			throw new IOException(entryName + " not found in the archive, please configure " + OdoConfiguration.ODO_ARCHIVE_ENTRY + " properly");
		}
		return out;
	}
}
