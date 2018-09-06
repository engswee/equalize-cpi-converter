package com.equalize.converter.core

import com.equalize.converter.core.fcc.RecordTypeParametersFactory
import com.equalize.converter.core.fcc.RecordTypeParametersXML2Plain
import com.equalize.converter.core.util.AbstractConverter
import com.equalize.converter.core.util.ClassTypeConverter
import com.equalize.converter.core.util.ConversionDOMInput
import com.equalize.converter.core.util.ConversionPlainOutput
import com.equalize.converter.core.util.ConverterException
import com.equalize.converter.core.util.Field
import com.equalize.converter.core.util.XMLElementContainer

class XML2DeepPlainConverter extends AbstractConverter {
	ConversionDOMInput domIn
	ConversionPlainOutput plainOut
	XMLElementContainer rootXML
	String encoding
	String recordsetStructure
	final Map<String, RecordTypeParametersXML2Plain> recordTypes

	XML2DeepPlainConverter(Object body, Map<String,Object> properties, ClassTypeConverter typeConverter) {
		super(body, properties, typeConverter)
		this.recordTypes = new HashMap<String, RecordTypeParametersXML2Plain>()
	}

	@Override
	void retrieveParameters(){
		this.encoding = this.ph.retrieveProperty('encoding', 'UTF-8')
		this.recordsetStructure = this.ph.retrieveProperty('recordsetStructure')

		String[] recordsetList = this.recordsetStructure.split(',')
		recordsetList.each { recordTypeName ->
			if (!this.recordTypes.containsKey(recordTypeName)) {
				RecordTypeParametersXML2Plain rtp = (RecordTypeParametersXML2Plain) RecordTypeParametersFactory
						.newInstance()
						.newParameter(recordTypeName, recordsetList, this.encoding, this.ph, 'xml2plain')
				rtp.storeAdditionalParameters(recordTypeName, this.ph, this.encoding)
				this.recordTypes.put(recordTypeName, rtp)
			} else {
				throw new ConverterException("Duplicate field found in 'recordsetStructure': $recordTypeName")
			}
		}
	}

	@Override
	void parseInput() {
		// Parse input XML contents
		def is =  this.typeConverter.convertTo(InputStream, this.body)
		this.domIn = new ConversionDOMInput(is)
		this.rootXML = this.domIn.extractDOMContent()
	}

	@Override
	Object generateOutput() {
		// Create output converter and generate output flat content
		this.plainOut = new ConversionPlainOutput()

		constructTextfromXML(this.rootXML, true).getBytes(encoding)
	}

	private String constructTextfromXML(XMLElementContainer element, boolean isRoot) {
		StringBuilder sb = new StringBuilder()
		// First, construct output for current element's child fields
		if (!isRoot) {
			sb.append(generateRowTextForElement(element))
		}
		// Then recursively process child elements that are segments
		for (Field childField : element.getChildFields()) {
			Object fieldContent = childField.fieldContent
			if (fieldContent instanceof XMLElementContainer) {
				sb.append(constructTextfromXML((XMLElementContainer) fieldContent, false))
			}
		}
		return sb.toString()
	}

	private String generateRowTextForElement(XMLElementContainer element) {

		List<Field> childFields = element.getChildFields()
		String segmentName = element.getElementName()
		if (!this.recordTypes.containsKey(segmentName)) {
			throw new ConverterException("Record Type $segmentName not listed in parameter 'recordsetStructure'")
		}

		RecordTypeParametersXML2Plain rtp = this.recordTypes.get(segmentName)
		if (rtp.fixedLengths != null) {
			checkFieldCountConsistency(segmentName, childFields, rtp.fixedLengths.length)
		}

		return this.plainOut.generateLineText(childFields, rtp.fieldSeparator, rtp.fixedLengths, rtp.endSeparator,
				rtp.fixedLengthTooShortHandling)
	}

	private void checkFieldCountConsistency(String segmentName, List<Field> childFields, int noOfColumns) {
		int leafFieldCount = 0
		// Count the number of child leaf nodes
		for (Field childField : childFields) {
			Object fieldContent = childField.fieldContent
			if (fieldContent instanceof String) {
				leafFieldCount++
			}
		}
		if (leafFieldCount > noOfColumns) {
			throw new ConverterException("More fields found in XML structure than specified in parameter '${segmentName}.fieldFixedLengths'")
		}
	}
}
