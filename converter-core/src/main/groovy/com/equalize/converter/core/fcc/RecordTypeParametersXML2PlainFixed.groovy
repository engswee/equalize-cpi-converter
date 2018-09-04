package com.equalize.converter.core.fcc

import com.equalize.converter.core.util.ConverterException
import com.equalize.converter.core.util.PropertyHelper

class RecordTypeParametersXML2PlainFixed extends RecordTypeParametersXML2Plain {

	RecordTypeParametersXML2PlainFixed(String[] fixedLengths) {
		super(null, fixedLengths)
	}

	@Override
	void storeAdditionalParameters(String recordTypeName, PropertyHelper param, String encoding) {
		super.storeAdditionalParameters(recordTypeName, param, encoding)
		// Fixed Length too short handling
		this.fixedLengthTooShortHandling = param.retrieveProperty("${recordTypeName}.fixedLengthTooShortHandling", "error")
		param.checkValidValues("${recordTypeName}.fixedLengthTooShortHandling", this.fixedLengthTooShortHandling.toLowerCase(), ['error', 'cut', 'ignore'] as Set)
	}
}
