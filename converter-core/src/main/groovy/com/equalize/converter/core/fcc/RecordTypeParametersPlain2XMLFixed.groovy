package com.equalize.converter.core.fcc

import com.equalize.converter.core.util.ConverterException
import com.equalize.converter.core.util.Field
import com.equalize.converter.core.util.PropertyHelper

class RecordTypeParametersPlain2XMLFixed extends RecordTypeParametersPlain2XML {

	RecordTypeParametersPlain2XMLFixed(String fieldSeparator, String[] fixedLengths) {
		super(fieldSeparator, fixedLengths)
	}

	void setAdditionalParameters(String recordTypeName, String[] recordsetList, PropertyHelper param) throws ConverterException {
		super.setAdditionalParameters(recordTypeName, recordsetList, param)
		if (this.fieldNames.length != this.fixedLengths.length) {
			throw new ConverterException("No. of fields in 'fieldNames' and 'fieldFixedLengths' do not match for record type = '$recordTypeName'")
		}
		setKeyFieldParameters(recordTypeName, param, false)
	}

	String parseKeyFieldValue(String lineInput) {
		String currentLineKeyFieldValue = null
		String valueAtKeyFieldPosition = dynamicSubstring(lineInput, this.keyFieldStartPosition, this.keyFieldLength)
		if (valueAtKeyFieldPosition.trim() == this.keyFieldValue) {
			currentLineKeyFieldValue = this.keyFieldValue
		}
		return currentLineKeyFieldValue
	}

	Field[] extractLineContents(String lineInput, boolean trim, int lineIndex) throws ConverterException {
		List<Field> fields = new ArrayList<Field>()
		int start = 0
		for (int i = 0; i < this.fieldNames.length; i++) {
			int length = Integer.parseInt(this.fixedLengths[i])
			String content = dynamicSubstring(lineInput, start, length)

			if (lineInput.length() < start) {
				if (this.missingLastFields.toLowerCase() == 'error') {
					throw new ConverterException("Line ${lineIndex+1} has less fields than configured")
				} else if (this.missingLastFields.toLowerCase() == 'add') {
					fields.add(createNewField(this.fieldNames[i], content, trim))
				}
			} else {
				fields.add(createNewField(this.fieldNames[i], content, trim))
			}
			// Set start location for next field
			start += length

			// After the last configured field, check if there are any more
			// content in the input
			if (i == this.fieldNames.length - 1 && lineInput.length() > start
			&& this.additionalLastFields.toLowerCase() == 'error') {
				throw new ConverterException("Line ${lineIndex+1} has more fields than configured")
			}
		}
		return fields.toArray(new Field[fields.size()])
	}

	private String dynamicSubstring(String input, int start, int length) {
		int startPos = start
		int endPos = start + length - 1
		String output = ''

		if (startPos < 0) {
			// (1) Start position is before start of input, return empty string
		} else if (startPos >= 0 && startPos < input.length()) {
			if (endPos < input.length()) {
				// (2) Start & end positions are before end of input, return the
				// partial substring
				output = input.substring(startPos, endPos + 1)
			} else if (endPos >= input.length()) {
				// (3) Start position is before start of input but end position
				// is after end of input, return from start till end of input
				output = input.substring(startPos, input.length())
			}
		} else if (startPos >= input.length()) {
			// (4) Start position is after end of input, return empty string
		}
		return output
	}
}
