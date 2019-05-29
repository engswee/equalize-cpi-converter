package com.equalize.converter.core.fcc

import com.equalize.converter.core.util.PropertyHelper

class RecordTypeParametersXML2PlainCSV extends RecordTypeParametersXML2Plain {

	RecordTypeParametersXML2PlainCSV(String fieldSeparator) {
		super(fieldSeparator, null)
	}

	@Override
	void storeAdditionalParameters(String recordTypeName, PropertyHelper param, String encoding) {
		super.storeAdditionalParameters(recordTypeName, param, encoding)
		// Enclosure sign handling
		this.enclosureSign = param.retrieveProperty('defaultEnclosureSign', '')
		this.enclosureSignEscape = param.retrieveProperty('defaultEnclosureSignEscape', '')
	}
}
