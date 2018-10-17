package com.equalize.converter.core

import org.apache.camel.CamelContext
import org.apache.camel.Exchange
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.impl.DefaultExchange
import com.equalize.converter.core.util.ConverterException
import com.equalize.cpi.converter.FormatConversionBean

import spock.lang.Specification

class XML2DeepPlainConverterSpec extends Specification {
	static final String filePath = 'src/test/resources/FCC'
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
		this.properties = ['converterClass':'com.equalize.converter.core.XML2DeepPlainConverter']
	}

	private String process() {
		this.exchange.getIn().setBody(new File("$filePath/$inputFileName"))
		this.expectedOutputFile = new File("$filePath/$outputFileName")

		def fcb = new FormatConversionBean(this.exchange, properties)
		byte[] output = fcb.convert()

		String generatedOutput = new String(output, 'UTF-8')
		// Output is generated with system native line endings
		// So on Windows, replace CRLF so that it matches sample output
		if (newLine == '\r\n')
			generatedOutput = generatedOutput.replaceAll(newLine, '\n')

		return generatedOutput
	}

	// Reference - https://blogs.sap.com/2015/03/06/deepfccbean-the-better-fcc-at-meeting-your-deep-structure-needs-part-1-deep-xml-to-flat-file/

	def 'XML > Plain - exception is thrown when recordsetStructure is not configured'() {
		when:
		process()

		then:
		ConverterException e = thrown()
		e.message == "Mandatory parameter 'recordsetStructure' is missing"
	}

	def 'XML > Plain - exception is thrown when defaultFieldSeparator, fieldSeparator, or fieldFixedLengths are not configured'() {
		given:
		this.properties << ['recordsetStructure':'Header']

		when:
		process()

		then:
		ConverterException e = thrown()
		e.message == "'defaultFieldSeparator', 'Header.fieldSeparator' or 'Header.fieldFixedLengths' must be populated"
	}

	def 'XML > Plain - exception is thrown when both fieldSeparator and fieldFixedLengths are configured'() {
		given:
		this.properties << ['recordsetStructure':'Header']
		this.properties << ['Header.fieldSeparator':',']
		this.properties << ['Header.fieldFixedLengths':'5,10']

		when:
		process()

		then:
		ConverterException e = thrown()
		e.message == "Use only parameter 'Header.fieldSeparator'/'defaultFieldSeparator' or 'Header.fieldFixedLengths', not both"
	}

	def 'XML > Plain - exception is thrown when both defaultFieldSeparator and fieldFixedLengths are configured'() {
		given:
		this.properties << ['recordsetStructure':'Header']
		this.properties << ['defaultFieldSeparator':',']
		this.properties << ['Header.fieldFixedLengths':'5,10']

		when:
		process()

		then:
		ConverterException e = thrown()
		e.message == "Use only parameter 'Header.fieldSeparator'/'defaultFieldSeparator' or 'Header.fieldFixedLengths', not both"
	}

	def 'XML > Plain - exception is thrown when duplicate found in recordsetStructure'() {
		given:
		this.properties << ['recordsetStructure':'Header,Header']
		this.properties << ['defaultFieldSeparator':',']

		when:
		process()

		then:
		ConverterException e = thrown()
		e.message == "Duplicate field found in 'recordsetStructure': Header"
	}

	def 'XML > Plain - Comma/tab output with custom end separator'() {
		given:
		this.properties << ['recordsetStructure':'Delivery,Order,Item']
		this.properties << ['Delivery.fieldSeparator':',']
		this.properties << ['Delivery.endSeparator':"ZZZZ'nl'"]
		this.properties << ['Item.fieldSeparator':"'0x09'"]
		this.properties << ['Order.fieldSeparator':',']
		this.inputFileName = 'XML2DeepPlain_Scenario1.xml'
		this.outputFileName = 'XML2DeepPlain_Scenario1_output.txt'

		expect:
		process() == this.expectedOutputFile.getText('UTF-8')
	}

	def 'XML > Plain - CSV output with default field separator'() {
		given:
		this.properties << ['recordsetStructure':'Header,Delivery,Order,OrderText,Item,Footer']
		this.properties << ['defaultFieldSeparator':',']
		this.inputFileName = 'XML2DeepPlain_Scenario3.xml'
		this.outputFileName = 'XML2DeepPlain_Scenario3_output.txt'

		expect:
		process() == this.expectedOutputFile.getText('UTF-8')
	}

	def 'XML > Plain - CSV output with default field separator using DOM'() {
		given:
		this.properties << ['recordsetStructure':'Header,Delivery,Order,OrderText,Item,Footer']
		this.properties << ['defaultFieldSeparator':',']
		this.properties << ['useDOM':'Y']
		this.inputFileName = 'XML2DeepPlain_Scenario3.xml'
		this.outputFileName = 'XML2DeepPlain_Scenario3_output.txt'

		expect:
		process() == this.expectedOutputFile.getText('UTF-8')
	}

	def 'XML > Plain - CSV output with certain segments without fields'() {
		given:
		this.properties << ['recordsetStructure':'Header,Delivery,Order,OrderText,Item,Footer']
		this.properties << ['defaultFieldSeparator':',']
		this.inputFileName = 'XML2DeepPlain_Scenario4.xml'
		this.outputFileName = 'XML2DeepPlain_Scenario4_output.txt'

		expect:
		process() == this.expectedOutputFile.getText('UTF-8')
	}

	def 'XML > Plain - exception is thrown when XML input has segments not matching structure in recordsetStructure'() {
		given:
		this.properties << ['recordsetStructure':'Header,Delivery']
		this.properties << ['defaultFieldSeparator':',']
		this.inputFileName = 'XML2DeepPlain_Scenario3.xml'

		when:
		process()

		then:
		ConverterException e = thrown()
		e.message == "Record Type Order not listed in parameter 'recordsetStructure'"
	}

	def 'XML > Plain - fixedLengthTooShortHandling - error'() {
		given:
		this.properties << ['recordsetStructure':'Delivery,Order,Item']
		this.properties << ['Delivery.fieldFixedLengths':'5,10']
		this.properties << ['Order.fieldFixedLengths':'5,5,10']
		this.properties << ['Item.fieldFixedLengths':'5,10,10,10']
		this.inputFileName = 'XML2DeepPlain_Scenario2.xml'

		when:
		process()

		then:
		ConverterException e = thrown()
		e.message == "Field value 'Delivery2' longer than allowed length 5 for field 'DeliveryNo'"
	}

	def 'XML > Plain - fixedLengthTooShortHandling - cut'() {
		given:
		this.properties << ['recordsetStructure':'Delivery,Order,Item']
		this.properties << ['Delivery.fieldFixedLengths':'5,10']
		this.properties << ['Order.fieldFixedLengths':'5,10,10']
		this.properties << ['Item.fieldFixedLengths':'5,5,10,10']
		this.properties << ['Item.fixedLengthTooShortHandling':'Cut']
		this.inputFileName = 'XML2DeepPlain_Scenario2.xml'
		this.outputFileName = 'XML2DeepPlain_Scenario2a_output.txt'

		expect:
		process() == this.expectedOutputFile.getText('UTF-8')
	}

	def 'XML > Plain - fixedLengthTooShortHandling - ignore'() {
		given:
		this.properties << ['recordsetStructure':'Delivery,Order,Item']
		this.properties << ['Delivery.fieldFixedLengths':'5,10']
		this.properties << ['Order.fieldFixedLengths':'5,5,10']
		this.properties << ['Order.fixedLengthTooShortHandling':'Ignore']
		this.properties << ['Item.fieldFixedLengths':'5,10,10,10']
		this.inputFileName = 'XML2DeepPlain_Scenario2.xml'
		this.outputFileName = 'XML2DeepPlain_Scenario2b_output.txt'

		expect:
		process() == this.expectedOutputFile.getText('UTF-8')
	}

	def 'XML > Plain - more fields in input than specified in fieldFixedLengths'() {
		given:
		this.properties << ['recordsetStructure':'Delivery,Order,Item']
		this.properties << ['Delivery.fieldFixedLengths':'5']
		this.properties << ['Order.fieldFixedLengths':'5,10,10']
		this.properties << ['Item.fieldFixedLengths':'5,5,10,10']
		this.inputFileName = 'XML2DeepPlain_Scenario2.xml'

		when:
		process()

		then:
		ConverterException e = thrown()
		e.message == "More fields found in XML structure than specified in parameter 'Delivery.fieldFixedLengths'"
	}
}