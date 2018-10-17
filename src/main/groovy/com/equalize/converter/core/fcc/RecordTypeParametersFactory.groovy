package com.equalize.converter.core.fcc

import com.equalize.converter.core.util.ConverterException
import com.equalize.converter.core.util.PropertyHelper
import com.sap.aii.af.sdk.xi.adapter.trans.Separator

class RecordTypeParametersFactory {

	// Private constructor
	private RecordTypeParametersFactory() {
	}

	static RecordTypeParametersFactory newInstance() {
		return new RecordTypeParametersFactory()
	}

	Object newParameter(String recordTypeName, String[] recordsetList, String encoding, PropertyHelper param, String convType) {
		// Set parameter values for the record type
		// 1 - Field Separator
		String defaultFieldSeparator = param.retrieveProperty('defaultFieldSeparator', '')
		String fieldSeparatorName = "${recordTypeName}.fieldSeparator"
		String fieldSeparator = param.retrieveProperty(fieldSeparatorName, defaultFieldSeparator)
		if (fieldSeparator) {
			Separator sep = new Separator(fieldSeparator, encoding)
			fieldSeparator = sep.toString()
		}

		// 2 - Fixed Lengths
		String fieldFixedLengthsName = "${recordTypeName}.fieldFixedLengths"
		String tempFixedLengths = param.retrieveProperty(fieldFixedLengthsName, '')
		String[] fixedLengths
		if (!tempFixedLengths) {
			fixedLengths = null
		} else {
			String lengthsWithoutComma = tempFixedLengths.replaceAll(',', '')
			if (!containsOnlyDigits(lengthsWithoutComma))
				throw new ConverterException("Maintain only integers separated by commas for '$fieldFixedLengthsName'")
			fixedLengths = tempFixedLengths.split(',')
		}

		// Validate the parameter values
		if (!fieldSeparator && !fixedLengths) {
			throw new ConverterException("'defaultFieldSeparator', '$fieldSeparatorName' or '$fieldFixedLengthsName' must be populated")
		} else if (fieldSeparator && fixedLengths) {
			throw new ConverterException("Use only parameter '$fieldSeparatorName'/'defaultFieldSeparator' or '$fieldFixedLengthsName', not both")
		}

		switch (convType) {
			case 'xml2plain':
				if (fieldSeparator)
					return new RecordTypeParametersXML2PlainCSV(fieldSeparator)
				else
					return new RecordTypeParametersXML2PlainFixed(fixedLengths)
			case 'plain2xml':
				if (fieldSeparator)
					return new RecordTypeParametersPlain2XMLCSV(fieldSeparator)
				else
					return new RecordTypeParametersPlain2XMLFixed(fixedLengths)
		}
	}

	private boolean containsOnlyDigits(String input) {
		return input.toCharArray().every { Character.isDigit(it) }
	}
}
