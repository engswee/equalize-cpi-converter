package com.equalize.converter.core

import com.equalize.converter.core.util.AbstractConverter
import com.equalize.converter.core.util.ClassTypeConverter
import com.equalize.converter.core.util.ConversionExcelInput
import com.equalize.converter.core.util.ConversionExcelOutput
import com.equalize.converter.core.util.Field

class Excel2ExcelConverter extends AbstractConverter {

	String sheetName
	int headerRow = 0

	String excelFormat
	List<Field> contents

	// Constructor
	Excel2ExcelConverter(Object body, Map<String, Object> properties, ClassTypeConverter typeConverter) {
		super(body, properties, typeConverter)
	}

	@Override
	void retrieveParameters() {
		this.sheetName = this.ph.retrieveProperty('sheetName')
		this.headerRow = this.ph.retrievePropertyAsInt('headerRow', '0')
	}

	@Override
	void parseInput() {
		def is =  this.typeConverter.convertTo(InputStream, this.body)
		ConversionExcelInput excelIn = new ConversionExcelInput(is, this.sheetName)
		excelIn.determineColumnDetails('fromFile', null, 0, this.headerRow, false)
		int rowOffset = this.headerRow + 1
		this.contents = excelIn.extractExcelContent(rowOffset, 0, true, true, 'excel', null, this.headerRow, 'Record')
		this.excelFormat = excelIn.retrieveFormat()

		// Extract header row details
		List<Field> header = new ArrayList<Field>()
		excelIn.columnNames.each { columnName ->
			header.add(new Field(columnName, columnName))
		}
		this.contents.add(0, new Field('Header', header))
	}

	@Override
	Object generateOutput() {
		ConversionExcelOutput excelOut = new ConversionExcelOutput(this.excelFormat)
		excelOut.generateOutput(this.sheetName, this.contents)
	}
}
