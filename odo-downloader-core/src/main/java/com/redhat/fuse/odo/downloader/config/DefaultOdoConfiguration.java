package com.redhat.fuse.odo.downloader.config;

import java.nio.file.Paths;

public class DefaultOdoConfiguration implements OdoConfiguration {

	@Override
	public String clientArch() {
		return System.getProperty(ODO_CLIENT_ARCH, "amd64");
	}
	@Override
	public String clientOs() {
		return System.getProperty(ODO_CLIENT_OS, "linux");
	}
	@Override
	public String odoVersion() {
		return System.getProperty(ODO_VERSION, "latest");
	};
	@Override
	public String odoMirrorBaseUrl() {
		return System.getProperty(ODO_MIRROR_BASE_URL, "https://developers.redhat.com/content-gateway/rest/mirror/pub/openshift-v4/clients/odo");
	}
	@Override
	public String checksumFile() {
		return System.getProperty(ODO_CHECKSUM_FILE, "sha256sum.txt");
	}
	@Override
	public String odoTargetFile() {
		return System.getProperty(ODO_TARGET_FILE, Paths.get(System.getProperty("java.io.tmpdir", "."), "odo-binary", archiveEntryName()).toString());
	}
	@Override
	public String archiveExt() {
		return System.getProperty(ODO_ARCHIVE_EXT, clientOs().equalsIgnoreCase("windows") ? "zip" : "tar.gz");
	}
	@Override
	public String archiveEntryName() {
		return System.getProperty(ODO_ARCHIVE_ENTRY, clientOs().equalsIgnoreCase("windows") ? "odo.exe" : "odo");
	}
	@Override
	public boolean downloadArchive() {
		return Boolean.parseBoolean(System.getProperty(ODO_DOWNLOAD_ARCHIVE, "true"));
	}
}
