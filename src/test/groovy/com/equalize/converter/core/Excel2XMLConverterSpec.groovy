package com.equalize.converter.core

import org.apache.camel.CamelContext
import org.apache.camel.Exchange
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.impl.DefaultExchange

import com.equalize.converter.core.util.ConverterException
import com.equalize.cpi.converter.FormatConversionBean

import spock.lang.Specification
import spock.lang.Unroll

class Excel2XMLConverterSpec extends Specification {
	static final String filePath = 'src/test/resources/Excel'

	Exchange exchange
	Map<String,Object> properties

	String inputFileName
	String outputFileName
	File expectedOutputFile

	def setup() {
		// Setup the Camel context, Camel exchange
		CamelContext context = new DefaultCamelContext()
		this.exchange = new DefaultExchange(context)
		this.properties = ['converterClass':'com.equalize.converter.core.Excel2XMLConverter']
	}

	private String process() {
		this.exchange.getIn().setBody(new File("$filePath/$inputFileName"))
		this.expectedOutputFile = new File("$filePath/$outputFileName")

		def fcb = new FormatConversionBean(this.exchange, properties)
		byte[] output = fcb.convert()

		return new String(output, 'UTF-8')
	}

	// Reference - https://blogs.sap.com/2014/10/21/exceltransformbean-part-1-convert-various-excel-formats-to-simple-xml-easily/

	@Unroll
	def 'Excel > XML - exception is thrown when #fieldName is not configured'(String fieldName) {
		given:
		this.properties << ['documentName':'MT_Order']
		this.properties << ['documentNamespace':'urn:equalize:com']
		this.properties << ['sheetName':'Sheet1']
		this.properties << ['processFieldNames':'fromFile']

		this.properties.remove(fieldName)

		when:
		process()

		then:
		ConverterException e = thrown()
		e.message == "Mandatory parameter '$fieldName' is missing"

		where:
		fieldName | _
		'documentName' | _
		'documentNamespace' | _
		'processFieldNames' | _
	}

	def 'Excel -> XML - exception is thrown when sheetName or sheetIndex is not configured'() {
		given:
		this.properties << ['documentName':'MT_Order']
		this.properties << ['documentNamespace':'urn:equalize:com']
		this.properties << ['processFieldNames':'fromFile']

		when:
		process()

		then:
		ConverterException e = thrown()
		e.message == 'Parameter sheetName or sheetIndex is missing'
	}

	def 'Excel -> XML - exception is thrown when sheetName and sheetIndex are both configured'() {
		given:
		this.properties << ['documentName':'MT_Order']
		this.properties << ['documentNamespace':'urn:equalize:com']
		this.properties << ['sheetName':'Sheet1']
		this.properties << ['sheetIndex':'0']
		this.properties << ['processFieldNames':'fromFile']

		when:
		process()

		then:
		ConverterException e = thrown()
		e.message == 'Use only parameter sheetName or sheetIndex, not both'
	}

	def 'Excel -> XML - exception is thrown when fieldNames is not configured when processFieldNames = fromConfiguration'() {
		given:
		this.properties << ['documentName':'MT_Order']
		this.properties << ['documentNamespace':'urn:equalize:com']
		this.properties << ['sheetName':'Sheet1']
		this.properties << ['processFieldNames':'fromConfiguration']

		when:
		process()

		then:
		ConverterException e = thrown()
		e.message == "Mandatory parameter 'fieldNames' is missing"
	}

	def 'Excel -> XML - exception is thrown when columnCount is not configured when processFieldNames = notAvailable'() {
		given:
		this.properties << ['documentName':'MT_Order']
		this.properties << ['documentNamespace':'urn:equalize:com']
		this.properties << ['sheetName':'Sheet1']
		this.properties << ['processFieldNames':'notAvailable']

		when:
		process()

		then:
		ConverterException e = thrown()
		e.message == "Mandatory parameter 'columnCount' is missing"
	}

	def 'Excel -> XML - exception is thrown when rowOffset is smaller than headerRow'() {
		given:
		this.properties << ['documentName':'MT_Order']
		this.properties << ['documentNamespace':'urn:equalize:com']
		this.properties << ['sheetName':'Sheet1']
		this.properties << ['processFieldNames':'fromFile']
		this.properties << ['rowOffset':'1']
		this.properties << ['headerRow':'2']

		when:
		process()

		then:
		ConverterException e = thrown()
		e.message == "Parameter 'rowOffset' must be larger than parameter 'headerRow'"
	}

	def 'Excel -> XML - exception is thrown when sheetName is invalid'() {
		given:
		this.properties << ['documentName':'MT_Order']
		this.properties << ['documentNamespace':'urn:equalize:com']
		this.properties << ['sheetName':'Sheet4']
		this.properties << ['processFieldNames':'fromFile']
		this.inputFileName = 'Excel2XML_Scenario1.xlsx'

		when:
		process()

		then:
		ConverterException e = thrown()
		e.message == 'Sheet Sheet4 not found'
	}

	def 'Excel -> XML - exception is thrown when rowOffset is larger than last row of sheet'() {
		given:
		this.properties << ['documentName':'MT_Order']
		this.properties << ['documentNamespace':'urn:equalize:com']
		this.properties << ['sheetName':'Sheet1']
		this.properties << ['processFieldNames':'fromFile']
		this.properties << ['rowOffset':'3']
		this.inputFileName = 'Excel2XML_Scenario1.xlsx'

		when:
		process()

		then:
		ConverterException e = thrown()
		e.message == 'Starting row is greater than last row of sheet'
	}

	def 'Excel -> XML - exception is thrown when columns in headerRow is empty'() {
		given:
		this.properties << ['documentName':'MT_Order']
		this.properties << ['documentNamespace':'urn:equalize:com']
		this.properties << ['sheetName':'Sheet1']
		this.properties << ['processFieldNames':'fromFile']
		this.inputFileName = 'Excel2XML_Scenario0_emptycontent.xlsx'

		when:
		process()

		then:
		ConverterException e = thrown()
		e.message == 'No. of columns in row 0 is zero.'
	}

	def 'Excel -> XML - exception is thrown when no valid contents found in sheet'() {
		given:
		this.properties << ['documentName':'MT_Order']
		this.properties << ['documentNamespace':'urn:equalize:com']
		this.properties << ['sheetName':'Sheet1']
		this.properties << ['processFieldNames':'notAvailable']
		this.properties << ['columnCount':'2']
		this.inputFileName = 'Excel2XML_Scenario0_emptycontent.xlsx'

		when:
		process()

		then:
		ConverterException e = thrown()
		e.message == 'No rows with valid contents found'
	}

	def 'Excel -> XML - exception is thrown when header row has empty column'() {
		given:
		this.properties << ['documentName':'MT_Order']
		this.properties << ['documentNamespace':'urn:equalize:com']
		this.properties << ['sheetName':'Sheet1']
		this.properties << ['processFieldNames':'fromFile']
		this.inputFileName = 'Excel2XML_Scenario0_headermissingcolumns.xlsx'

		when:
		process()

		then:
		ConverterException e = thrown()
		e.message == 'Empty column name found'
	}

	def 'Excel -> XML - exception is thrown when header row has column without valid XML characters'() {
		given:
		this.properties << ['documentName':'MT_Order']
		this.properties << ['documentNamespace':'urn:equalize:com']
		this.properties << ['sheetName':'Sheet1']
		this.properties << ['processFieldNames':'fromFile']
		this.properties << ['onlyValidCharsInXMLName':'Y']
		this.inputFileName = 'Excel2XML_Scenario0_headernovalidXML.xlsx'

		when:
		process()

		then:
		ConverterException e = thrown()
		e.message == 'Empty column name found'
	}

	def 'Excel -> XML - XLSX input processFieldNames = fromFile'() {
		given:
		this.properties << ['documentName':'MT_Order']
		this.properties << ['documentNamespace':'urn:equalize:com']
		this.properties << ['sheetName':'Sheet1']
		this.properties << ['processFieldNames':'fromFile']
		this.inputFileName = 'Excel2XML_Scenario1.xlsx'
		this.outputFileName = 'Excel2XML_Scenario1_output_noindent.xml'

		expect:
		process() == this.expectedOutputFile.getText('UTF-8')
	}

	def 'Excel -> XML - XLSX input processFieldNames = fromFile with indentFactor'() {
		given:
		this.properties << ['indentFactor':'2']
		this.properties << ['documentName':'MT_Order']
		this.properties << ['documentNamespace':'urn:equalize:com']
		this.properties << ['sheetName':'Sheet1']
		this.properties << ['processFieldNames':'fromFile']
		this.inputFileName = 'Excel2XML_Scenario1.xlsx'
		this.outputFileName = 'Excel2XML_Scenario1_output.xml'

		expect:
		process() == this.expectedOutputFile.getText('UTF-8')
	}

	def 'Excel -> XML - XLS input processFieldNames = fromConfiguration, rowOffset, specific recordName and raw formatting'() {
		given:
		this.properties << ['indentFactor':'2']
		this.properties << ['documentName':'MT_CustomOrder']
		this.properties << ['documentNamespace':'urn:equalize:com']
		this.properties << ['sheetIndex':'0']
		this.properties << ['processFieldNames':'fromConfiguration']
		this.properties << ['fieldNames':'Order,Date,Material,Quantity']
		this.properties << ['rowOffset':'2']
		this.properties << ['recordName':'Line']
		this.properties << ['formatting':'raw']
		this.inputFileName = 'Excel2XML_Scenario2.xls'
		this.outputFileName = 'Excel2XML_Scenario2_output.xml'

		expect:
		process() == this.expectedOutputFile.getText('UTF-8')
	}

	def 'Excel -> XML - XLSX input processFieldNames = notAvailable using sheetIndex'() {
		given:
		this.properties << ['indentFactor':'2']
		this.properties << ['documentName':'MT_CustomOrder']
		this.properties << ['documentNamespace':'urn:equalize:com']
		this.properties << ['sheetIndex':'0']
		this.properties << ['processFieldNames':'notAvailable']
		this.properties << ['columnCount':'5']
		this.properties << ['rowOffset':'1']
		this.inputFileName = 'Excel2XML_Scenario3.xlsx'
		this.outputFileName = 'Excel2XML_Scenario3_output.xml'

		expect:
		process() == this.expectedOutputFile.getText('UTF-8')
	}

	def 'Excel -> XML - XLSX input processFieldNames notAvailable - do not skip rows'() {
		given:
		this.properties << ['indentFactor':'2']
		this.properties << ['documentName':'MT_CustomOrder']
		this.properties << ['documentNamespace':'urn:equalize:com']
		this.properties << ['sheetIndex':'0']
		this.properties << ['processFieldNames':'notAvailable']
		this.properties << ['columnCount':'5']
		this.properties << ['rowOffset':'1']
		this.properties << ['skipEmptyRows':'N']
		this.properties << ['emptyCellOutput':'defaultValue']
		this.properties << ['emptyCellDefaultValue':'space']
		this.inputFileName = 'Excel2XML_Scenario3.xlsx'
		this.outputFileName = 'Excel2XML_Scenario3_output_createempty.xml'

		expect:
		process() == this.expectedOutputFile.getText('UTF-8')
	}

	def 'Excel -> XML - XLSX input processFieldNames notAvailable - do not evaluate formula'() {
		given:
		this.properties << ['indentFactor':'2']
		this.properties << ['documentName':'MT_CustomOrder']
		this.properties << ['documentNamespace':'urn:equalize:com']
		this.properties << ['sheetIndex':'0']
		this.properties << ['processFieldNames':'notAvailable']
		this.properties << ['columnCount':'5']
		this.properties << ['rowOffset':'1']
		this.properties << ['evaluateFormulas':'N']
		this.inputFileName = 'Excel2XML_Scenario3.xlsx'
		this.outputFileName = 'Excel2XML_Scenario3_output_donotevaluate.xml'

		expect:
		process() == this.expectedOutputFile.getText('UTF-8')
	}

	def 'Excel -> XML - XLSX input onlyValidCharsInXMLName = Y, formatting = excel'() {
		given:
		this.properties << ['indentFactor':'2']
		this.properties << ['documentName':'MT_Order']
		this.properties << ['documentNamespace':'urn:equalize:com']
		this.properties << ['sheetName':'Sheet1']
		this.properties << ['processFieldNames':'fromFile']
		this.properties << ['headerRow':'1']
		this.properties << ['rowOffset':'3']
		this.properties << ['onlyValidCharsInXMLName':'Y']
		this.inputFileName = 'Excel2XML_Scenario4.xlsx'
		this.outputFileName = 'Excel2XML_Scenario4_output.xml'

		expect:
		process() == this.expectedOutputFile.getText('UTF-8')
	}

	def 'Excel -> XML - XLSX input onlyValidCharsInXMLName = Y, formatting = raw'() {
		given:
		this.properties << ['indentFactor':'2']
		this.properties << ['documentName':'MT_Order']
		this.properties << ['documentNamespace':'urn:equalize:com']
		this.properties << ['sheetName':'Sheet1']
		this.properties << ['processFieldNames':'fromFile']
		this.properties << ['headerRow':'1']
		this.properties << ['rowOffset':'3']
		this.properties << ['onlyValidCharsInXMLName':'Y']
		this.properties << ['formatting':'raw']
		this.inputFileName = 'Excel2XML_Scenario4.xlsx'
		this.outputFileName = 'Excel2XML_Scenario4_output_raw.xml'

		expect:
		process() == this.expectedOutputFile.getText('UTF-8')
	}

	def 'Excel -> XML - exception is thrown when invalid XML character in file header when onlyValidCharsInXMLName = N'() {
		given:
		this.properties << ['documentName':'MT_Order']
		this.properties << ['documentNamespace':'urn:equalize:com']
		this.properties << ['sheetName':'Sheet1']
		this.properties << ['processFieldNames':'fromFile']
		this.properties << ['headerRow':'1']
		this.inputFileName = 'Excel2XML_Scenario4.xlsx'

		when:
		process()

		then:
		ConverterException e = thrown()
		e.message == 'Invalid character in XML element name: Or<der'
	}
}