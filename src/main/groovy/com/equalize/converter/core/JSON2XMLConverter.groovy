package com.equalize.converter.core

import com.equalize.converter.core.util.AbstractConverter
import com.equalize.converter.core.util.ClassTypeConverter
import com.equalize.converter.core.util.ConversionDOMOutput
import com.equalize.converter.core.util.ConversionJSONInput
import com.equalize.converter.core.util.Field

class JSON2XMLConverter extends AbstractConverter {
	String documentName
	String documentNamespace
	int indentFactor
	boolean escapeInvalidNameStartChar
	boolean mangleInvalidNameChar
	boolean allowArrayAtTop
	String topArrayName
	List<Field> inputContents

	JSON2XMLConverter(Object body, Map<String,Object> properties, ClassTypeConverter typeConverter) {
		super(body, properties, typeConverter)
	}

	@Override
	void retrieveParameters() {
		this.documentName = this.ph.retrieveProperty('documentName')
		this.documentNamespace = this.ph.retrieveProperty('documentNamespace')
		this.indentFactor = this.ph.retrievePropertyAsInt('indentFactor', '0')
		this.escapeInvalidNameStartChar = this.ph.retrievePropertyAsBoolean('escapeInvalidNameStartChar', 'N')
		this.mangleInvalidNameChar = this.ph.retrievePropertyAsBoolean('mangleInvalidNameChar', 'N')
		this.allowArrayAtTop = this.ph.retrievePropertyAsBoolean('allowArrayAtTop', 'N')
		if(this.allowArrayAtTop)
			this.topArrayName = this.ph.retrieveProperty('topArrayName')
	}

	@Override
	void parseInput() {
		def input =  this.typeConverter.convertTo(Reader, this.body)
		ConversionJSONInput jsonIn
		if(this.allowArrayAtTop)
			jsonIn = new ConversionJSONInput(input, this.topArrayName)
		else
			jsonIn = new ConversionJSONInput(input)
		this.inputContents = jsonIn.extractJSONContent()
	}

	@Override
	Object generateOutput() {
		ConversionDOMOutput domOut = new ConversionDOMOutput(this.documentName, this.documentNamespace)
		if(this.indentFactor)
			domOut.setIndentFactor(this.indentFactor)
		domOut.setEscapeInvalidNameStartChar(this.escapeInvalidNameStartChar)
		domOut.setMangleInvalidNameChar(this.mangleInvalidNameChar)
		domOut.generateDOMOutput(this.inputContents).toByteArray()
	}
}