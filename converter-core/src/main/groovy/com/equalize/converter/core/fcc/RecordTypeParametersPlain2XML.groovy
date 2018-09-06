package com.equalize.converter.core.fcc

import com.equalize.converter.core.util.ConverterException
import com.equalize.converter.core.util.Field
import com.equalize.converter.core.util.PropertyHelper

abstract class RecordTypeParametersPlain2XML {
	public final String fieldSeparator
	public final String[] fixedLengths
	public String endSeparator
	// Plain to XML
	protected String keyFieldValue
	protected String[] fieldNames
	protected int keyFieldIndex
	protected int keyFieldStartPosition = 0
	protected int keyFieldLength = 0

	protected String missingLastFields
	protected String additionalLastFields
	public String parentRecordType

	RecordTypeParametersPlain2XML(String fieldSeparator, String[] fixedLengths) {
		this.fieldSeparator = fieldSeparator
		this.fixedLengths = fixedLengths
	}

	void storeAdditionalParameters(String recordTypeName, String[] recordsetList, PropertyHelper param) {
		// Parent record type
		this.parentRecordType = param.retrieveProperty("${recordTypeName}.parent")
		if (this.parentRecordType == recordTypeName) {
			throw new ConverterException("Value in '${recordTypeName}.parent' cannot be the same as substructure name")
		} else if (this.parentRecordType != 'Root') {
			boolean found = false
			recordsetList.each {
				if (!found && this.parentRecordType == it) {
					found = true
				}
			}
			if (!found) {
				throw new ConverterException("Value '${this.parentRecordType}' in '${recordTypeName}.parent' not found in parameter 'recordsetStructure'")
			}
		}

		// Field names
		String fieldNamesColumn = recordTypeName + '.fieldNames'
		String tempFieldNames = param.retrieveProperty(fieldNamesColumn)
		this.fieldNames = tempFieldNames.split(',')
		// Validate the field names
		validateFieldNames(recordTypeName, this.fieldNames)
		// Structure deviations
		this.missingLastFields = param.retrieveProperty("${recordTypeName}.missingLastFields", 'ignore')
		this.additionalLastFields = param.retrieveProperty("${recordTypeName}.additionalLastFields", 'ignore')
	}

	abstract String parseKeyFieldValue(String lineInput)

	abstract Field[] extractLineContents(String lineInput, boolean trim, int lineIndex)

	protected Field createNewField(String fieldName, String fieldValue, boolean trim) {
		if (trim) {
			fieldValue = fieldValue.trim()
		}
		return new Field(fieldName, fieldValue)
	}

	protected void storeKeyFieldParameters(String recordTypeName, PropertyHelper param, boolean csvMode) {
		String genericRecordType = param.retrieveProperty('genericRecordType', '')
		if (!genericRecordType || genericRecordType != recordTypeName) {
			// Key field name and value
			String keyFieldName = param.retrieveProperty('keyFieldName')
			this.keyFieldValue = param.retrieveProperty("${recordTypeName}.keyFieldValue")

			// Index and position of key field in record type
			boolean found = false
			for (int i = 0; i < this.fieldNames.length; i++) {
				if (this.fieldNames[i] == keyFieldName) {
					this.keyFieldIndex = i
					found = true
					if (!csvMode) {
						this.keyFieldLength = Integer.parseInt(this.fixedLengths[i])
					}
					break
				}
				if (!csvMode) {
					this.keyFieldStartPosition += Integer.parseInt(this.fixedLengths[i])
				}
			}
			if (!found) {
				throw new ConverterException("Key field '$keyFieldName' not found in '${recordTypeName}.fieldNames'")
			}
		}
	}

	private void validateFieldNames(String recordTypeName, String[] fieldNames) {
		// No duplicates in field names
		Set<String> set = new HashSet<String>()
		fieldNames.each { field ->
			if (set.contains(field))
				throw new ConverterException("Duplicate field found in '${recordTypeName}.fieldNames': $field")
			else
				set.add(field)
		}

	}
}
