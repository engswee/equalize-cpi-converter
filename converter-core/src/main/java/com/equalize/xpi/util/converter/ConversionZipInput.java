package com.equalize.xpi.util.converter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ConversionZipInput {
	private final ZipInputStream zis;
	private static final int DEF_BUFFER_SIZE = 8192;

	public ConversionZipInput(InputStream inStream) {
		this.zis = new ZipInputStream(inStream);
	}

	public ConversionZipInput(byte[] content) throws IOException {
		this.zis = new ZipInputStream(new ByteArrayInputStream(content));
	}

	public Map<String, byte[]> getEntriesContent() throws IOException {
		ZipEntry ze = null;
		Map<String, byte[]> map = new LinkedHashMap<String, byte[]>();

		// Loop through all entries in the zip file
		while ((ze = this.zis.getNextEntry()) != null) {
			byte[] zipContent = getInputStreamBytes(this.zis);
			map.put(ze.getName(), zipContent);
			this.zis.closeEntry();
		}
		this.zis.close();
		return map;
	}

	private byte[] getInputStreamBytes(InputStream inStream) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[DEF_BUFFER_SIZE];
		int read = 0;
		while ((read = inStream.read(buffer, 0, buffer.length)) != -1) {
			baos.write(buffer, 0, read);
		}
		baos.flush();
		return baos.toByteArray();
	}
}
