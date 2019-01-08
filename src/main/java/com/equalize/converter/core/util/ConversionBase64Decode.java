package com.equalize.converter.core.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.bind.DatatypeConverter;

public class ConversionBase64Decode {
	private final String base64String;
	private final boolean zippedContent;
	private static final int DEF_BUFFER_SIZE = 8192;

	public ConversionBase64Decode(String base64String) {
		this(base64String, false);
	}

	public ConversionBase64Decode(String base64String, boolean zippedContent) {
		this.base64String = base64String;
		this.zippedContent = zippedContent;
	}

	public byte[] decode() throws IOException, ConverterException {
		byte[] decoded = DatatypeConverter.parseBase64Binary(this.base64String);

		if (!this.zippedContent) {
			return decoded;
		} else {
			// Unzip the contents, assumption is only 1 zip entry in the zip
			// content
			ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(decoded));
			ZipEntry ze = zis.getNextEntry();
			// Check if there is a zip entry
			if (ze == null) {
				throw new ConverterException("Unable to decompress as content is not zipped");
			}
			byte[] content = getInputStreamBytes(zis);
			zis.closeEntry();
			zis.close();
			return content;
		}
	}

	private byte[] getInputStreamBytes(InputStream inStream) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[DEF_BUFFER_SIZE];
		int read;
		while ((read = inStream.read(buffer, 0, buffer.length)) != -1) {
			baos.write(buffer, 0, read);
		}
		baos.flush();
		return baos.toByteArray();
	}
}
