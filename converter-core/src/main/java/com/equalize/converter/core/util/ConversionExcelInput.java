package com.equalize.converter.core.util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.xerces.util.XMLChar;

public class ConversionExcelInput {

	private final Workbook workbook;
	private final Sheet sheet;

	private String[] columnNames;
	private int noOfColumns;

	public ConversionExcelInput(InputStream is, String sheetName) throws Exception {
		this.workbook = WorkbookFactory.create(is);
		this.sheet = this.workbook.getSheet(sheetName);
		if (this.sheet == null)
			throw new ConverterException("Sheet " + sheetName + " not found");
	}

	public ConversionExcelInput(InputStream is, int sheetIndex) throws Exception {
		this.workbook = WorkbookFactory.create(is);
		this.sheet = this.workbook.getSheetAt(sheetIndex);
	}
	
	public String retrieveCellStringValue(int row, int col) {
		return this.sheet.getRow(row).getCell(col).getStringCellValue();
	}
	
	public String retrieveFormat() {
		if (this.workbook instanceof HSSFWorkbook) {
			return "xls"; 
		} else {
			return "xlsx";
		}
	}

	public void determineColumnDetails(String processFieldNames, String fieldNames, int columnCount, int headerRow,
			boolean onlyValidCharsInXMLName) throws ConverterException {
		switch (processFieldNames) {
			case "fromFile":
				this.noOfColumns = retrieveHeaderColumnCount(headerRow);
				this.columnNames = retrieveColumnNamesFromFileHeader(headerRow, this.noOfColumns,
						onlyValidCharsInXMLName);
				break;
			case "fromConfiguration":
				this.columnNames = fieldNames.split(",");
				this.noOfColumns = this.columnNames.length;
				break;
			case "notAvailable":
				this.noOfColumns = columnCount;
				break;
		}
	}

	public List<Field> extractExcelContent(int startRow, int startCol, boolean skipEmptyRows, boolean evaluateFormulas,
			String formatting, String emptyCellDefaultValue, int headerRow, String recordName)
			throws ConverterException {
		int noOfRows = this.sheet.getLastRowNum() + 1;
		if (startRow >= noOfRows)
			throw new ConverterException("Starting row is greater than last row of sheet");

		List<Field> contents = new ArrayList<Field>();
		int lastColumn = startCol + this.noOfColumns;
		// Go through each row
		for (int rowNo = startRow; rowNo < noOfRows; rowNo++) {
			Row row = this.sheet.getRow(rowNo);
			boolean contentFoundOnRow = false;
			if (row != null) {
				List<Field> rowContent = new ArrayList<Field>(this.noOfColumns);
				// Go through each column cell of the current row
				for (int colNo = startCol; colNo < lastColumn; colNo++) {
					Cell cell = row.getCell(colNo);
					String columnName = retrieveColumnName(colNo - startCol);
					if (cell != null) {
						String cellContent = retrieveCellContent(cell, evaluateFormulas, formatting);
						if (cellContent != null) {
							contentFoundOnRow = true;
							rowContent.add(new Field(columnName, cellContent));
						} else if (emptyCellDefaultValue != null) {
							rowContent.add(new Field(columnName, emptyCellDefaultValue));
						}
					} else {
						rowContent.add(new Field(columnName, emptyCellDefaultValue));
					}
				}
				if (contentFoundOnRow)
					contents.add(new Field(recordName, rowContent));
			}
			// Add empty rows if skip parameter set to NO
			if (!skipEmptyRows && !contentFoundOnRow && emptyCellDefaultValue != null) {
				List<Field> emptyRow = new ArrayList<Field>(this.noOfColumns);
				for (int i = 0; i < this.noOfColumns; i++) {
					emptyRow.add(new Field(retrieveColumnName(i), emptyCellDefaultValue));
				}
				contents.add(new Field(recordName, emptyRow));
			}
		}
		if (contents.size() == 0)
			throw new ConverterException("No rows with valid contents found");
		else
			return contents;
	}

	private String retrieveCellContent(Cell cell, boolean evaluateFormulas, String formatting) {
		FormulaEvaluator evaluator = this.workbook.getCreationHelper().createFormulaEvaluator();
		DataFormatter formatter = new DataFormatter(true);
		String cellContent = null;
		CellType cellType = cell.getCellTypeEnum();
		switch (cellType) {
			case FORMULA:
				cellContent = evaluateFormulas ? formatter.formatCellValue(cell, evaluator) : cell.getCellFormula();
				break;
			case NUMERIC:
				cellContent = formatting.equals("raw") ? Double.toString(cell.getNumericCellValue())
						: formatter.formatCellValue(cell);
				break;
			case STRING:
				cellContent = formatting.equals("raw") ? cell.getStringCellValue() : formatter.formatCellValue(cell);
				break;
			case BOOLEAN:
				cellContent = formatting.equals("raw") ? Boolean.toString(cell.getBooleanCellValue())
						: formatter.formatCellValue(cell);
				break;
			default:
				break;
		}
		return cellContent;
	}

	private int retrieveHeaderColumnCount(int headerRow) throws ConverterException {
		Row header = this.sheet.getRow(headerRow);
		int lastCellNum = 0;
		if (header != null)
			lastCellNum = header.getLastCellNum();

		if (lastCellNum != 0)
			return lastCellNum;
		else
			throw new ConverterException("No. of columns in row " + headerRow + " is zero.");
	}

	private String[] retrieveColumnNamesFromFileHeader(int headerRow, int columnNo, boolean onlyValidCharsInXMLName)
			throws ConverterException {
		Row row = this.sheet.getRow(headerRow);
		String[] headerColumns = new String[columnNo];
		for (int col = 0; col < columnNo; col++) {
			Cell cell = row.getCell(col);
			if (cell == null)
				throw new ConverterException("Empty column name found");

			headerColumns[col] = cell.getStringCellValue();
			String fieldName = headerColumns[col].replaceAll("\\s+", "");

			// ensure only valid chars are included in the XML element name
			if (onlyValidCharsInXMLName)
				fieldName = XMLChar.stripInvalidCharsFromName(fieldName);

			if (fieldName.isEmpty())
				throw new ConverterException("Empty column name found");

			if (!fieldName.equals(headerColumns[col]))
				headerColumns[col] = fieldName;
		}
		return headerColumns;
	}

	private String retrieveColumnName(int index) {
		if (this.columnNames != null)
			return this.columnNames[index];
		else
			return "Column" + Integer.toString(index + 1);
	}
}
