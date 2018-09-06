package com.equalize.converter.core.fcc

import com.equalize.converter.core.util.ConverterException
import com.equalize.converter.core.util.Field
import com.equalize.converter.core.util.PropertyHelper

class RecordTypeParametersPlain2XMLFixed extends RecordTypeParametersPlain2XML {

	RecordTypeParametersPlain2XMLFixed(String fieldSeparator, String[] fixedLengths) {
		super(fieldSeparator, fixedLengths)
	}

	void storeAdditionalParameters(String recordTypeName, String[] recordsetList, PropertyHelper param) {
		super.storeAdditionalParameters(recordTypeName, recordsetList, param)
		if (this.fieldNames.length != this.fixedLengths.length) {
			throw new ConverterException("No. of fields in 'fieldNames' and 'fieldFixedLengths' do not match for record type = '$recordTypeName'")
		}
		storeKeyFieldParameters(recordTypeName, param, false)
	}

	String parseKeyFieldValue(String lineInput) {
		String currentLineKeyFieldValue = null
		String valueAtKeyFieldPosition = dynamicSubstring(lineInput, this.keyFieldStartPosition, this.keyFieldLength)
		if (valueAtKeyFieldPosition.trim() == this.keyFieldValue) {
			currentLineKeyFieldValue = this.keyFieldValue
		}
		return currentLineKeyFieldValue
	}

	Field[] extractLineContents(String lineInput, boolean trim, int lineIndex) {
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

	private String dynamicSubstring(String input, int startPos, int length) {
		int endPos = startPos + length - 1
		String output = ''

		if (startPos < input.length()) {
			if (endPos < input.length()) {
				// Start & end positions are before end of input, return the
				// partial substring
				output = input.substring(startPos, endPos + 1)
			} else {
				// Start position is before start of input but end position
				// is after end of input, return from start till end of input
				output = input.substring(startPos, input.length())
			}
		} else {
			// Start position is after end of input, return empty string
		}
		return output
	}
}
