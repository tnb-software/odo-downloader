package software.tnb.odo.downloader.util;

import java.io.File;
import java.io.IOException;

public interface FileUnarchiver {
	String extension();
	File extract(File path, String entryName) throws IOException;
}
