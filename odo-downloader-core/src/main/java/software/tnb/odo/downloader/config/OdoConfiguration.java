package software.tnb.odo.downloader.config;

public interface OdoConfiguration {

	String CONFIG_PREFIX = "odo.";
	String ODO_CLIENT_ARCH = CONFIG_PREFIX + "client.arch";
	String ODO_CLIENT_OS = CONFIG_PREFIX + "client.os";
	String ODO_VERSION = CONFIG_PREFIX + "version";
	String ODO_MIRROR_BASE_URL = CONFIG_PREFIX + "mirror.base.url";
	String ODO_CHECKSUM_FILE = CONFIG_PREFIX + "checksum.file";
	String ODO_TARGET_FILE = CONFIG_PREFIX + "target.file";
	String ODO_ARCHIVE_EXT = CONFIG_PREFIX + "archive.ext";
	String ODO_ARCHIVE_ENTRY = CONFIG_PREFIX + "archive.entry";
	String ODO_DOWNLOAD_ARCHIVE = CONFIG_PREFIX + "download.archive";
	String ODO_DOWNLOAD_FORCE = CONFIG_PREFIX + "download.force";

	String odoVersion();

	String clientArch();

	String clientOs();

	String odoMirrorBaseUrl();

	String checksumFile();

	String odoTargetFile();

	String archiveExt();

	String archiveEntryName();

	boolean downloadArchive();

	boolean forceDownload();
}
