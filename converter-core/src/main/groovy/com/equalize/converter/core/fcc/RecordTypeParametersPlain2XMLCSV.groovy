package com.equalize.converter.core.fcc

import com.equalize.converter.core.util.ConverterException
import com.equalize.converter.core.util.Field
import com.equalize.converter.core.util.MyStringTokenizer
import com.equalize.converter.core.util.PropertyHelper

class RecordTypeParametersPlain2XMLCSV extends RecordTypeParametersPlain2XML {
	boolean enclosureConversion
	String enclBegin
	String enclEnd
	String enclBeginEsc
	String enclEndEsc

	RecordTypeParametersPlain2XMLCSV(String fieldSeparator, String[] fixedLengths) {
		super(fieldSeparator, fixedLengths)
	}

	void setAdditionalParameters(String recordTypeName, String[] recordsetList, PropertyHelper param) throws ConverterException {
		super.setAdditionalParameters(recordTypeName, recordsetList, param)
		setKeyFieldParameters(recordTypeName, param, true)
		// Enclosure signs
		this.enclBegin = param.getProperty("${recordTypeName}.enclosureSignBegin", '')
		this.enclEnd = param.getProperty("${recordTypeName}.enclosureSignEnd", this.enclBegin)
		this.enclBeginEsc = param.getProperty("${recordTypeName}.enclosureSignBeginEscape", '')
		this.enclEndEsc = param.getProperty("${recordTypeName}.enclosureSignEndEscape", this.enclBeginEsc)
		this.enclosureConversion = param.getPropertyAsBoolean("${recordTypeName}.enclosureConversion", 'Y')
	}

	String parseKeyFieldValue(String lineInput) {
		String currentLineKeyFieldValue = null
		String[] inputFieldContents = splitLineBySeparator(lineInput)
		if (this.keyFieldIndex <= inputFieldContents.length) {
			if (inputFieldContents[this.keyFieldIndex] == this.keyFieldValue) {
				currentLineKeyFieldValue = this.keyFieldValue
			}
		}
		return currentLineKeyFieldValue
	}

	Field[] extractLineContents(String lineInput, boolean trim, int lineIndex) throws ConverterException {
		List<Field> fields = new ArrayList<Field>()

		String[] inputFieldContents = splitLineBySeparator(lineInput)
		int outputSize = inputFieldContents.length // Use length of input line for default 'ignore' or anything else
		// Content has less fields than specified in configuration
		if (inputFieldContents.length < this.fieldNames.length) {
			if (this.missingLastFields.toLowerCase() == 'add') {
				outputSize = this.fieldNames.length
			} else if (this.missingLastFields.toLowerCase() == 'error') {
				throw new ConverterException("Line ${lineIndex+1} has less fields than configured")
			}
			// Content has more fields than specified in configuration
		} else if (inputFieldContents.length > this.fieldNames.length) {
			outputSize = this.fieldNames.length // Default to length of configuration fields
			if (this.additionalLastFields.toLowerCase() == 'error') {
				throw new ConverterException("Line ${lineIndex+1}  has more fields than configured")
			}
		}
		for (int i = 0; i < outputSize; i++) {
			String content = ''
			if (i < inputFieldContents.length) {
				content = (inputFieldContents[i] == null) ? '' : inputFieldContents[i]
			}
			fields.add(createNewField(this.fieldNames[i], content, trim))
		}
		return fields.toArray(new Field[fields.size()])
	}

	private String[] splitLineBySeparator(String input) {
		// Split input with enclosure signs and escapes
		List<String> contents = new ArrayList<String>()
		MyStringTokenizer tokenizer = new MyStringTokenizer(input, this.fieldSeparator, this.enclBegin, this.enclEnd,
				this.enclBeginEsc, this.enclEndEsc, true)
		for (int i = 0; i < tokenizer.countTokens(); i++) {
			String fieldContent = (String) tokenizer.nextElement()
			// If the token field content is not a separator, then store it in
			// the output array
			if (fieldContent.compareToIgnoreCase(this.fieldSeparator) != 0) {
				if (this.enclosureConversion) {
					contents.add(tokenizer.convertEncls(fieldContent))
				} else {
					contents.add(fieldContent)
				}
			}
		}
		return contents.toArray(new String[contents.size()])
	}
}
