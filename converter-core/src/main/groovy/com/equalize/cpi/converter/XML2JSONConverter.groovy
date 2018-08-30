package com.equalize.cpi.converter

import com.equalize.cpi.converter.util.AbstractConverter
import com.equalize.cpi.converter.util.ClassTypeConverter
import com.equalize.xpi.util.converter.ConversionDOMInput
import com.equalize.xpi.util.converter.ConversionJSONOutput
import com.equalize.xpi.util.converter.XMLElementContainer

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
	void getParameters() {
		this.indentFactor = this.ph.getPropertyAsInt('indentFactor', '0')
		this.skipRootNode = this.ph.getPropertyAsBoolean('skipRootNode', 'N')
		this.forceArrayAll = this.ph.getPropertyAsBoolean('forceArrayAll', 'N')
		String arrayFieldList = this.ph.getProperty('arrayFieldList', '')
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