package com.equalize.converter.core.fcc

import com.equalize.converter.core.util.ConverterException
import com.equalize.converter.core.util.Field
import com.equalize.converter.core.util.PropertyHelper
import com.sap.aii.af.sdk.xi.adapter.MyStringTokenizer

class RecordTypeParametersPlain2XMLCSV extends RecordTypeParametersPlain2XML {
	boolean enclosureConversion
	String enclBegin
	String enclEnd
	String enclBeginEsc
	String enclEndEsc

	RecordTypeParametersPlain2XMLCSV(String fieldSeparator) {
		super(fieldSeparator, null)
	}

	@Override
	void storeAdditionalParameters(String recordTypeName, String[] recordsetList, PropertyHelper param) {
		super.storeAdditionalParameters(recordTypeName, recordsetList, param)
		storeKeyFieldParameters(recordTypeName, param, true)
		// Enclosure signs
		this.enclBegin = param.retrieveProperty("${recordTypeName}.enclosureSignBegin", '')
		this.enclEnd = param.retrieveProperty("${recordTypeName}.enclosureSignEnd", this.enclBegin)
		this.enclBeginEsc = param.retrieveProperty("${recordTypeName}.enclosureSignBeginEscape", '')
		this.enclEndEsc = param.retrieveProperty("${recordTypeName}.enclosureSignEndEscape", this.enclBeginEsc)
		this.enclosureConversion = param.retrievePropertyAsBoolean("${recordTypeName}.enclosureConversion", 'Y')
	}

	@Override
	String parseKeyFieldValue(String lineInput) {
		String[] inputFieldContents = splitLineBySeparator(lineInput)
		if ((this.keyFieldIndex < inputFieldContents.length) && inputFieldContents[this.keyFieldIndex] == this.keyFieldValue)
			return this.keyFieldValue
		else
			return null
	}

	@Override
	List<Field> extractLineContents(String lineInput, boolean trim, int lineIndex) {
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
			if (this.additionalLastFields.toLowerCase() == 'error') {
				throw new ConverterException("Line ${lineIndex+1} has more fields than configured")
			} else {
				// Default to length of configuration fields
				outputSize = this.fieldNames.length
			}
		}
		outputSize.times { index ->
			String content = (index < inputFieldContents.length) ? inputFieldContents[index] : ''
			fields << createNewField(this.fieldNames[index], content, trim)
		}
		return fields
	}

	private String[] splitLineBySeparator(String input) {
		// Split input with enclosure signs and escapes
		List<String> contents = new ArrayList<String>()
		MyStringTokenizer tokenizer = new MyStringTokenizer(input, this.fieldSeparator, this.enclBegin, this.enclEnd,
				this.enclBeginEsc, this.enclEndEsc, true)
		tokenizer.countTokens().times {
			String fieldContent = (String) tokenizer.nextElement()
			// If the token field content is not a separator, then store it in
			// the output array
			if (fieldContent.compareToIgnoreCase(this.fieldSeparator) != 0) {
				if (this.enclosureConversion)
					contents << tokenizer.convertEncls(fieldContent)
				else
					contents << fieldContent
			}
		}
		return contents as String[]
	}
}
