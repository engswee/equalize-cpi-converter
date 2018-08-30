package com.equalize.converter.core

import com.equalize.converter.core.util.AbstractConverter
import com.equalize.converter.core.util.ClassTypeConverter
import com.equalize.converter.core.util.ConversionBase64Decode
import com.equalize.converter.core.util.ConversionDOMInput

class Base64DecodeConverter extends AbstractConverter {
	String inputType
	String xpath
	String contentType
	boolean zippedContent
	String base64String

	Base64DecodeConverter(Object body, Map<String,Object> properties, ClassTypeConverter typeConverter) {
		super(body, properties, typeConverter)
	}

	@Override
	void getParameters() {
		this.inputType = this.ph.getProperty('inputType')
		if(this.inputType)
			this.ph.checkValidValues('inputType', this.inputType, ['plain', 'xml'] as Set)

		this.zippedContent = this.ph.getPropertyAsBoolean('zippedContent', 'N')
		if(this.inputType == 'xml')
			this.xpath = this.ph.getProperty('xpath')
	}

	@Override
	void parseInput() {
		if(this.inputType == 'plain') {
			this.base64String = this.typeConverter.convertTo(String, this.body)
		} else if(this.inputType == 'xml') {
			def is =  this.typeConverter.convertTo(InputStream, this.body)
			ConversionDOMInput domIn = new ConversionDOMInput(is)
			this.base64String = domIn.evaluateXPathToString(this.xpath)
		}
	}

	@Override
	Object generateOutput() {
		ConversionBase64Decode decoder = new ConversionBase64Decode(this.base64String, this.zippedContent)
		byte[] content = decoder.decode()
	}
}