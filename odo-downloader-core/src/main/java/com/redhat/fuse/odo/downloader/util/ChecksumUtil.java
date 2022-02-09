package com.redhat.fuse.odo.downloader.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Scanner;
import java.util.stream.Collectors;

public final class ChecksumUtil {

	public static final String KV_SEPARATOR = "  ";

	private ChecksumUtil() {
	}

	public static String getChecksumFromChecksumsFile(final URL remoteUrl, final String fileName) throws IOException {
		try (ReadableByteChannel input = Channels.newChannel(remoteUrl.openStream())) {
			return getChecksumFromChecksumsFile(new Scanner(input), fileName);
		}
	}

	public static String calculateFileChecksum(final File filePath, final MessageDigest digest) throws IOException {
		final MessageDigest md;
		try (DigestInputStream dis = new DigestInputStream(new FileInputStream(filePath), digest);
			 ByteArrayOutputStream result = new ByteArrayOutputStream()) {
			dis.transferTo(result);
			md = dis.getMessageDigest();
		}
		return String.format("%032x", new BigInteger(1, md.digest()));
	}

	private static String getChecksumFromChecksumsFile(final Scanner scanner, final String fileName) {
		return scanner.useDelimiter(System.lineSeparator()).tokens()
				.collect(Collectors.toMap(p -> p.split(KV_SEPARATOR)[1], p -> p.split(KV_SEPARATOR)[0])).get(fileName);
	}
}
