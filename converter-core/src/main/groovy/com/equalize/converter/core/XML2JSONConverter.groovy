package com.equalize.converter.core

import com.equalize.converter.core.util.AbstractConverter
import com.equalize.converter.core.util.ClassTypeConverter
import com.equalize.converter.core.util.ConversionDOMInput
import com.equalize.converter.core.util.ConversionJSONOutput
import com.equalize.converter.core.util.XMLElementContainer

class XML2JSONConverter extends AbstractConverter {
	int indentFactor
	boolean skipRootNode
	boolean forceArrayAll
	Set<String> arrayFields = []
	XMLElementContainer rootXML

	XML2JSONConverter(Object body, Map<String,Object> properties, ClassTypeConverter typeConverter) {
		super(body, properties, typeConverter)
	}

	@Override
	void retrieveParameters() {
		this.indentFactor = this.ph.retrievePropertyAsInt('indentFactor', '0')
		this.skipRootNode = this.ph.retrievePropertyAsBoolean('skipRootNode', 'N')
		this.forceArrayAll = this.ph.retrievePropertyAsBoolean('forceArrayAll', 'N')
		String arrayFieldList = this.ph.retrieveProperty('arrayFieldList', '')
		if(arrayFieldList && arrayFieldList.trim()) {
			arrayFieldList.split(',').each {
				this.arrayFields.add(it)
			}
		}
	}

	@Override
	void parseInput() {
		def is =  this.typeConverter.convertTo(InputStream, this.body)
		ConversionDOMInput domIn = new ConversionDOMInput(is)
		this.rootXML = domIn.extractDOMContent()
	}

	@Override
	Object generateOutput() {
		ConversionJSONOutput jsonOut = new ConversionJSONOutput()
		jsonOut.setArrayFields(this.arrayFields)
		jsonOut.setForceArray(this.forceArrayAll)
		jsonOut.generateJSONText(this.rootXML, this.skipRootNode, this.indentFactor)
	}
}