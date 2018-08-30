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
	void getParameters() {
		this.documentName = this.ph.getProperty('documentName')
		this.documentNamespace = this.ph.getProperty('documentNamespace')
		this.indentFactor = this.ph.getPropertyAsInt('indentFactor', '0')
		this.escapeInvalidNameStartChar = this.ph.getPropertyAsBoolean('escapeInvalidNameStartChar', 'N')
		this.mangleInvalidNameChar = this.ph.getPropertyAsBoolean('mangleInvalidNameChar', 'N')
		this.allowArrayAtTop = this.ph.getPropertyAsBoolean('allowArrayAtTop', 'N')
		if(this.allowArrayAtTop)
			this.topArrayName = this.ph.getProperty('topArrayName')
	}

	@Override
	void parseInput() {
		def input =  this.typeConverter.convertTo(String, this.body)
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