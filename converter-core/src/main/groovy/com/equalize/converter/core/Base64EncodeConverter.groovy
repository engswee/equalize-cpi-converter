package com.equalize.converter.core

import com.equalize.converter.core.util.AbstractConverter
import com.equalize.converter.core.util.ClassTypeConverter
import com.equalize.converter.core.util.ConversionBase64Encode
import com.equalize.converter.core.util.ConversionDOMOutput
import com.equalize.converter.core.util.Field

class Base64EncodeConverter extends AbstractConverter {
	String outputType
	String documentName
	String documentNamespace
	String base64FieldName
	boolean compress
	byte[] content

	Base64EncodeConverter(Object body, Map<String,Object> properties, ClassTypeConverter typeConverter) {
		super(body, properties, typeConverter)
	}

	@Override
	void retrieveParameters() {
		this.outputType = this.ph.retrieveProperty('outputType')
		this.ph.checkValidValues('outputType', this.outputType, ['plain', 'xml'] as Set)

		this.compress = this.ph.retrievePropertyAsBoolean('compress', 'N')
		if(this.outputType == 'xml') {
			this.documentName = this.ph.retrieveProperty('documentName')
			this.documentNamespace = this.ph.retrieveProperty('documentNamespace')
			this.base64FieldName = this.ph.retrieveProperty('base64FieldName','base64Content')
		}
	}

	@Override
	void parseInput() {
		this.content = this.typeConverter.convertTo(byte[], this.body)
	}

	@Override
	Object generateOutput() {
		ConversionBase64Encode encoder = new ConversionBase64Encode(this.content)
		def base64String = encoder.encode(this.compress, 'Base64.txt')
		switch (this.outputType) {
			case 'xml':
				ConversionDOMOutput domOut = new ConversionDOMOutput(this.documentName, this.documentNamespace)
				List<Field> xmlContent = new ArrayList<Field>()
				xmlContent.add(new Field(this.base64FieldName, base64String))

				domOut.setIndentFactor(2)
				ByteArrayOutputStream baos = domOut.generateDOMOutput(xmlContent)
				return baos.toByteArray()
			case 'plain':
				return base64String.getBytes('UTF-8')
		}
	}
}