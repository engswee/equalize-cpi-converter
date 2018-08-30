package com.equalize.converter.core.fcc

import com.equalize.converter.core.util.ConverterException
import com.equalize.converter.core.util.PropertyHelper
import com.equalize.converter.core.util.Separator

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

	void setAdditionalParameters(String recordTypeName, PropertyHelper param, String encoding) throws ConverterException {
		// End Separator
		String tempEndSeparator = param.getProperty("${recordTypeName}.endSeparator", '')
		if (!tempEndSeparator) {
			tempEndSeparator = Separator.newLine
		} else {
			Separator sep = new Separator(tempEndSeparator, encoding)
			tempEndSeparator = sep.toString()
		}
		this.endSeparator = tempEndSeparator
	}
}
