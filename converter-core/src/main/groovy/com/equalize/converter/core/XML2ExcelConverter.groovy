package com.equalize.converter.core

import com.equalize.converter.core.util.AbstractConverter
import com.equalize.converter.core.util.ClassTypeConverter
import com.equalize.converter.core.util.ConversionDOMInput
import com.equalize.converter.core.util.ConversionExcelOutput
import com.equalize.converter.core.util.Field
import com.equalize.converter.core.util.XMLElementContainer

class XML2ExcelConverter extends AbstractConverter {

	// Module parameters
	String sheetName
	String excelFormat
	String addHeaderLine
	String fieldNames

	String[] columnNames
	XMLElementContainer rootXML

	XML2ExcelConverter(Object body, Map<String, Object> properties, ClassTypeConverter typeConverter) {
		super(body, properties, typeConverter)
	}

	@Override
	void retrieveParameters() {
		// Output format
		this.excelFormat = this.ph.retrieveProperty('excelFormat', 'xlsx')
		this.ph.checkValidValues('excelFormat', this.excelFormat, ['xlsx', 'xls'] as Set)

		// Sheet name
		this.sheetName = this.ph.retrieveProperty('sheetName', 'Sheet1')
		// Header line
		this.addHeaderLine = this.ph.retrieveProperty('addHeaderLine', 'none')
		this.ph.checkValidValues('addHeaderLine', this.addHeaderLine, ['none', 'fromXML', 'fromConfiguration'] as Set)
		if (this.addHeaderLine == 'fromConfiguration') {
			this.fieldNames = this.ph.retrieveProperty('fieldNames')
			this.columnNames = this.fieldNames.split(',')
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
		ConversionExcelOutput excelOut = new ConversionExcelOutput(this.excelFormat)
		List<Field> contents = generateList(this.rootXML)
		addHeaderLine(contents)
		excelOut.generateOutput(this.sheetName, contents)
	}

	private List<Field> generateList(XMLElementContainer element) {
		List<Field> contents = new ArrayList<Field>()
		element.getChildFields().each { child ->
			switch(child.fieldContent) {
				case XMLElementContainer:
					contents.add(new Field(child.fieldName, generateList(child.fieldContent))) //TODO - trampoline
					break
				case String:
					contents.add(new Field(child.fieldName, child.fieldContent))
					break
			}
		}
		return contents
	}

	private void addHeaderLine(List<Field> contents) {
		List<Field> header = new ArrayList<Field>()
		switch (this.addHeaderLine) {
			case 'fromConfiguration':
				for (int i = 0; i < this.columnNames.length; i++) {
					header.add(new Field("Column_${i}", this.columnNames[i]))
				}
				break
			case 'fromXML':
				XMLElementContainer firstRow = this.rootXML.getChildFields().get(0).fieldContent
				firstRow.getChildFields().each {
					header.add(new Field(it.fieldName, it.fieldName))
				}
				break
		}
		if (header.size() != 0)
			contents.add(0, new Field('Header', header))
	}
}
