package com.equalize.converter.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class ConversionPlainInput {
	private final List<String> lineContents;

	public ConversionPlainInput(InputStream inStream, String endSeparator, String encoding) {
		List<String> contents = new ArrayList<>();
		Scanner scanner = new Scanner(inStream, encoding);
		scanner.useDelimiter(endSeparator);
		while (scanner.hasNext()) {
			String lineContent = scanner.next();
			contents.add(lineContent);
		}
		scanner.close();
		this.lineContents = contents;
	}

	public ConversionPlainInput(InputStream inStream, String encoding) throws IOException {
		List<String> contents = new ArrayList<>();
		LineNumberReader lnr = new LineNumberReader(new InputStreamReader(inStream, encoding));
		String lineContent;
		while ((lineContent = lnr.readLine()) != null) {
			contents.add(lineContent);
		}
		lnr.close();
		this.lineContents = contents;
	}

	public ConversionPlainInput(String input) {
		String[] lines = input.split("\r\n|\r|\n");
		this.lineContents = Arrays.asList(lines);
	}

	public List<String> getLineContents() {
		return this.lineContents;
	}
}
