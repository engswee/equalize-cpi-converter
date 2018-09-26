package com.equalize.converter.core.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ConversionExcelOutput {

	private final Workbook workbook;

	public ConversionExcelOutput(String excelFormat) {
		if (excelFormat.equals("xls"))
			this.workbook = new HSSFWorkbook();
		else
			this.workbook = new XSSFWorkbook();
	}

	public byte[] generateOutput(String sheetName, List<Field> contents) throws IOException {
		Sheet sheet = this.workbook.createSheet(sheetName);

		// Generate rows and cells - for simple structure only
		for (int i = 0; i < contents.size(); i++) {
			@SuppressWarnings("unchecked")
			List<Field> rowContents = (List<Field>) contents.get(i).fieldContent;
			Row sheetRow = sheet.createRow(i);
			for (int j = 0; j < rowContents.size(); j++) {
				Cell cell = sheetRow.createCell(j);
				cell.setCellType(CellType.STRING);
				cell.setCellValue((String) rowContents.get(j).fieldContent);
			}
		}

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		this.workbook.write(baos);
		baos.close();
		return baos.toByteArray();
	}
}
