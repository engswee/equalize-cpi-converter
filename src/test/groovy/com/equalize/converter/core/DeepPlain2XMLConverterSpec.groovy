package com.equalize.converter.core

import org.apache.camel.CamelContext
import org.apache.camel.Exchange
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.impl.DefaultExchange

import com.equalize.converter.core.util.ConverterException
import com.equalize.cpi.converter.FormatConversionBean

import spock.lang.Specification
import spock.lang.Unroll

class DeepPlain2XMLConverterSpec extends Specification {
	static final String filePath = 'src/test/resources/FCC'

	Exchange exchange
	Map<String,Object> properties

	String inputFileName
	String outputFileName
	File expectedOutputFile

	def setup() {
		// Setup the Camel context, Camel exchange
		CamelContext context = new DefaultCamelContext()
		this.exchange = new DefaultExchange(context)
		this.properties = ['converterClass':'com.equalize.converter.core.DeepPlain2XMLConverter']
	}

	private String process() {
		this.exchange.getIn().setBody(new File("$filePath/$inputFileName"))
		this.expectedOutputFile = new File("$filePath/$outputFileName")

		def fcb = new FormatConversionBean(this.exchange, properties)
		byte[] output = fcb.convert()

		return new String(output, 'UTF-8')
	}

	// Reference - https://blogs.sap.com/2015/03/11/deepfccbean-the-better-fcc-at-meeting-your-deep-structure-needs-part-2-flat-file-to-deep-xml/

	@Unroll
	def 'Plain > XML - exception is thrown when #fieldName is not configured'(String fieldName) {
		given:
		this.properties << ['documentName':'MT_DeepPlain2XML']
		this.properties << ['documentNamespace':'urn:equalize:com']
		this.properties << ['recordsetStructure':'Header']
		this.properties << ['Header.parent':'Root']
		this.properties << ['Header.fieldNames':'Key,Line']
		this.properties << ['keyFieldName':'Key']
		this.properties << ['Header.keyFieldValue':'H']
		this.properties << ['defaultFieldSeparator':',']

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
		'recordsetStructure' | _
		'Header.parent' | _
		'Header.fieldNames' | _
		'keyFieldName' | _
		'Header.keyFieldValue' | _
	}

	def 'Plain > XML - exception is thrown when defaultFieldSeparator, fieldSeparator, or fieldFixedLengths are not configured'() {
		given:
		this.properties << ['documentName':'MT_DeepPlain2XML']
		this.properties << ['documentNamespace':'urn:equalize:com']
		this.properties << ['recordsetStructure':'Header']

		when:
		process()

		then:
		ConverterException e = thrown()
		e.message == "'defaultFieldSeparator', 'Header.fieldSeparator' or 'Header.fieldFixedLengths' must be populated"
	}

	def 'Plain > XML - exception is thrown when both fieldSeparator and fieldFixedLengths are configured'() {
		given:
		this.properties << ['documentName':'MT_DeepPlain2XML']
		this.properties << ['documentNamespace':'urn:equalize:com']
		this.properties << ['recordsetStructure':'Header']
		this.properties << ['Header.parent':'Root']
		this.properties << ['Header.fieldNames':'Key,Line']
		this.properties << ['keyFieldName':'Key']
		this.properties << ['Header.keyFieldValue':'H']
		this.properties << ['Header.fieldSeparator':',']
		this.properties << ['Header.fieldFixedLengths':'5,10']

		when:
		process()

		then:
		ConverterException e = thrown()
		e.message == "Use only parameter 'Header.fieldSeparator'/'defaultFieldSeparator' or 'Header.fieldFixedLengths', not both"
	}

	def 'Plain > XML - exception is thrown when both defaultFieldSeparator and fieldFixedLengths are configured'() {
		given:
		this.properties << ['documentName':'MT_DeepPlain2XML']
		this.properties << ['documentNamespace':'urn:equalize:com']
		this.properties << ['recordsetStructure':'Header']
		this.properties << ['Header.parent':'Root']
		this.properties << ['Header.fieldNames':'Key,Line']
		this.properties << ['keyFieldName':'Key']
		this.properties << ['Header.keyFieldValue':'H']
		this.properties << ['defaultFieldSeparator':',']
		this.properties << ['Header.fieldFixedLengths':'5,10']

		when:
		process()

		then:
		ConverterException e = thrown()
		e.message == "Use only parameter 'Header.fieldSeparator'/'defaultFieldSeparator' or 'Header.fieldFixedLengths', not both"
	}

	def 'Plain > XML - exception is thrown when fieldFixedLengths contain non digit values'() {
		given:
		this.properties << ['documentName':'MT_DeepPlain2XML']
		this.properties << ['documentNamespace':'urn:equalize:com']
		this.properties << ['recordsetStructure':'Header']
		this.properties << ['Header.parent':'Root']
		this.properties << ['Header.fieldNames':'Key,Line']
		this.properties << ['keyFieldName':'Key']
		this.properties << ['Header.keyFieldValue':'H']
		this.properties << ['Header.fieldFixedLengths':'5,10,X']

		when:
		process()

		then:
		ConverterException e = thrown()
		e.message == "Maintain only integers separated by commas for 'Header.fieldFixedLengths'"
	}

	def 'Plain > XML - exception is thrown when no of fields in fieldNames and fieldFixedLengths do not match'() {
		given:
		this.properties << ['documentName':'MT_DeepPlain2XML']
		this.properties << ['documentNamespace':'urn:equalize:com']
		this.properties << ['recordsetStructure':'Header']
		this.properties << ['Header.parent':'Root']
		this.properties << ['Header.fieldNames':'Key,Line,Value']
		this.properties << ['keyFieldName':'Key']
		this.properties << ['Header.keyFieldValue':'H']
		this.properties << ['Header.fieldFixedLengths':'5,10']

		when:
		process()

		then:
		ConverterException e = thrown()
		e.message == "No. of fields in 'fieldNames' and 'fieldFixedLengths' do not match for record type = 'Header'"
	}

	def 'Plain > XML - exception is thrown when duplicate found in fieldNames'() {
		given:
		this.properties << ['documentName':'MT_DeepPlain2XML']
		this.properties << ['documentNamespace':'urn:equalize:com']
		this.properties << ['recordsetStructure':'Header']
		this.properties << ['Header.parent':'Root']
		this.properties << ['Header.fieldNames':'Key,Line,Line']
		this.properties << ['keyFieldName':'Key']
		this.properties << ['Header.keyFieldValue':'H']
		this.properties << ['defaultFieldSeparator':',']

		when:
		process()

		then:
		ConverterException e = thrown()
		e.message == "Duplicate field found in 'Header.fieldNames': Line"
	}

	def 'Plain > XML - exception is thrown when key field not found in fieldNames'() {
		given:
		this.properties << ['documentName':'MT_DeepPlain2XML']
		this.properties << ['documentNamespace':'urn:equalize:com']
		this.properties << ['recordsetStructure':'Header']
		this.properties << ['Header.parent':'Root']
		this.properties << ['Header.fieldNames':'Field1,Field2']
		this.properties << ['keyFieldName':'Key']
		this.properties << ['Header.keyFieldValue':'H']
		this.properties << ['defaultFieldSeparator':',']

		when:
		process()

		then:
		ConverterException e = thrown()
		e.message == "Key field 'Key' not found in 'Header.fieldNames'"
	}

	def 'Plain > XML - exception is thrown when duplicate found in recordsetStructure'() {
		given:
		this.properties << ['documentName':'MT_DeepPlain2XML']
		this.properties << ['documentNamespace':'urn:equalize:com']
		this.properties << ['recordsetStructure':'Header,Header']
		this.properties << ['Header.parent':'Root']
		this.properties << ['Header.fieldNames':'Key,Line']
		this.properties << ['keyFieldName':'Key']
		this.properties << ['Header.keyFieldValue':'H']
		this.properties << ['defaultFieldSeparator':',']

		when:
		process()

		then:
		ConverterException e = thrown()
		e.message == "Duplicate field found in 'recordsetStructure': Header"
	}

	def 'Plain > XML - exception is thrown when reserve name Root is configured in recordsetStructure'() {
		given:
		this.properties << ['documentName':'MT_DeepPlain2XML']
		this.properties << ['documentNamespace':'urn:equalize:com']
		this.properties << ['recordsetStructure':'Root']
		this.properties << ['Root.parent':'Root']
		this.properties << ['Root.fieldNames':'Key,Line']
		this.properties << ['keyFieldName':'Key']
		this.properties << ['Root.keyFieldValue':'H']
		this.properties << ['defaultFieldSeparator':',']

		when:
		process()

		then:
		ConverterException e = thrown()
		e.message == "'Root' is a reserved name and not allowed in parameter 'recordsetStructure'"
	}

	def 'Plain > XML - exception is thrown when parent is same as record type'() {
		given:
		this.properties << ['documentName':'MT_DeepPlain2XML']
		this.properties << ['documentNamespace':'urn:equalize:com']
		this.properties << ['recordsetStructure':'Header,Detail']
		this.properties << ['Header.parent':'Root']
		this.properties << ['Header.fieldNames':'Key,Line']
		this.properties << ['keyFieldName':'Key']
		this.properties << ['Header.keyFieldValue':'H']
		this.properties << ['Detail.parent':'Detail']
		this.properties << ['Detail.fieldNames':'Key,DetailLine']
		this.properties << ['Detail.keyFieldValue':'D']
		this.properties << ['defaultFieldSeparator':',']

		when:
		process()

		then:
		ConverterException e = thrown()
		e.message == "Value in 'Detail.parent' cannot be the same as substructure name"
	}

	def 'Plain > XML - exception is thrown when invalid parent specified'() {
		given:
		this.properties << ['documentName':'MT_DeepPlain2XML']
		this.properties << ['documentNamespace':'urn:equalize:com']
		this.properties << ['recordsetStructure':'Header,Detail']
		this.properties << ['Header.parent':'Root']
		this.properties << ['Header.fieldNames':'Key,Line']
		this.properties << ['keyFieldName':'Key']
		this.properties << ['Header.keyFieldValue':'H']
		this.properties << ['Detail.parent':'Parent']
		this.properties << ['Detail.fieldNames':'Key,DetailLine']
		this.properties << ['Detail.keyFieldValue':'D']
		this.properties << ['defaultFieldSeparator':',']

		when:
		process()

		then:
		ConverterException e = thrown()
		e.message == "Value 'Parent' in 'Detail.parent' not found in parameter 'recordsetStructure'"
	}

	def 'Plain > XML - Comma/Tab input with enclosure sign and conversion and row offset'() {
		given:
		this.properties << ['documentName':'MT_DeepPlain2XML']
		this.properties << ['documentNamespace':'urn:equalize:com']
		this.properties << ['recordsetStructure':'Delivery,Order,Item']
		this.properties << ['keyFieldName':'Type']
		this.properties << ['Delivery.fieldNames':'Type,DeliveryNo']
		this.properties << ['Order.fieldNames':'Type,DeliveryNo,OrderNo']
		this.properties << ['Item.fieldNames':'Type,OrderNo,ItemNo,Quantity']
		this.properties << ['Delivery.keyFieldValue':'D']
		this.properties << ['Order.keyFieldValue':'O']
		this.properties << ['Item.keyFieldValue':'I']
		this.properties << ['Delivery.parent':'Root']
		this.properties << ['Order.parent':'Delivery']
		this.properties << ['Item.parent':'Order']
		this.properties << ['Delivery.fieldSeparator':"'0x09'"]
		this.properties << ['defaultFieldSeparator':',']
		this.properties << ['Order.enclosureSignBegin':'"']
		this.properties << ['Order.enclosureSignBeginEscape':'""']
		this.properties << ['Item.enclosureConversion':'N']
		this.properties << ['Item.enclosureSignBegin':'"']
		this.properties << ['rowOffset':'1']
		this.properties << ['indentFactor':'2']
		this.inputFileName = 'DeepPlain2XML_Scenario1.txt'
		this.outputFileName = 'DeepPlain2XML_Scenario1_output.xml'

		expect:
		process() == this.expectedOutputFile.getText('UTF-8')
	}

	def 'Plain > XML - exception is thrown when record type cannot be determined from input file'() {
		given:
		this.properties << ['documentName':'MT_DeepPlain2XML']
		this.properties << ['documentNamespace':'urn:equalize:com']
		this.properties << ['recordsetStructure':'Count']
		this.properties << ['keyFieldName':'Type']
		this.properties << ['Count.fieldNames':'Type,Count']
		this.properties << ['Count.keyFieldValue':'COUNT']
		this.properties << ['Count.parent':'Root']
		this.properties << ['Count.fieldSeparator':':']
		this.inputFileName = 'DeepPlain2XML_Scenario5a.txt'
		when:
		process()

		then:
		ConverterException e = thrown()
		e.message == "Unable to determine record type for line 1"
	}

	def 'Plain > XML - Tab delimited input with default separator'() {
		given:
		this.properties << ['documentName':'Test']
		this.properties << ['documentNamespace':'http://test.com']
		this.properties << ['recordsetStructure':'SatzartWB']
		this.properties << ['keyFieldName':'Satzart']
		this.properties << ['SatzartWB.fieldNames':'Satzart,Field2,Field3,Field4,Field5,Field6,Field7,Field8,Field9,Field10,Field11,Field12,Field13,Field14,Field15,Field16,Field17']
		this.properties << ['SatzartWB.keyFieldValue':'WB']
		this.properties << ['SatzartWB.parent':'Root']
		this.properties << ['defaultFieldSeparator':"'0x09'"]
		this.properties << ['indentFactor':'2']
		this.inputFileName = 'DeepPlain2XML_Scenario6.txt'
		this.outputFileName = 'DeepPlain2XML_Scenario6_output.xml'

		expect:
		process() == this.expectedOutputFile.getText('UTF-8')
	}

	def 'Plain > XML - Fixed length input without indent'() {
		given:
		this.properties << ['documentName':'MT_DeepPlain2XML']
		this.properties << ['documentNamespace':'urn:equalize:com']
		this.properties << ['recordsetStructure':'Header,Delivery,Order,Item']
		this.properties << ['keyFieldName':'Type']
		this.properties << ['Header.fieldNames':'Type,FileName']
		this.properties << ['Delivery.fieldNames':'Type,DeliveryNo']
		this.properties << ['Order.fieldNames':'Type,DeliveryNo,OrderNo']
		this.properties << ['Item.fieldNames':'Type,OrderNo,ItemNo,Quantity']
		this.properties << ['Header.keyFieldValue':'H']
		this.properties << ['Delivery.keyFieldValue':'D']
		this.properties << ['Order.keyFieldValue':'O']
		this.properties << ['Item.keyFieldValue':'I']
		this.properties << ['Header.parent':'Root']
		this.properties << ['Delivery.parent':'Root']
		this.properties << ['Order.parent':'Delivery']
		this.properties << ['Item.parent':'Order']
		this.properties << ['Header.fieldFixedLengths':'5,20']
		this.properties << ['Delivery.fieldFixedLengths':'5,10']
		this.properties << ['Order.fieldFixedLengths':'5,10,10']
		this.properties << ['Item.fieldFixedLengths':'5,10,10,10']
		this.properties << ['trimContents':'N']
		this.inputFileName = 'DeepPlain2XML_Scenario2.txt'
		this.outputFileName = 'DeepPlain2XML_Scenario2_output.xml'

		expect:
		process() == this.expectedOutputFile.getText('UTF-8')
	}

	def 'Plain > XML - Fixed length input with key field not in first position'() {
		given:
		this.properties << ['documentName':'MT_DeepPlain2XML']
		this.properties << ['documentNamespace':'urn:equalize:com']
		this.properties << ['recordsetStructure':'Header,Delivery,Order,Item']
		this.properties << ['keyFieldName':'Type']
		this.properties << ['Header.fieldNames':'FileName,Type']
		this.properties << ['Delivery.fieldNames':'Type,DeliveryNo']
		this.properties << ['Order.fieldNames':'Type,DeliveryNo,OrderNo']
		this.properties << ['Item.fieldNames':'Type,OrderNo,ItemNo,Quantity']
		this.properties << ['Header.keyFieldValue':'H']
		this.properties << ['Delivery.keyFieldValue':'D']
		this.properties << ['Order.keyFieldValue':'O']
		this.properties << ['Item.keyFieldValue':'I']
		this.properties << ['Header.parent':'Root']
		this.properties << ['Delivery.parent':'Root']
		this.properties << ['Order.parent':'Delivery']
		this.properties << ['Item.parent':'Order']
		this.properties << ['Header.fieldFixedLengths':'20,5']
		this.properties << ['Delivery.fieldFixedLengths':'5,10']
		this.properties << ['Order.fieldFixedLengths':'5,10,10']
		this.properties << ['Item.fieldFixedLengths':'5,10,10,10']
		this.properties << ['trimContents':'N']
		this.inputFileName = 'DeepPlain2XML_Scenario2a.txt'
		this.outputFileName = 'DeepPlain2XML_Scenario2a_output.xml'

		expect:
		process() == this.expectedOutputFile.getText('UTF-8')
	}

	def 'Plain > XML - Fixed length input with contents not trimmed'() {
		given:
		this.properties << ['documentName':'MT_DeepPlain2XML']
		this.properties << ['documentNamespace':'urn:equalize:com']
		this.properties << ['recordsetStructure':'Header,Delivery,Order,Item']
		this.properties << ['keyFieldName':'Type']
		this.properties << ['Header.fieldNames':'Type,FileName']
		this.properties << ['Delivery.fieldNames':'Type,DeliveryNo']
		this.properties << ['Order.fieldNames':'Type,DeliveryNo']
		this.properties << ['Item.fieldNames':'Type,OrderNo']
		this.properties << ['Header.keyFieldValue':'CR']
		this.properties << ['Delivery.keyFieldValue':'DH']
		this.properties << ['Order.keyFieldValue':'DD']
		this.properties << ['Item.keyFieldValue':'TR']
		this.properties << ['Header.parent':'Root']
		this.properties << ['Delivery.parent':'Root']
		this.properties << ['Order.parent':'Delivery']
		this.properties << ['Item.parent':'Root']
		this.properties << ['Header.fieldFixedLengths':'2,15']
		this.properties << ['Delivery.fieldFixedLengths':'2,15']
		this.properties << ['Order.fieldFixedLengths':'2,15']
		this.properties << ['Item.fieldFixedLengths':'2,15']
		this.properties << ['trimContents':'N']
		this.properties << ['indentFactor':'2']
		this.inputFileName = 'DeepPlain2XML_Scenario4.txt'
		this.outputFileName = 'DeepPlain2XML_Scenario4_output.xml'

		expect:
		process() == this.expectedOutputFile.getText('UTF-8')
	}

	def 'Plain > XML - Fixed length input with generic record type'() {
		given:
		this.properties << ['documentName':'MT_DeepPlain2XML']
		this.properties << ['documentNamespace':'urn:equalize:com']
		this.properties << ['recordsetStructure':'Header,Detail,Footer']
		this.properties << ['genericRecordType':'Detail']
		this.properties << ['keyFieldName':'Type']
		this.properties << ['Header.fieldNames':'Type,Invoice']
		this.properties << ['Detail.fieldNames':'Plant,Text']
		this.properties << ['Footer.fieldNames':'Type,Count']
		this.properties << ['Header.keyFieldValue':'CR']
		this.properties << ['Footer.keyFieldValue':'TR']
		this.properties << ['Header.parent':'Root']
		this.properties << ['Detail.parent':'Root']
		this.properties << ['Footer.parent':'Root']
		this.properties << ['Header.fieldFixedLengths':'2,10']
		this.properties << ['Detail.fieldFixedLengths':'4,100']
		this.properties << ['Footer.fieldFixedLengths':'2,10']
		this.properties << ['indentFactor':'2']
		this.inputFileName = 'DeepPlain2XML_Scenario5b.txt'
		this.outputFileName = 'DeepPlain2XML_Scenario5b_output.xml'

		expect:
		process() == this.expectedOutputFile.getText('UTF-8')
	}

	def 'Plain > XML - Fixed length input with missing last fields - ignore'() {
		given:
		this.properties << ['documentName':'MT_DeepPlain2XML']
		this.properties << ['documentNamespace':'urn:equalize:com']
		this.properties << ['recordsetStructure':'Header,Delivery,Order,Item']
		this.properties << ['keyFieldName':'Type']
		this.properties << ['Header.fieldNames':'Type,FileName']
		this.properties << ['Delivery.fieldNames':'Type,DeliveryNo,Dummy']
		this.properties << ['Order.fieldNames':'Type,DeliveryNo']
		this.properties << ['Item.fieldNames':'Type,OrderNo,ItemNo,Quantity']
		this.properties << ['Header.keyFieldValue':'H']
		this.properties << ['Delivery.keyFieldValue':'D']
		this.properties << ['Order.keyFieldValue':'O']
		this.properties << ['Item.keyFieldValue':'I']
		this.properties << ['Header.parent':'Root']
		this.properties << ['Delivery.parent':'Root']
		this.properties << ['Order.parent':'Delivery']
		this.properties << ['Item.parent':'Order']
		this.properties << ['Header.fieldFixedLengths':'5,20']
		this.properties << ['Delivery.fieldFixedLengths':'5,10,10']
		this.properties << ['Order.fieldFixedLengths':'5,10']
		this.properties << ['Item.fieldFixedLengths':'5,10,10,10']
		this.properties << ['trimContents':'N']
		this.properties << ['indentFactor':'2']
		this.inputFileName = 'DeepPlain2XML_Scenario7.txt'
		this.outputFileName = 'DeepPlain2XML_Scenario7_output_missing_ignore.xml'

		expect:
		process() == this.expectedOutputFile.getText('UTF-8')
	}

	def 'Plain > XML - Fixed length input with missing last fields - add'() {
		given:
		this.properties << ['documentName':'MT_DeepPlain2XML']
		this.properties << ['documentNamespace':'urn:equalize:com']
		this.properties << ['recordsetStructure':'Header,Delivery,Order,Item']
		this.properties << ['keyFieldName':'Type']
		this.properties << ['Header.fieldNames':'Type,FileName']
		this.properties << ['Delivery.fieldNames':'Type,DeliveryNo,Dummy']
		this.properties << ['Order.fieldNames':'Type,DeliveryNo']
		this.properties << ['Item.fieldNames':'Type,OrderNo,ItemNo,Quantity']
		this.properties << ['Header.keyFieldValue':'H']
		this.properties << ['Delivery.keyFieldValue':'D']
		this.properties << ['Order.keyFieldValue':'O']
		this.properties << ['Item.keyFieldValue':'I']
		this.properties << ['Header.parent':'Root']
		this.properties << ['Delivery.parent':'Root']
		this.properties << ['Order.parent':'Delivery']
		this.properties << ['Item.parent':'Order']
		this.properties << ['Header.fieldFixedLengths':'5,20']
		this.properties << ['Delivery.fieldFixedLengths':'5,10,10']
		this.properties << ['Order.fieldFixedLengths':'5,10']
		this.properties << ['Item.fieldFixedLengths':'5,10,10,10']
		this.properties << ['trimContents':'N']
		this.properties << ['indentFactor':'2']
		this.properties << ['Delivery.missingLastFields':'add']
		this.inputFileName = 'DeepPlain2XML_Scenario7.txt'
		this.outputFileName = 'DeepPlain2XML_Scenario7_output_missing_add.xml'

		expect:
		process() == this.expectedOutputFile.getText('UTF-8')
	}

	def 'Plain > XML - Fixed length input with missing last fields - error'() {
		given:
		this.properties << ['documentName':'MT_DeepPlain2XML']
		this.properties << ['documentNamespace':'urn:equalize:com']
		this.properties << ['recordsetStructure':'Header,Delivery,Order,Item']
		this.properties << ['keyFieldName':'Type']
		this.properties << ['Header.fieldNames':'Type,FileName']
		this.properties << ['Delivery.fieldNames':'Type,DeliveryNo,Dummy']
		this.properties << ['Order.fieldNames':'Type,DeliveryNo']
		this.properties << ['Item.fieldNames':'Type,OrderNo,ItemNo,Quantity']
		this.properties << ['Header.keyFieldValue':'H']
		this.properties << ['Delivery.keyFieldValue':'D']
		this.properties << ['Order.keyFieldValue':'O']
		this.properties << ['Item.keyFieldValue':'I']
		this.properties << ['Header.parent':'Root']
		this.properties << ['Delivery.parent':'Root']
		this.properties << ['Order.parent':'Delivery']
		this.properties << ['Item.parent':'Order']
		this.properties << ['Header.fieldFixedLengths':'5,20']
		this.properties << ['Delivery.fieldFixedLengths':'5,10,10']
		this.properties << ['Order.fieldFixedLengths':'5,10']
		this.properties << ['Item.fieldFixedLengths':'5,10,10,10']
		this.properties << ['trimContents':'N']
		this.properties << ['indentFactor':'2']
		this.properties << ['Delivery.missingLastFields':'error']
		this.inputFileName = 'DeepPlain2XML_Scenario7.txt'

		when:
		process()

		then:
		ConverterException e = thrown()
		e.message == "Line 2 has less fields than configured"
	}

	def 'Plain > XML - Fixed length input with additional last fields - ignore'() {
		given:
		this.properties << ['documentName':'MT_DeepPlain2XML']
		this.properties << ['documentNamespace':'urn:equalize:com']
		this.properties << ['recordsetStructure':'Header,Delivery,Order,Item']
		this.properties << ['keyFieldName':'Type']
		this.properties << ['Header.fieldNames':'Type,FileName']
		this.properties << ['Delivery.fieldNames':'Type,DeliveryNo']
		this.properties << ['Order.fieldNames':'Type']
		this.properties << ['Item.fieldNames':'Type,OrderNo,ItemNo,Quantity']
		this.properties << ['Header.keyFieldValue':'H']
		this.properties << ['Delivery.keyFieldValue':'D']
		this.properties << ['Order.keyFieldValue':'O']
		this.properties << ['Item.keyFieldValue':'I']
		this.properties << ['Header.parent':'Root']
		this.properties << ['Delivery.parent':'Root']
		this.properties << ['Order.parent':'Delivery']
		this.properties << ['Item.parent':'Order']
		this.properties << ['Header.fieldFixedLengths':'5,20']
		this.properties << ['Delivery.fieldFixedLengths':'5,10']
		this.properties << ['Order.fieldFixedLengths':'5']
		this.properties << ['Item.fieldFixedLengths':'5,10,10,10']
		this.properties << ['trimContents':'N']
		this.properties << ['indentFactor':'2']
		this.inputFileName = 'DeepPlain2XML_Scenario7.txt'
		this.outputFileName = 'DeepPlain2XML_Scenario7_output_additional_ignore.xml'

		expect:
		process() == this.expectedOutputFile.getText('UTF-8')
	}

	def 'Plain > XML - Fixed length input with additional last fields - error'() {
		given:
		this.properties << ['documentName':'MT_DeepPlain2XML']
		this.properties << ['documentNamespace':'urn:equalize:com']
		this.properties << ['recordsetStructure':'Header,Delivery,Order,Item']
		this.properties << ['keyFieldName':'Type']
		this.properties << ['Header.fieldNames':'Type,FileName']
		this.properties << ['Delivery.fieldNames':'Type,DeliveryNo']
		this.properties << ['Order.fieldNames':'Type']
		this.properties << ['Item.fieldNames':'Type,OrderNo,ItemNo,Quantity']
		this.properties << ['Header.keyFieldValue':'H']
		this.properties << ['Delivery.keyFieldValue':'D']
		this.properties << ['Order.keyFieldValue':'O']
		this.properties << ['Item.keyFieldValue':'I']
		this.properties << ['Header.parent':'Root']
		this.properties << ['Delivery.parent':'Root']
		this.properties << ['Order.parent':'Delivery']
		this.properties << ['Item.parent':'Order']
		this.properties << ['Header.fieldFixedLengths':'5,20']
		this.properties << ['Delivery.fieldFixedLengths':'5,10']
		this.properties << ['Order.fieldFixedLengths':'5']
		this.properties << ['Item.fieldFixedLengths':'5,10,10,10']
		this.properties << ['trimContents':'N']
		this.properties << ['indentFactor':'2']
		this.properties << ['Order.additionalLastFields':'error']
		this.inputFileName = 'DeepPlain2XML_Scenario7.txt'

		when:
		process()

		then:
		ConverterException e = thrown()
		e.message == "Line 3 has more fields than configured"
	}

	def 'Plain > XML - CSV input with key field missing in input'() {
		given:
		this.properties << ['documentName':'MT_DeepPlain2XML']
		this.properties << ['documentNamespace':'urn:equalize:com']
		this.properties << ['recordsetStructure':'Header,Delivery,Order,OrderText,Item,Footer']
		this.properties << ['keyFieldName':'Type']
		this.properties << ['Header.fieldNames':'Dummy,FileName,Type']
		this.properties << ['Delivery.fieldNames':'Type,DeliveryNo']
		this.properties << ['Order.fieldNames':'Type,DeliveryNo,OrderNo']
		this.properties << ['OrderText.fieldNames':'Type,OrderNo,TextValue']
		this.properties << ['Item.fieldNames':'Type,OrderNo,ItemNo,Quantity']
		this.properties << ['Footer.fieldNames':'Type,DeliveryCount']
		this.properties << ['Header.keyFieldValue':'H']
		this.properties << ['Delivery.keyFieldValue':'D']
		this.properties << ['Order.keyFieldValue':'O']
		this.properties << ['OrderText.keyFieldValue':'T']
		this.properties << ['Item.keyFieldValue':'I']
		this.properties << ['Footer.keyFieldValue':'F']
		this.properties << ['Header.parent':'Root']
		this.properties << ['Delivery.parent':'Root']
		this.properties << ['Order.parent':'Delivery']
		this.properties << ['OrderText.parent':'Order']
		this.properties << ['Item.parent':'Order']
		this.properties << ['Footer.parent':'Root']
		this.properties << ['defaultFieldSeparator':',']
		this.inputFileName = 'DeepPlain2XML_Scenario3.txt'

		when:
		process()

		then:
		ConverterException e = thrown()
		e.message == "Unable to determine record type for line 1"
	}

	def 'Plain > XML - CSV input with generic record type'() {
		given:
		this.properties << ['documentName':'MT_DeepPlain2XML']
		this.properties << ['documentNamespace':'urn:equalize:com']
		this.properties << ['recordsetStructure':'Line,Count']
		this.properties << ['genericRecordType':'Line']
		this.properties << ['keyFieldName':'Type']
		this.properties << ['Line.fieldNames':'FirstName,LastName,PostCode,Date']
		this.properties << ['Count.fieldNames':'Type,Count']
		this.properties << ['Count.keyFieldValue':'COUNT']
		this.properties << ['Line.parent':'Root']
		this.properties << ['Count.parent':'Root']
		this.properties << ['Line.fieldSeparator':';']
		this.properties << ['Count.fieldSeparator':':']
		this.properties << ['indentFactor':'2']
		this.inputFileName = 'DeepPlain2XML_Scenario5a.txt'
		this.outputFileName = 'DeepPlain2XML_Scenario5a_output.xml'

		expect:
		process() == this.expectedOutputFile.getText('UTF-8')
	}

	def 'Plain > XML - CSV input with generic record type and endSeparator'() {
		given:
		this.properties << ['documentName':'MT_DeepPlain2XML']
		this.properties << ['documentNamespace':'urn:equalize:com']
		this.properties << ['recordsetStructure':'Line']
		this.properties << ['genericRecordType':'Line']
		this.properties << ['Line.fieldNames':'Key,Value']
		this.properties << ['Line.parent':'Root']
		this.properties << ['Line.fieldSeparator':'=']
		this.properties << ['indentFactor':'2']
		this.properties << ['endSeparator':'&']
		this.inputFileName = 'DeepPlain2XML_Scenario8.txt'
		this.outputFileName = 'DeepPlain2XML_Scenario8_output.xml'

		expect:
		process() == this.expectedOutputFile.getText('UTF-8')
	}

	def 'Plain > XML - CSV input with missing last fields - ignore'() {
		given:
		this.properties << ['documentName':'MT_DeepPlain2XML']
		this.properties << ['documentNamespace':'urn:equalize:com']
		this.properties << ['recordsetStructure':'Header,Delivery,Order,OrderText,Item,Footer']
		this.properties << ['keyFieldName':'Type']
		this.properties << ['Header.fieldNames':'Type,FileName,MoreField']
		this.properties << ['Delivery.fieldNames':'Type,DeliveryNo']
		this.properties << ['Order.fieldNames':'Type,DeliveryNo,OrderNo']
		this.properties << ['OrderText.fieldNames':'Type,OrderNo,TextValue']
		this.properties << ['Item.fieldNames':'Type,OrderNo,ItemNo,Quantity']
		this.properties << ['Footer.fieldNames':'Type,DeliveryCount']
		this.properties << ['Header.keyFieldValue':'H']
		this.properties << ['Delivery.keyFieldValue':'D']
		this.properties << ['Order.keyFieldValue':'O']
		this.properties << ['OrderText.keyFieldValue':'T']
		this.properties << ['Item.keyFieldValue':'I']
		this.properties << ['Footer.keyFieldValue':'F']
		this.properties << ['Header.parent':'Root']
		this.properties << ['Delivery.parent':'Root']
		this.properties << ['Order.parent':'Delivery']
		this.properties << ['OrderText.parent':'Order']
		this.properties << ['Item.parent':'Order']
		this.properties << ['Footer.parent':'Root']
		this.properties << ['defaultFieldSeparator':',']
		this.properties << ['indentFactor':'2']
		this.inputFileName = 'DeepPlain2XML_Scenario3.txt'
		this.outputFileName = 'DeepPlain2XML_Scenario3_output_missing_ignore.xml'

		expect:
		process() == this.expectedOutputFile.getText('UTF-8')
	}

	def 'Plain > XML - CSV input with missing last fields - add'() {
		given:
		this.properties << ['documentName':'MT_DeepPlain2XML']
		this.properties << ['documentNamespace':'urn:equalize:com']
		this.properties << ['recordsetStructure':'Header,Delivery,Order,OrderText,Item,Footer']
		this.properties << ['keyFieldName':'Type']
		this.properties << ['Header.fieldNames':'Type,FileName,MoreField']
		this.properties << ['Delivery.fieldNames':'Type,DeliveryNo']
		this.properties << ['Order.fieldNames':'Type,DeliveryNo,OrderNo']
		this.properties << ['OrderText.fieldNames':'Type,OrderNo,TextValue']
		this.properties << ['Item.fieldNames':'Type,OrderNo,ItemNo,Quantity']
		this.properties << ['Footer.fieldNames':'Type,DeliveryCount']
		this.properties << ['Header.keyFieldValue':'H']
		this.properties << ['Delivery.keyFieldValue':'D']
		this.properties << ['Order.keyFieldValue':'O']
		this.properties << ['OrderText.keyFieldValue':'T']
		this.properties << ['Item.keyFieldValue':'I']
		this.properties << ['Footer.keyFieldValue':'F']
		this.properties << ['Header.parent':'Root']
		this.properties << ['Delivery.parent':'Root']
		this.properties << ['Order.parent':'Delivery']
		this.properties << ['OrderText.parent':'Order']
		this.properties << ['Item.parent':'Order']
		this.properties << ['Footer.parent':'Root']
		this.properties << ['defaultFieldSeparator':',']
		this.properties << ['indentFactor':'2']
		this.properties << ['Header.missingLastFields':'add']
		this.inputFileName = 'DeepPlain2XML_Scenario3.txt'
		this.outputFileName = 'DeepPlain2XML_Scenario3_output_missing_add.xml'

		expect:
		process() == this.expectedOutputFile.getText('UTF-8')
	}

	def 'Plain > XML - CSV input with missing last fields - error'() {
		given:
		this.properties << ['documentName':'MT_DeepPlain2XML']
		this.properties << ['documentNamespace':'urn:equalize:com']
		this.properties << ['recordsetStructure':'Header,Delivery,Order,OrderText,Item,Footer']
		this.properties << ['keyFieldName':'Type']
		this.properties << ['Header.fieldNames':'Type,FileName,MoreField']
		this.properties << ['Delivery.fieldNames':'Type,DeliveryNo']
		this.properties << ['Order.fieldNames':'Type,DeliveryNo,OrderNo']
		this.properties << ['OrderText.fieldNames':'Type,OrderNo,TextValue']
		this.properties << ['Item.fieldNames':'Type,OrderNo,ItemNo,Quantity']
		this.properties << ['Footer.fieldNames':'Type,DeliveryCount']
		this.properties << ['Header.keyFieldValue':'H']
		this.properties << ['Delivery.keyFieldValue':'D']
		this.properties << ['Order.keyFieldValue':'O']
		this.properties << ['OrderText.keyFieldValue':'T']
		this.properties << ['Item.keyFieldValue':'I']
		this.properties << ['Footer.keyFieldValue':'F']
		this.properties << ['Header.parent':'Root']
		this.properties << ['Delivery.parent':'Root']
		this.properties << ['Order.parent':'Delivery']
		this.properties << ['OrderText.parent':'Order']
		this.properties << ['Item.parent':'Order']
		this.properties << ['Footer.parent':'Root']
		this.properties << ['defaultFieldSeparator':',']
		this.properties << ['indentFactor':'2']
		this.properties << ['Header.missingLastFields':'error']
		this.inputFileName = 'DeepPlain2XML_Scenario3.txt'

		when:
		process()

		then:
		ConverterException e = thrown()
		e.message == "Line 1 has less fields than configured"
	}

	def 'Plain > XML - CSV input with additional last fields - ignore'() {
		given:
		this.properties << ['documentName':'MT_DeepPlain2XML']
		this.properties << ['documentNamespace':'urn:equalize:com']
		this.properties << ['recordsetStructure':'Header,Delivery,Order,OrderText,Item,Footer']
		this.properties << ['keyFieldName':'Type']
		this.properties << ['Header.fieldNames':'Type,FileName']
		this.properties << ['Delivery.fieldNames':'Type']
		this.properties << ['Order.fieldNames':'Type,DeliveryNo,OrderNo']
		this.properties << ['OrderText.fieldNames':'Type,OrderNo,TextValue']
		this.properties << ['Item.fieldNames':'Type,OrderNo,ItemNo,Quantity']
		this.properties << ['Footer.fieldNames':'Type,DeliveryCount']
		this.properties << ['Header.keyFieldValue':'H']
		this.properties << ['Delivery.keyFieldValue':'D']
		this.properties << ['Order.keyFieldValue':'O']
		this.properties << ['OrderText.keyFieldValue':'T']
		this.properties << ['Item.keyFieldValue':'I']
		this.properties << ['Footer.keyFieldValue':'F']
		this.properties << ['Header.parent':'Root']
		this.properties << ['Delivery.parent':'Root']
		this.properties << ['Order.parent':'Delivery']
		this.properties << ['OrderText.parent':'Order']
		this.properties << ['Item.parent':'Order']
		this.properties << ['Footer.parent':'Root']
		this.properties << ['defaultFieldSeparator':',']
		this.properties << ['indentFactor':'2']
		this.inputFileName = 'DeepPlain2XML_Scenario3.txt'
		this.outputFileName = 'DeepPlain2XML_Scenario3_output_additional_ignore.xml'

		expect:
		process() == this.expectedOutputFile.getText('UTF-8')

	}

	def 'Plain > XML - CSV input with additional last fields - error'() {
		given:
		this.properties << ['documentName':'MT_DeepPlain2XML']
		this.properties << ['documentNamespace':'urn:equalize:com']
		this.properties << ['recordsetStructure':'Header,Delivery,Order,OrderText,Item,Footer']
		this.properties << ['keyFieldName':'Type']
		this.properties << ['Header.fieldNames':'Type,FileName']
		this.properties << ['Delivery.fieldNames':'Type']
		this.properties << ['Order.fieldNames':'Type,DeliveryNo,OrderNo']
		this.properties << ['OrderText.fieldNames':'Type,OrderNo,TextValue']
		this.properties << ['Item.fieldNames':'Type,OrderNo,ItemNo,Quantity']
		this.properties << ['Footer.fieldNames':'Type,DeliveryCount']
		this.properties << ['Header.keyFieldValue':'H']
		this.properties << ['Delivery.keyFieldValue':'D']
		this.properties << ['Order.keyFieldValue':'O']
		this.properties << ['OrderText.keyFieldValue':'T']
		this.properties << ['Item.keyFieldValue':'I']
		this.properties << ['Footer.keyFieldValue':'F']
		this.properties << ['Header.parent':'Root']
		this.properties << ['Delivery.parent':'Root']
		this.properties << ['Order.parent':'Delivery']
		this.properties << ['OrderText.parent':'Order']
		this.properties << ['Item.parent':'Order']
		this.properties << ['Footer.parent':'Root']
		this.properties << ['defaultFieldSeparator':',']
		this.properties << ['indentFactor':'2']
		this.properties << ['Delivery.additionalLastFields':'error']
		this.inputFileName = 'DeepPlain2XML_Scenario3.txt'

		when:
		process()

		then:
		ConverterException e = thrown()
		e.message == "Line 2 has more fields than configured"
	}

	def 'Plain > XML - enclosureSignEnd and enclosureSignEndEscape'() {
		given:
		this.properties << ['documentName':'MT_DeepPlain2XML']
		this.properties << ['documentNamespace':'urn:equalize:com']
		this.properties << ['recordsetStructure':'Delivery,Order']
		this.properties << ['keyFieldName':'Type']
		this.properties << ['Delivery.fieldNames':'Type,DeliveryNo']
		this.properties << ['Order.fieldNames':'Type,DeliveryNo,OrderNo']
		this.properties << ['Delivery.keyFieldValue':'D']
		this.properties << ['Order.keyFieldValue':'O']
		this.properties << ['Delivery.parent':'Root']
		this.properties << ['Order.parent':'Delivery']
		this.properties << ['defaultFieldSeparator':',']
		this.properties << ['Order.enclosureSignBegin':'"']
		this.properties << ['Order.enclosureSignBeginEscape':'""']
		this.properties << ['Order.enclosureSignEnd':';']
		this.properties << ['Order.enclosureSignEndEscape':';;']
		this.properties << ['indentFactor':'2']
		this.inputFileName = 'DeepPlain2XML_Scenario1a.txt'
		this.outputFileName = 'DeepPlain2XML_Scenario1a_output.xml'

		expect:
		process() == this.expectedOutputFile.getText('UTF-8')
	}

	def 'Plain > XML - exception is thrown when parent line is missing in input file'() {
		given:
		this.properties << ['documentName':'MT_DeepPlain2XML']
		this.properties << ['documentNamespace':'urn:equalize:com']
		this.properties << ['recordsetStructure':'Delivery,Order']
		this.properties << ['keyFieldName':'Type']
		this.properties << ['Delivery.fieldNames':'Type,DeliveryNo']
		this.properties << ['Order.fieldNames':'Type,DeliveryNo,OrderNo']
		this.properties << ['Delivery.keyFieldValue':'D']
		this.properties << ['Order.keyFieldValue':'O']
		this.properties << ['Delivery.parent':'Root']
		this.properties << ['Order.parent':'Delivery']
		this.properties << ['defaultFieldSeparator':',']
		this.inputFileName = 'DeepPlain2XML_Scenario1b.txt'

		when:
		process()

		then:
		ConverterException e = thrown()
		e.message == "Cannot find parent for line 1: Record Type = Order"
	}
}