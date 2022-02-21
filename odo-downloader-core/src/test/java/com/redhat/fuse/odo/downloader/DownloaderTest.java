package com.redhat.fuse.odo.downloader;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.redhat.fuse.odo.downloader.config.OdoConfiguration;
import com.redhat.fuse.odo.downloader.config.OdoConfigurationFactory;
import com.redhat.fuse.odo.downloader.util.ChecksumUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DownloaderTest {

	private final Downloader downloader = new Downloader();

	@AfterEach
	void cleanupBinaryAndProperties() throws IOException {
		final OdoConfiguration config = OdoConfigurationFactory.getConfiguration();
		Files.deleteIfExists(Paths.get(config.odoTargetFile()));
		System.getProperties().keySet().stream()
				.filter(key -> key.toString().startsWith("odo."))
				.forEach(key -> System.clearProperty(key.toString()));
	}

	@Test
	void downloadAndVerifyChcksum() throws IOException, NoSuchAlgorithmException {
		final File destFile = Files.createTempFile("odo", ".bin").toFile();
		destFile.deleteOnExit();
		final String remoteFile = "https://developers.redhat.com/content-gateway/file/pub/openshift-v4/clients/odo/v2.5.0/odo-linux-amd64.tar.gz";
		final String remoteFileHash = "https://developers.redhat.com/content-gateway/file/pub/openshift-v4/clients/odo/v2.5.0/sha256sum.txt";
		final String checksum = ChecksumUtil.getChecksumFromChecksumsFile(new URL(remoteFileHash), "odo-linux-amd64.tar.gz");

		boolean errorDuringDownloadAndVerify = false;
		try {
			downloader.download(new URL(remoteFile), destFile, checksum, MessageDigest.getInstance("SHA-256"));
		} catch (final IOException ex) {
			errorDuringDownloadAndVerify = true;
		}

		Assertions.assertFalse(errorDuringDownloadAndVerify);

	}

	@Test
	void downloadAndVerifyWrongChecksum() throws IOException {
		final File destFile = Files.createTempFile("odo", ".bin").toFile();
		destFile.deleteOnExit();
		final String remoteFile = "https://developers.redhat.com/content-gateway/file/pub/openshift-v4/clients/odo/v2.5.0/odo-linux-amd64.tar.gz";

		Assertions.assertThrowsExactly(IOException.class,
				() -> downloader.download(new URL(remoteFile), destFile, "fake hash", MessageDigest.getInstance("SHA-256")));
	}

	@Test
	void downloadOdoUsingDefaultConfig() throws IOException {
		downloadOdo();
	}

	@Test
	void downloadOdoForWindows() throws IOException {
		System.setProperty(OdoConfiguration.ODO_CLIENT_OS, "windows");
		downloadOdo();
	}

	@Test
	void downloadUncompressed() throws IOException {
		System.setProperty(OdoConfiguration.ODO_DOWNLOAD_ARCHIVE, "false");
		downloadOdo();
	}

	@Test
	void downloadUncompressedOnWindows() throws IOException {
		System.setProperty(OdoConfiguration.ODO_DOWNLOAD_ARCHIVE, "false");
		System.setProperty(OdoConfiguration.ODO_CLIENT_OS, "windows");
		downloadOdo();
	}

	@Test
	void downloadS390x() throws IOException {
		System.setProperty(OdoConfiguration.ODO_CLIENT_ARCH, "s390x");
		downloadOdo();
	}

	@Test
	void downloadPowerPc() throws IOException {
		System.setProperty(OdoConfiguration.ODO_CLIENT_ARCH, "ppc64le");
		downloadOdo();
	}

	@Test
	void downloadDarwin() throws IOException {
		System.setProperty(OdoConfiguration.ODO_CLIENT_OS, "darwin");
		downloadOdo();
	}

	@Test
	void anotherMirror() throws IOException {
		System.setProperty(OdoConfiguration.ODO_MIRROR_BASE_URL, "https://mirror.openshift.com/pub/openshift-v4/clients/odo");
		downloadOdo();
	}

	@Test
	void tryWrongOS() {
		System.setProperty(OdoConfiguration.ODO_CLIENT_OS, "fakeOs");
		Assertions.assertThrowsExactly(FileNotFoundException.class, this::downloadOdo);
	}

	@Test
	void downloadOnCustomLocation() throws IOException {
		final Path destination = Paths.get(System.getProperty("java.io.tmpdir", "."), "odo.custom").toAbsolutePath();
		System.setProperty(OdoConfiguration.ODO_TARGET_FILE, destination.toString());
		downloadOdo();
		Assertions.assertTrue(Files.exists(destination));
		Files.delete(destination);
	}

	@Test
	void skipDownloadIfExists() throws IOException {
		Assertions.assertEquals(Files.readAttributes(Paths.get(downloadOdo()), BasicFileAttributes.class).lastModifiedTime(),
				Files.readAttributes(Paths.get(downloadOdo()), BasicFileAttributes.class).lastModifiedTime());
	}

	@Test
	void forceDownload() throws IOException {
		System.setProperty(OdoConfiguration.ODO_DOWNLOAD_FORCE, "true");
		Assertions.assertNotEquals(Files.readAttributes(Paths.get(downloadOdo()), BasicFileAttributes.class).lastModifiedTime(),
				Files.readAttributes(Paths.get(downloadOdo()), BasicFileAttributes.class).lastModifiedTime());
	}

	private String downloadOdo() throws IOException {
		final String binaryPath = downloader.downloadOdoBinary();
		Assertions.assertTrue(Files.exists(Paths.get(binaryPath)));
		Assertions.assertTrue(Files.isExecutable(Paths.get(binaryPath)));
		return binaryPath;
	}
}
