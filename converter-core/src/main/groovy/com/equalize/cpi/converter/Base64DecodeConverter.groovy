package com.equalize.cpi.converter

import org.apache.camel.Exchange

import com.equalize.xpi.util.converter.ConversionBase64Decode
import com.equalize.xpi.util.converter.ConversionDOMInput

import com.equalize.cpi.converter.util.AbstractConverter

class Base64DecodeConverter extends AbstractConverter {
	String inputType
	String xpath
	String contentType
	boolean zippedContent
	String base64String

	Base64DecodeConverter(Exchange exchange, Map<String,Object> properties) {
		super(exchange, properties)
	}

	@Override
	void getParameters() {
		this.inputType = this.ph.getProperty('inputType')
		if(this.inputType)
			this.ph.checkValidValues('inputType', this.inputType, ['plain','xml'] as Set)

		this.zippedContent = this.ph.getPropertyAsBoolean('zippedContent', 'N')
		if(this.inputType == 'xml')
			this.xpath = this.ph.getProperty('xpath')
	}

	@Override
	void parseInput() {
		if(this.inputType == 'plain') {
			this.base64String = this.tch.convertTo(String, this.exchange.getIn().getBody())
			//def is =  this.tch.convertTo(InputStream, this.exchange.getIn().getBody())
			//this.base64String = Converter.toString(is)
		} else if(this.inputType == 'xml') {
			def is =  this.tch.convertTo(InputStream, this.exchange.getIn().getBody())
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