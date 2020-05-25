package com.equalize.converter.core

import org.apache.camel.CamelContext
import org.apache.camel.Exchange
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.impl.DefaultExchange

import com.equalize.converter.core.util.ConversionExcelInput
import com.equalize.converter.core.util.ConverterException
import com.equalize.cpi.converter.FormatConversionBean

import spock.lang.Specification

class XML2ExcelConverterSpec extends Specification {
	static final String filePath = 'src/test/resources/Excel'
	static final String newLine = System.getProperty('line.separator')

	Exchange exchange
	Map<String,Object> properties

	String inputFileName
	String outputFileName
	File expectedOutputFile

	def setup() {
		// Setup the Camel context, Camel exchange
		CamelContext context = new DefaultCamelContext()
		this.exchange = new DefaultExchange(context)
		this.properties = ['converterClass':'com.equalize.converter.core.XML2ExcelConverter']
	}

	private byte[] process() {
		this.exchange.getIn().setBody(new File("$filePath/$inputFileName"))
		this.expectedOutputFile = new File("$filePath/$outputFileName")

		def fcb = new FormatConversionBean(this.exchange, properties)
		fcb.convert()
	}

	// Reference - https://blogs.sap.com/2014/11/03/exceltransformbean-part-2-convert-simple-xml-to-various-excel-formats-easily/

	def 'XML -> Excel - exception is thrown when fieldNames is not configured when addHeaderLine = fromConfiguration'() {
		given:
		this.properties << ['addHeaderLine':'fromConfiguration']

		when:
		process()

		then:
		ConverterException e = thrown()
		e.message == "Mandatory parameter 'fieldNames' is missing"
	}

	def 'XML -> Excel - XLSX output'() {
		given:
		this.inputFileName = 'XML2Excel_Scenario1.xml'
		this.outputFileName = 'XML2Excel_Scenario1_output.xlsx'

		when:
		InputStream is = new ByteArrayInputStream(process())
		ConversionExcelInput generatedExcel = new ConversionExcelInput(is, 0)

		then:
		verifyAll {
			generatedExcel.retrieveFormat() == 'xlsx'
			generatedExcel.retrieveCellStringValue(0, 0) == '123456.00'
			generatedExcel.retrieveCellStringValue(0, 1) == '7-Dec'
			generatedExcel.retrieveCellStringValue(0, 2) == 'ABC123'
			generatedExcel.retrieveCellStringValue(0, 3) == '10'
			generatedExcel.retrieveCellStringValue(1, 0) == '47890'
			generatedExcel.retrieveCellStringValue(1, 1) == '3-Feb'
			generatedExcel.retrieveCellStringValue(1, 2) == 'XYZ456'
			generatedExcel.retrieveCellStringValue(1, 3) == '15'
		}
	}

	def 'XML -> Excel - XLSX output using DOM'() {
		given:
		this.properties << ['useDOM':'Y']
		this.inputFileName = 'XML2Excel_Scenario1.xml'
		this.outputFileName = 'XML2Excel_Scenario1_output.xlsx'

		when:
		InputStream is = new ByteArrayInputStream(process())
		ConversionExcelInput generatedExcel = new ConversionExcelInput(is, 0)

		then:
		verifyAll {
			generatedExcel.retrieveFormat() == 'xlsx'
			generatedExcel.retrieveCellStringValue(0, 0) == '123456.00'
			generatedExcel.retrieveCellStringValue(0, 1) == '7-Dec'
			generatedExcel.retrieveCellStringValue(0, 2) == 'ABC123'
			generatedExcel.retrieveCellStringValue(0, 3) == '10'
			generatedExcel.retrieveCellStringValue(1, 0) == '47890'
			generatedExcel.retrieveCellStringValue(1, 1) == '3-Feb'
			generatedExcel.retrieveCellStringValue(1, 2) == 'XYZ456'
			generatedExcel.retrieveCellStringValue(1, 3) == '15'
		}
	}

	def 'XML -> Excel - XLS output'() {
		given:
		this.properties << ['excelFormat':'xls']
		this.properties << ['sheetName':'MySheet1']
		this.properties << ['addHeaderLine':'fromXML']
		this.inputFileName = 'XML2Excel_Scenario2.xml'
		this.outputFileName = 'XML2Excel_Scenario2_output.xls'

		when:
		InputStream is = new ByteArrayInputStream(process())
		ConversionExcelInput generatedExcel = new ConversionExcelInput(is, 'MySheet1')

		then:
		verifyAll {
			generatedExcel.retrieveFormat() == 'xls'
			generatedExcel.retrieveCellStringValue(0, 0) == 'Order'
			generatedExcel.retrieveCellStringValue(0, 1) == 'Date'
			generatedExcel.retrieveCellStringValue(0, 2) == 'Material'
			generatedExcel.retrieveCellStringValue(0, 3) == 'Quantity'
			generatedExcel.retrieveCellStringValue(1, 0) == '123456.00'
			generatedExcel.retrieveCellStringValue(1, 1) == '7-Dec'
			generatedExcel.retrieveCellStringValue(1, 2) == 'ABC123'
			generatedExcel.retrieveCellStringValue(1, 3) == '10'
			generatedExcel.retrieveCellStringValue(2, 0) == '47890'
			generatedExcel.retrieveCellStringValue(2, 1) == '3-Feb'
			generatedExcel.retrieveCellStringValue(2, 2) == 'XY&Z456'
			generatedExcel.retrieveCellStringValue(2, 3) == '15'
		}
	}

	def 'XML -> Excel - XLSX output addHeaderLine fromConfiguration'() {
		given:
		this.properties << ['addHeaderLine':'fromConfiguration']
		this.properties << ['fieldNames':'Field1,Date,Field2,Quantity']
		this.inputFileName = 'XML2Excel_Scenario3.xml'
		this.outputFileName = 'XML2Excel_Scenario3_output.xlsx'

		when:
		InputStream is = new ByteArrayInputStream(process())
		ConversionExcelInput generatedExcel = new ConversionExcelInput(is, 0)

		then:
		verifyAll {
			generatedExcel.retrieveFormat() == 'xlsx'
			generatedExcel.retrieveCellStringValue(0, 0) == 'Field1'
			generatedExcel.retrieveCellStringValue(0, 1) == 'Date'
			generatedExcel.retrieveCellStringValue(0, 2) == 'Field2'
			generatedExcel.retrieveCellStringValue(0, 3) == 'Quantity'
			generatedExcel.retrieveCellStringValue(1, 0) == '123456.00'
			generatedExcel.retrieveCellStringValue(1, 1) == ''
			generatedExcel.retrieveCellStringValue(1, 2) == 'ABC123'
			generatedExcel.retrieveCellStringValue(1, 3) == '10'
			generatedExcel.retrieveCellStringValue(2, 0) == '47890'
			generatedExcel.retrieveCellStringValue(2, 1) == '3-Feb'
			generatedExcel.retrieveCellStringValue(2, 2) == ''
			generatedExcel.retrieveCellStringValue(2, 3) == ''
		}
	}
}