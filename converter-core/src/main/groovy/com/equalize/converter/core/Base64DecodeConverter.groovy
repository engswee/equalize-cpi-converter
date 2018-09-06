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
	void retrieveParameters() {
		this.inputType = this.ph.retrieveProperty('inputType')
		this.ph.checkValidValues('inputType', this.inputType, ['plain', 'xml'] as Set)

		this.zippedContent = this.ph.retrievePropertyAsBoolean('zippedContent', 'N')
		if(this.inputType == 'xml')
			this.xpath = this.ph.retrieveProperty('xpath')
	}

	@Override
	void parseInput() {
		switch(this.inputType) {
			case 'plain':
				this.base64String = this.typeConverter.convertTo(String, this.body)
				break
			case 'xml':
				def is =  this.typeConverter.convertTo(InputStream, this.body)
				ConversionDOMInput domIn = new ConversionDOMInput(is)
				this.base64String = domIn.evaluateXPathToString(this.xpath)
				break
		}
	}

	@Override
	Object generateOutput() {
		ConversionBase64Decode decoder = new ConversionBase64Decode(this.base64String, this.zippedContent)
		decoder.decode()
	}
}