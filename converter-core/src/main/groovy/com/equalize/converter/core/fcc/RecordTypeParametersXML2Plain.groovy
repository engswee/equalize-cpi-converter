package com.equalize.converter.core.fcc

import com.equalize.converter.core.util.ConverterException
import com.equalize.converter.core.util.PropertyHelper
import com.sap.aii.af.sdk.xi.adapter.trans.Separator

abstract class RecordTypeParametersXML2Plain {
	public final String fieldSeparator
	public final String[] fixedLengths
	public String endSeparator
	// XML to Plain
	public String fixedLengthTooShortHandling

	public RecordTypeParametersXML2Plain(String fieldSeparator, String[] fixedLengths) {
		this.fieldSeparator = fieldSeparator
		this.fixedLengths = fixedLengths
	}

	void storeAdditionalParameters(String recordTypeName, PropertyHelper param, String encoding) {
		// End Separator
		String tempEndSeparator = param.retrieveProperty("${recordTypeName}.endSeparator", '')
		if (!tempEndSeparator) {
			this.endSeparator = Separator.newLine
		} else {
			Separator sep = new Separator(tempEndSeparator, encoding)
			this.endSeparator = sep.toString()
		}
	}
}
