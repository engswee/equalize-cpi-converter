package com.equalize.cpi.converter

import org.apache.camel.Exchange

import com.equalize.xpi.util.converter.ConversionBase64Encode
import com.equalize.xpi.util.converter.ConversionDOMOutput

import com.equalize.cpi.converter.util.AbstractConverter

import com.equalize.xpi.util.converter.Field

class Base64EncodeConverter extends AbstractConverter {
	String outputType
	String documentName
	String documentNamespace
	String base64FieldName
	boolean compress
	byte[] content

	Base64EncodeConverter(Exchange exchange, Map<String,Object> properties) {
		super(exchange, properties)
	}

	@Override
	void getParameters() {
		this.outputType = this.ph.getProperty('outputType')
		if(this.outputType)
			this.ph.checkValidValues('outputType', this.outputType, ['plain','xml'] as Set)

		this.compress = this.ph.getPropertyAsBoolean('compress', 'N')
		if(this.outputType == 'xml') {
			this.documentName = this.ph.getProperty('documentName')
			this.documentNamespace = this.ph.getProperty('documentNamespace')
			this.base64FieldName = this.ph.getProperty('base64FieldName','base64Content')
		}
	}

	@Override
	void parseInput() {
		this.content = this.tch.convertTo(byte[], this.exchange.getIn().getBody())
	}

	@Override
	Object generateOutput() {
		ConversionBase64Encode encoder = new ConversionBase64Encode(this.content)
		def base64String = encoder.encode(this.compress, 'Base64.txt')
		if(this.outputType == 'xml') {
			ConversionDOMOutput domOut = new ConversionDOMOutput(this.documentName, this.documentNamespace)
			List<Field> xmlContent = new ArrayList<Field>()
			xmlContent.add(new Field(this.base64FieldName, base64String))

			domOut.setIndentFactor(2)
			ByteArrayOutputStream baos = domOut.generateDOMOutput(xmlContent)
			baos.toByteArray()
		} else {
			base64String.getBytes('UTF-8')
		}
	}
}