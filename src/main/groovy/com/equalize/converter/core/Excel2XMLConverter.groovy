package com.equalize.converter.core

import com.equalize.converter.core.util.AbstractConverter
import com.equalize.converter.core.util.ClassTypeConverter
import com.equalize.converter.core.util.ConversionDOMOutput
import com.equalize.converter.core.util.ConversionExcelInput
import com.equalize.converter.core.util.ConverterException
import com.equalize.converter.core.util.Field

class Excel2XMLConverter extends AbstractConverter {

	// Module parameters
	String sheetName
	String password
	int sheetIndex
	String processFieldNames
	int headerRow = 0
	boolean onlyValidCharsInXMLName
	String fieldNames
	int columnCount = 0
	String recordName
	String documentName
	String documentNamespace
	String formatting
	boolean evaluateFormulas
	String emptyCellOutput
	String emptyCellDefaultValue
	int rowOffset
	int columnOffset
	boolean skipEmptyRows
	int indentFactor

	List<Field> sheetContents

	// Constructor
	Excel2XMLConverter(Object body, Map<String, Object> properties, ClassTypeConverter typeConverter) {
		super(body, properties, typeConverter)
	}

	@Override
	void retrieveParameters() {
		// Active sheet
		this.sheetName = this.ph.retrieveProperty('sheetName', '')
		String sheetIndexString = this.ph.retrieveProperty('sheetIndex', '')
		if (!this.sheetName && !sheetIndexString) {
			throw new ConverterException('Parameter sheetName or sheetIndex is missing')
		} else if (this.sheetName && sheetIndexString) {
			throw new ConverterException('Use only parameter sheetName or sheetIndex, not both')
		} else if (sheetIndexString)
			this.sheetIndex = sheetIndexString as int
		this.password = this.ph.retrieveProperty('password', '')
		// Output XML document properties
		this.recordName = this.ph.retrieveProperty('recordName', 'Record')
		this.documentName = this.ph.retrieveProperty('documentName')
		this.documentNamespace = this.ph.retrieveProperty('documentNamespace')

		// Row & Column processing options
		this.skipEmptyRows = this.ph.retrievePropertyAsBoolean('skipEmptyRows', 'Y')
		this.rowOffset = this.ph.retrievePropertyAsInt('rowOffset', '0')
		this.columnOffset = this.ph.retrievePropertyAsInt('columnOffset', '0')

		// Determine number of columns and field names if any
		this.processFieldNames = this.ph.retrieveProperty('processFieldNames')
		this.ph.checkValidValues('processFieldNames', this.processFieldNames, ['fromFile', 'fromConfiguration', 'notAvailable'] as Set)
		switch (this.processFieldNames) {
			case 'fromFile':
				this.onlyValidCharsInXMLName = this.ph.retrievePropertyAsBoolean('onlyValidCharsInXMLName', 'N')
				this.headerRow = this.ph.retrievePropertyAsInt('headerRow', '0')
				if (this.rowOffset == 0)
					this.rowOffset = this.headerRow + 1
				if (this.headerRow >= this.rowOffset)
					throw new ConverterException("Parameter 'rowOffset' must be larger than parameter 'headerRow'")
				break

			case 'fromConfiguration':
				this.fieldNames = this.ph.retrieveProperty('fieldNames')
				break

			case 'notAvailable':
				this.columnCount = this.ph.retrievePropertyAsInt('columnCount')
				break
		}

		// Output options
		this.formatting = this.ph.retrieveProperty('formatting', 'excel')
		this.ph.checkValidValues('formatting', this.formatting, ['excel', 'raw'] as Set)
		this.evaluateFormulas = this.ph.retrievePropertyAsBoolean('evaluateFormulas', 'Y')
		this.emptyCellOutput = this.ph.retrieveProperty('emptyCellOutput', 'suppress')
		this.ph.checkValidValues('emptyCellOutput', this.emptyCellOutput, ['suppress', 'defaultValue'] as Set)
		if (this.emptyCellOutput == 'defaultValue')
			this.emptyCellDefaultValue = this.ph.retrieveProperty('emptyCellDefaultValue', '')
		this.indentFactor = this.ph.retrievePropertyAsInt('indentFactor', '0')
	}

	@Override
	void parseInput() {
		def is =  this.typeConverter.convertTo(InputStream, this.body)
		ConversionExcelInput excelIn
		if (this.sheetName)
			excelIn = new ConversionExcelInput(is, this.sheetName, this.password)
		else
			excelIn = new ConversionExcelInput(is, this.sheetIndex, this.password)

		excelIn.determineColumnDetails(this.processFieldNames, this.fieldNames, this.columnCount, this.headerRow, this.onlyValidCharsInXMLName)

		// Get the cell contents of the sheet
		this.sheetContents = excelIn.extractExcelContent(this.rowOffset, this.columnOffset,
				this.skipEmptyRows, this.evaluateFormulas, this.formatting, this.emptyCellDefaultValue, this.headerRow, this.recordName)
	}

	@Override
	Object generateOutput() {
		ConversionDOMOutput domOut = new ConversionDOMOutput(this.documentName, this.documentNamespace)
		if (this.indentFactor > 0)
			domOut.setIndentFactor(this.indentFactor)
		domOut.generateDOMOutput(this.sheetContents).toByteArray()
	}
}
