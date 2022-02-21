package com.redhat.fuse.odo.downloader;

import com.redhat.fuse.odo.downloader.config.OdoConfiguration;
import com.redhat.fuse.odo.downloader.config.OdoConfigurationFactory;
import com.redhat.fuse.odo.downloader.util.ChecksumUtil;
import com.redhat.fuse.odo.downloader.util.FileUnarchiverFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Downloader {

	private static final Logger log = Logger.getLogger(Downloader.class.getName());

	public String downloadOdoBinary() throws IOException {
		final OdoConfiguration config = OdoConfigurationFactory.getConfiguration();
		if (Paths.get(config.odoTargetFile()).toFile().exists() && !config.forceDownload()) {
			log.log(Level.INFO, "{0} already exists, in order to force download set {1}=true",
					new Object[]{config.odoTargetFile(), OdoConfiguration.ODO_DOWNLOAD_FORCE});
			return config.odoTargetFile();
		}
		final URL downloadUrl = getDownloadUrl(config);
		final URL checksumUrl = getChecksumUrl(config);
		File tmpFile = Files.createTempFile("odo", ".bin").toFile();
		final String checksum = ChecksumUtil.getChecksumFromChecksumsFile(checksumUrl,
				downloadUrl.getFile().substring(downloadUrl.getFile().lastIndexOf('/') + 1));
		try {
			this.download(downloadUrl, tmpFile, checksum, MessageDigest.getInstance("SHA-256"));
		} catch (final NoSuchAlgorithmException e) {
			throw new IOException(e);
		} catch (final FileNotFoundException e) {
			log.log(Level.SEVERE, "{0} not found, try to change properties", e.getMessage());
			throw e;
		}
		if (config.downloadArchive()) {
			tmpFile = FileUnarchiverFactory.getUnarchiver(config)
					.extract(tmpFile, config.archiveEntryName());
		}
		tmpFile.deleteOnExit();
		final Path odoBinaryPath = Paths.get(config.odoTargetFile());
		Files.createDirectories(odoBinaryPath.getParent());
		tmpFile.setExecutable(true);
		final Path destination = Files.copy(tmpFile.toPath(), odoBinaryPath, StandardCopyOption.REPLACE_EXISTING,
				StandardCopyOption.COPY_ATTRIBUTES);

		log.log(Level.INFO, "binary downloaded to {0}", destination.toAbsolutePath());
		return destination.toAbsolutePath().toString();
	}

	public void download(final URL url, final File destFile, final String checksum, final MessageDigest digest) throws IOException {
		this.download(url, destFile);
		if (!ChecksumUtil.calculateFileChecksum(destFile, digest).equals(checksum)) {
			throw new IOException("checksum verification fails " + destFile + ":" + checksum);
		}
	}

	private void download(final URL remoteFile, final File destinationFile) throws IOException {
		log.log(Level.INFO, "downloading {0}", remoteFile);
		try (ReadableByteChannel readableByteChannel = Channels.newChannel(remoteFile.openStream());
			 FileOutputStream fileOutputStream = new FileOutputStream(destinationFile)) {
			fileOutputStream.getChannel()
					.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
			fileOutputStream.flush();
		}
	}

	private URL getChecksumUrl(final OdoConfiguration config) throws MalformedURLException {
		final String downloadChecksumFormat = "%s/%s/%s";
		return new URL(String.format(downloadChecksumFormat, config.odoMirrorBaseUrl(), config.odoVersion(), config.checksumFile()));
	}

	private URL getDownloadUrl(final OdoConfiguration config) throws MalformedURLException {
		final String ext = !config.downloadArchive() ? ""
				: config.archiveExt().startsWith(".") ? config.archiveExt() : "." + config.archiveExt();
		final String downloadFileFormat = config.clientOs().equalsIgnoreCase("windows") ? "%s/%s/odo-%s-%s.exe%s" : "%s/%s/odo-%s-%s%s";
		return new URL(String.format(downloadFileFormat, config.odoMirrorBaseUrl(), config.odoVersion(), config.clientOs(),
				config.clientArch(), ext));
	}
}
