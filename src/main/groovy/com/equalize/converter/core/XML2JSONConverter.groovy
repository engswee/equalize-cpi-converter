package com.equalize.converter.core

import com.equalize.converter.core.util.AbstractConverter
import com.equalize.converter.core.util.ClassTypeConverter
import com.equalize.converter.core.util.ConversionDOMInput
import com.equalize.converter.core.util.ConversionJSONOutput
import com.equalize.converter.core.util.ConversionSAXInput
import com.equalize.converter.core.util.XMLElementContainer

class XML2JSONConverter extends AbstractConverter {
	int indentFactor
	boolean skipRootNode
	boolean forceArrayAll
	boolean useDOM
	Set<String> arrayFields = []
	Map<String, String> fieldConversions = [:]
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
		if(arrayFieldList && arrayFieldList.trim())
			this.arrayFields = arrayFieldList.split(',') as Set
		this.useDOM = this.ph.retrievePropertyAsBoolean('useDOM', 'N')
		String fieldConversionsRaw = this.ph.retrieveProperty('fieldConversions', '')
		if (fieldConversionsRaw) {
			fieldConversionsRaw.split(',').each {
				String[] pairs = it.split(':')
				this.fieldConversions.put(pairs[0], pairs[1])
			}
		}
	}

	@Override
	void parseInput() {
		if (this.useDOM) {
			def is =  this.typeConverter.convertTo(InputStream, this.body)
			ConversionDOMInput domIn = new ConversionDOMInput(is)
			this.rootXML = domIn.extractDOMContent()
		} else {
			def reader =  this.typeConverter.convertTo(Reader, this.body)
			ConversionSAXInput saxIn = new ConversionSAXInput(reader)
			this.rootXML = saxIn.extractXMLContent()
		}
	}

	@Override
	Object generateOutput() {
		ConversionJSONOutput jsonOut = new ConversionJSONOutput()
		jsonOut.setArrayFields(this.arrayFields)
		jsonOut.setForceArray(this.forceArrayAll)
		jsonOut.setFieldConversions(this.fieldConversions)
		jsonOut.generateJSONText(this.rootXML, this.skipRootNode, this.indentFactor)
	}
}