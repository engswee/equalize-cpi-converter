package com.equalize.converter.core

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.FormulaEvaluator
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.w3c.dom.Document
import org.w3c.dom.Node

import com.equalize.converter.core.util.AbstractConverter
import com.equalize.converter.core.util.ClassTypeConverter
import com.equalize.converter.core.util.ConverterException
import org.apache.xerces.util.XMLChar

public class Excel2XMLConverter extends AbstractConverter {

	// Module parameters
	private String sheetName
	private int sheetIndex
	private String processFieldNames
	private int headerRow = 0
	private boolean onlyValidCharsInXMLName
	private String fieldNames
	private int columnCount = 0
	private String recordName
	private String documentName
	private String documentNamespace
	private String formatting
	private boolean evaluateFormulas
	private String emptyCellOutput
	private String emptyCellDefaultValue
	private int rowOffset
	private int columnOffset
	private boolean skipEmptyRows
	private int indentFactor

	private String[] columnNames
	private int noOfRows = 0
	private ArrayList<String[]> sheetContents

	// Constructor
	public Excel2XMLConverter(Object body, Map<String,Object> properties, ClassTypeConverter typeConverter) {
		super(body, properties, typeConverter)
	}

	@Override
	public void retrieveParameters() {
		// Active sheet
		this.sheetName = this.ph.retrieveProperty('sheetName', '')
		String sheetIndexString = this.ph.retrieveProperty('sheetIndex', '')
		if (!this.sheetName && !sheetIndexString) {
			throw new ConverterException('Parameter sheetName or sheetIndex is missing')
		} else if (this.sheetName && sheetIndexString) {
			throw new ConverterException('Use only parameter sheetName or sheetIndex, not both')
		} else if (sheetIndexString) {
			this.sheetIndex = this.ph.retrievePropertyAsInt('sheetIndex')
		}

		// Output XML document properties
		this.recordName = this.ph.retrieveProperty('recordName', 'Record')
		this.documentName = this.ph.retrieveProperty('documentName')
		this.documentNamespace = this.ph.retrieveProperty('documentNamespace')

		// Row & Column processing options
		this.skipEmptyRows = this.ph.retrievePropertyAsBoolean('skipEmptyRows', 'Y')
//		if (!this.skipEmptyRows) {
//			this.audit.addLog(AuditLogStatus.SUCCESS, 'Empty rows will be included')
//		}
		this.rowOffset = this.ph.retrievePropertyAsInt('rowOffset', '0')
		this.columnOffset = this.ph.retrievePropertyAsInt('columnOffset', '0')

		// Determine number of columns and field names if any
		this.processFieldNames = this.ph.retrieveProperty('processFieldNames')
		this.ph.checkValidValues('processFieldNames', this.processFieldNames, ['fromFile', 'fromConfiguration', 'notAvailable'] as Set)
		if (this.processFieldNames.equalsIgnoreCase('fromFile')) {
			this.onlyValidCharsInXMLName = this.ph.retrievePropertyAsBoolean('onlyValidCharsInXMLName', 'N')
			this.headerRow = this.ph.retrievePropertyAsInt('headerRow', '0')
			// this.columnCount remains 0
			if (this.rowOffset == 0) {
				this.rowOffset = this.headerRow + 1
				//this.audit.addLog(AuditLogStatus.ERROR, 'Processing automatically skipped to row after header row')
			}
			// throw an exception if headerRow is equal to or larger than
			// rowOffset.
			if (this.headerRow >= this.rowOffset) {
				throw new ConverterException("Parameter 'rowOffset' must be larger than parameter 'headerRow'")
			}
		} else if (this.processFieldNames.equalsIgnoreCase('fromConfiguration')) {
			this.fieldNames = this.ph.retrieveProperty('fieldNames', '')
			if (this.fieldNames == null || this.fieldNames.replaceAll('\\s+', '').isEmpty()) {
				throw new ConverterException(
						"Parameter 'fieldNames' required when 'processFieldNames' = fromConfiguration")
			} else {
				this.columnNames = this.fieldNames.split(',')
				this.columnCount = this.columnNames.length
			}
		} else if (this.processFieldNames.equalsIgnoreCase('notAvailable')) {
			//this.ph.getConditionallyMandatoryParameter('columnCount', 'processFieldNames', 'notAvailable')
			this.columnCount = this.ph.retrievePropertyAsInt('columnCount')
			if (this.columnCount <= 0) {
				throw new ConverterException('Only positive integers allowed for columnCount')
			}
		}

		// Output options
		this.formatting = this.ph.retrieveProperty('formatting', 'excel')
//		this.ph.checkParamValidValues('formatting', 'excel,raw')
//		if (this.formatting.equalsIgnoreCase('raw')) {
//			this.audit.addLog(AuditLogStatus.SUCCESS,
//					'Cell contents will not be formatted, raw values displayed instead')
//		}
		this.evaluateFormulas = this.ph.retrievePropertyAsBoolean('evaluateFormulas', 'Y')
//		if (!this.evaluateFormulas) {
//			this.audit.addLog(AuditLogStatus.SUCCESS,
//					'Formulas will not be evaluated, formula logic displayed instead')
//		}
		this.emptyCellOutput = this.ph.retrieveProperty('emptyCellOutput', 'suppress')
		//this.ph.checkParamValidValues('emptyCellOutput', 'suppress,defaultValue')
		if (this.emptyCellOutput.equalsIgnoreCase('defaultValue')) {
			this.emptyCellDefaultValue = this.ph.retrieveProperty('emptyCellDefaultValue', '')
//			this.audit.addLog(AuditLogStatus.SUCCESS,
//					'Empty cells will be filled with default value: '' + this.emptyCellDefaultValue + ''')
		}
		this.indentFactor = this.ph.retrievePropertyAsInt('indentFactor', '0')
//		if (this.indentFactor > 0) {
//			this.audit.addLog(AuditLogStatus.SUCCESS, 'XML output will be indented')
//		}
	}

	@Override
	public void parseInput() {
		// Get workbook
		Workbook wb
		try {
			def is =  this.typeConverter.convertTo(InputStream, this.body)
			wb = WorkbookFactory.create(is)
		} catch (Exception e) {
			throw new ConverterException(e.getMessage(), e)
		}
		// Get the sheet
		Sheet sheet = retrieveSheet(wb, this.sheetName, this.sheetIndex)
		// Get the number of rows and columns
		if (this.columnCount == 0) { // this only happens if processFieldNames =
										// fromFile
			this.columnCount = retrieveHeaderColumnCount(sheet)
		}

		this.noOfRows = sheet.getLastRowNum() + 1

		// Get the column names from header
		if (this.processFieldNames.equalsIgnoreCase('fromFile')) {
			this.columnNames = retrieveColumnNamesFromFileHeader(sheet, this.columnCount)
		}

		// Get the cell contents of the sheet
		this.sheetContents = extractSheetContents(sheet, wb, this.rowOffset, this.noOfRows, this.columnOffset,
				this.columnCount, this.skipEmptyRows, this.evaluateFormulas, this.formatting, false)
	}

	@Override
	public byte[] generateOutput() {
		try {
			DocumentBuilder docBuilder
			docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
			Document outDoc = docBuilder.newDocument()

			Node outRoot = outDoc.createElementNS(this.documentNamespace, 'ns:' + this.documentName)
			outDoc.appendChild(outRoot)

			//this.audit.addLog(AuditLogStatus.SUCCESS, 'Constructing output XML')
			// Loop through the 2D array of saved contents
			for (int row = 0; row < this.sheetContents.size(); row++) {
				String[] rowContent = this.sheetContents.get(row)
				// Add new row
				Node outRecord = addElementToNode(outDoc, outRoot, this.recordName)
				for (int col = 0; col < rowContent.length; col++) {
					if (rowContent[col] == null && this.emptyCellDefaultValue != null) {
						rowContent[col] = this.emptyCellDefaultValue
					}
					if (rowContent[col] != null) {
						String fieldName
						if (this.columnNames != null) {
							fieldName = this.columnNames[col]
						} else {
							fieldName = 'Column' + Integer.toString(col + 1)
						}
						// Add fields of the row
						addElementToNode(outDoc, outRecord, fieldName, rowContent[col])
					}
				}
			}
			// Transform the DOM to OutputStream
			javax.xml.transform.Transformer transformer = TransformerFactory.newInstance().newTransformer()
			if (this.indentFactor > 0) {
				transformer.setOutputProperty(OutputKeys.INDENT, 'yes')
				transformer.setOutputProperty('{http://xml.apache.org/xslt}indent-amount',
						Integer.toString(this.indentFactor))
			}
			ByteArrayOutputStream baos = new ByteArrayOutputStream()
			transformer.transform(new DOMSource(outDoc), new StreamResult(baos))

			//this.audit.addLog(AuditLogStatus.SUCCESS, 'Conversion complete')
			return baos.toByteArray()
		} catch (Exception e) {
			throw new ConverterException(e.getMessage(), e)
		}
	}

	private Sheet retrieveSheet(Workbook wb, String name, int sheetIndex) {
		Sheet sheet = null
		if (name) {
			//this.audit.addLog(AuditLogStatus.SUCCESS, 'Accessing sheet ' + name)
			sheet = wb.getSheet(name)
			if (sheet == null) {
				throw new ConverterException('Sheet ' + name + ' not found')
			}
		} else {
			sheet = wb.getSheetAt(sheetIndex)
//			this.audit.addLog(AuditLogStatus.SUCCESS,
//					'Accessing sheet ' + sheet.getSheetName() + ' at index ' + sheetIndex)
		}
		return sheet
	}

	private int retrieveHeaderColumnCount(Sheet sheet) {
		Row header = sheet.getRow(this.headerRow)
		int lastCellNum = 0
		if (header != null) {
			lastCellNum = header.getLastCellNum()
		}
		if (lastCellNum != 0) {
//			this.audit.addLog(AuditLogStatus.SUCCESS,
//					'No. of columns dynamically set to ' + lastCellNum + ' based on row ' + this.headerRow)
			return lastCellNum
		} else {
			throw new ConverterException('No. of columns in row ' + this.headerRow + ' is zero.')
		}
	}

	private String[] retrieveColumnNamesFromFileHeader(Sheet sheet, int columnNo) {
		Row row = sheet.getRow(this.headerRow)
		//this.audit.addLog(AuditLogStatus.SUCCESS, 'Retrieving column names from row ' + this.headerRow)
		String[] headerColumns = new String[columnNo]
		for (int col = 0; col < columnNo; col++) {
			Cell cell = row.getCell(col)
			if (cell == null) {
				throw new ConverterException('Empty column name found')
			}
			headerColumns[col] = cell.getStringCellValue()
			String fieldName = headerColumns[col].replaceAll('\\s+', '')

			// ensure only valid chars are included in the XML element name
			if (this.onlyValidCharsInXMLName) {
				fieldName = XMLChar.stripInvalidCharsFromName(fieldName)
			}

			if (fieldName.isEmpty()) {
				throw new ConverterException('Empty column name found')
			}
			if (!fieldName.equals(headerColumns[col])) {
				//this.audit.addLog(AuditLogStatus.SUCCESS,
				//		'Renaming field '' + headerColumns[col] + '' to ' + fieldName)
				headerColumns[col] = fieldName
			}
		}
		return headerColumns
	}

	private ArrayList<String[]> extractSheetContents(Sheet sheet, Workbook wb, int startRow, int noOfRows, int startCol,
			int noOfColumns, boolean skipEmptyRows, boolean evaluateFormulas, String formatting, boolean debug) {
		if (startRow >= noOfRows) {
			throw new ConverterException('Starting row is greater than last row of sheet')
		}
		//this.audit.addLog(AuditLogStatus.SUCCESS, 'Extracting Excel sheet contents')
		//this.audit.addLog(AuditLogStatus.SUCCESS, 'Start processing from row ' + Integer.toString(startRow + 1))
		//this.audit.addLog(AuditLogStatus.SUCCESS, 'Start processing from column ' + Integer.toString(startCol + 1))
		ArrayList<String[]> contents = new ArrayList<String[]>()
		int lastColumn = startCol + noOfColumns
		// Go through each row
		for (int rowNo = startRow; rowNo < noOfRows; rowNo++) {
			Row row = sheet.getRow(rowNo)
			boolean contentFound = false
			if (row != null) {
				String[] rowContent = new String[noOfColumns]
				// Go through each column cell of the current row
				for (int colNo = startCol; colNo < lastColumn; colNo++) {
					Cell cell = row.getCell(colNo)
					if (cell != null) {
						rowContent[colNo - startCol] = retrieveCellContent(cell, wb, evaluateFormulas, formatting)
						if (rowContent[colNo - startCol] != null) {
							contentFound = true
						}
					}
//					if (debug) {
//						this.audit.addLog(AuditLogStatus.SUCCESS, 'DEBUG Cell ' + Integer.toString(rowNo + 1) + ':'
//								+ Integer.toString(colNo + 1) + ' - ' + rowContent[colNo])
//					}
				}
				if (contentFound) {
					contents.add(rowContent)
				}
			} else if (debug) {
//				this.audit.addLog(AuditLogStatus.SUCCESS, 'DEBUG Row ' + Integer.toString(rowNo + 1) + ' empty')
			}
			// Add empty rows if skip parameter set to NO
			if (!skipEmptyRows && !contentFound) {
				contents.add(new String[noOfColumns])
			}

		}
		if (contents.size() == 0) {
			throw new ConverterException('No rows with valid contents found')
		} else {
			return contents
		}
	}

	private String retrieveCellContent(Cell cell, Workbook wb, boolean evaluateFormulas, String formatting) {
		FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator()
		DataFormatter formatter = new DataFormatter(true)
		String cellContent = null
		int cellType = cell.getCellType()
		switch (cellType) {
		case Cell.CELL_TYPE_BLANK:
			break
		case Cell.CELL_TYPE_FORMULA:
			if (evaluateFormulas) {
				cellContent = formatter.formatCellValue(cell, evaluator)
			} else {
				// Display the formula instead
				cellContent = cell.getCellFormula()
			}
			break
		default:
			if (formatting.equalsIgnoreCase('excel')) {
				cellContent = formatter.formatCellValue(cell)
			} else if (formatting.equalsIgnoreCase('raw')) {
				// Display the raw cell contents
				switch (cellType) {
				case Cell.CELL_TYPE_NUMERIC:
					cellContent = Double.toString(cell.getNumericCellValue())
					break
				case Cell.CELL_TYPE_STRING:
					cellContent = cell.getStringCellValue()
					break
				case Cell.CELL_TYPE_BOOLEAN:
					cellContent = Boolean.toString(cell.getBooleanCellValue())
					break
				}
			}
			break
		}
		return cellContent
	}

	private Node addElementToNode(Document doc, Node parentNode, String elementName) {
		Node element = doc.createElement(elementName)
		parentNode.appendChild(element)
		return element
	}

	private Node addElementToNode(Document doc, Node parentNode, String elementName, String elementTextValue) {
		Node element = addElementToNode(doc, parentNode, elementName)
		if (elementTextValue != null) {
			element.appendChild(doc.createTextNode(elementTextValue))
		}
		return element
	}
}
