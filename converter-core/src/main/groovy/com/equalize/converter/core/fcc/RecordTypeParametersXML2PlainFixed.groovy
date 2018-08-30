package com.equalize.converter.core.fcc

import com.equalize.converter.core.util.ConverterException
import com.equalize.converter.core.util.PropertyHelper

class RecordTypeParametersXML2PlainFixed extends RecordTypeParametersXML2Plain {

	RecordTypeParametersXML2PlainFixed(String fieldSeparator, String[] fixedLengths) {
		super(fieldSeparator, fixedLengths)
	}

	void setAdditionalParameters(String recordTypeName, PropertyHelper param, String encoding) throws ConverterException {
		super.setAdditionalParameters(recordTypeName, param, encoding)
		// Fixed Length too short handling
		this.fixedLengthTooShortHandling = param.getProperty(recordTypeName + ".fixedLengthTooShortHandling", "error")
		param.checkValidValues(recordTypeName + ".fixedLengthTooShortHandling", this.fixedLengthTooShortHandling.toLowerCase(), ['error', 'cut', 'ignore'] as Set)
	}
}