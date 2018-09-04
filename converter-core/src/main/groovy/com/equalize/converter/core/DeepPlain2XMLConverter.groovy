package com.equalize.converter.core

import com.equalize.converter.core.fcc.RecordTypeParametersFactory
import com.equalize.converter.core.fcc.RecordTypeParametersPlain2XML
import com.equalize.converter.core.util.AbstractConverter
import com.equalize.converter.core.util.ClassTypeConverter
import com.equalize.converter.core.util.ConversionDOMOutput
import com.equalize.converter.core.util.ConversionPlainInput
import com.equalize.converter.core.util.ConverterException
import com.equalize.converter.core.util.Field
import com.sap.aii.af.sdk.xi.adapter.trans.Separator

class DeepPlain2XMLConverter extends AbstractConverter {
	ConversionPlainInput plainIn
	String documentName
	String documentNamespace
	int indentFactor
	String recordsetStructure
	final Map<String, RecordTypeParametersPlain2XML> recordTypes
	String encoding
	List<Field> nestedContents
	int rowOffset
	boolean trimContents
	String genericRecordType
	String endSeparator

	DeepPlain2XMLConverter(Object body, Map<String,Object> properties, ClassTypeConverter typeConverter) {
		super(body, properties, typeConverter)
		this.recordTypes = new HashMap<String, RecordTypeParametersPlain2XML>()
	}

	@Override
	void retrieveParameters() {
		this.encoding = this.ph.retrieveProperty('encoding', 'UTF-8')
		this.documentName = this.ph.retrieveProperty('documentName')
		this.documentNamespace = this.ph.retrieveProperty('documentNamespace')
		this.indentFactor = this.ph.retrievePropertyAsInt('indentFactor', '0')
		this.recordsetStructure = this.ph.retrieveProperty('recordsetStructure')
		this.rowOffset = this.ph.retrievePropertyAsInt('rowOffset', '0')
		this.trimContents = this.ph.retrievePropertyAsBoolean('trimContents', 'Y')
		this.genericRecordType = this.ph.retrieveProperty('genericRecordType', '')
		this.endSeparator = this.ph.retrieveProperty('endSeparator', '')
		// Get the parameters for each substructure type
		String[] recordsetList = this.recordsetStructure.split(',')
		recordsetList.each { recordTypeName ->
			if (recordTypeName == 'Root')
				throw new ConverterException("'Root' is a reserved name and not allowed in parameter 'recordsetStructure'")

			if (!this.recordTypes.containsKey(recordTypeName)) {
				RecordTypeParametersPlain2XML rtp = (RecordTypeParametersPlain2XML) RecordTypeParametersFactory
						.newInstance()
						.newParameter(recordTypeName, recordsetList, this.encoding, this.ph, 'plain2xml')
				rtp.storeAdditionalParameters(recordTypeName, recordsetList, this.ph)
				this.recordTypes.put(recordTypeName, rtp)
			} else {
				throw new ConverterException("Duplicate field found in 'recordsetStructure': $recordTypeName")
			}
		}
	}

	@Override
	void parseInput() {
		// Parse input plain text contents
		def is =  this.typeConverter.convertTo(InputStream, this.body)
		if (this.endSeparator) {
			Separator sep = new Separator(this.endSeparator, this.encoding)
			this.plainIn = new ConversionPlainInput(is, sep.toString())
		} else
			this.plainIn = new ConversionPlainInput(is)
		this.nestedContents = generateNestedContents()
	}

	@Override
	Object generateOutput() {
		// Create output converter and generate output DOM
		ConversionDOMOutput domOut = new ConversionDOMOutput(this.documentName, this.documentNamespace)
		if (this.indentFactor > 0)
			domOut.setIndentFactor(this.indentFactor)
		domOut.generateDOMOutput(this.nestedContents).toByteArray()
	}

	private List<Field> generateNestedContents() {
		List<Field> nestedContents = new ArrayList<Field>()
		// Stack is used to track the depth of the traversal of the hierarchy
		List<Field> depthStack = new ArrayList<Field>(this.recordTypes.size())
		depthStack << new Field('Root:0', nestedContents)

		// Get the raw line contents and process them line by line
		List<String> rawLineContents = this.plainIn.getLineContents()
		for (int i = this.rowOffset; i < rawLineContents.size(); i++) {
			String currentLine = rawLineContents.get(i)
			// Determine record type for line
			String lineRecordType = determineRecordType(currentLine, i)
			// Extract the content of line into node containing field-value pairs
			List<Field> lineNode = this.recordTypes.get(lineRecordType).extractLineContents(currentLine, this.trimContents, i)
			// Get the parent node for current line from stack
			List<Field> parentNode = getParentNode(depthStack, lineRecordType, i + 1, lineNode)
			// Add the line node contents to the parent node
			parentNode << new Field(lineRecordType, lineNode)
		}
		return nestedContents
	}

	private String determineRecordType(String inputLine, int lineIndex) {
		// Loop through all record sets and parse to figure out key value
		for (String keyName : this.recordTypes.keySet()) {
			RecordTypeParametersPlain2XML recordType = this.recordTypes.get(keyName)
			String keyValue = recordType.parseKeyFieldValue(inputLine)
			if (keyValue)
				return keyName
		}
		if (this.genericRecordType)
			return this.genericRecordType
		else
			throw new ConverterException("Unable to determine record type for line ${lineIndex+1}")
	}

	private List<Field> getParentNode(List<Field> stack, String lineRecordType, int lineNo, List<Field> lineNode) {
		List<Field> parentNode = null
		// Go through the stack in reverse order (from bottom) to determine
		// which is the parent node
		// Entries in the stack are repeatedly removed from the bottom if it
		// does not match the current line's parent.
		// This way we do a reverse traversal of the stack back to root
		// to find the parent node
		while (stack.size() != 0) {
			Field currentStackLevel = stack.get(stack.size() - 1) // Always get the last item
			String parentRecordType = this.recordTypes.get(lineRecordType).parentRecordType
			String[] stackKey = currentStackLevel.fieldName.split(':')
			// If the stack key matches the line's parent type, then get the
			// parent node from the stack
			if (parentRecordType == stackKey[0]) {
				parentNode = (List<Field>) currentStackLevel.fieldContent
				// Add the current line to the bottom of the stack
				stack << new Field(lineRecordType + ':' + lineNo, lineNode)
				break
			} else {
				stack.remove(stack.size() - 1)
			}
		}
		if (parentNode == null)
			throw new ConverterException("Cannot find parent for line $lineNo: Record Type = $lineRecordType")
		return parentNode
	}
}
