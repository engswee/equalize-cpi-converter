package com.equalize.converter.core

import org.apache.camel.CamelContext
import org.apache.camel.Exchange
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.impl.DefaultExchange
import com.equalize.converter.core.util.ConverterException
import com.equalize.cpi.converter.FormatConversionBean
import spock.lang.Specification

class JSON2XMLConverterSpec extends Specification {
	static final String filePath = 'src/test/resources/JSON'

	Exchange exchange
	Map<String,Object> properties

	String inputFileName
	String outputFileName
	File expectedOutputFile

	def setup() {
		// Setup the Camel context, Camel exchange
		CamelContext context = new DefaultCamelContext()
		this.exchange = new DefaultExchange(context)
		this.properties = ['converterClass':'com.equalize.converter.core.JSON2XMLConverter']
	}

	private String process() {
		this.exchange.getIn().setBody(new File("$filePath/$inputFileName"))
		this.expectedOutputFile = new File("$filePath/$outputFileName")

		def fcb = new FormatConversionBean(this.exchange, properties)
		byte[] output = fcb.convert()

		return new String(output, 'UTF-8')
	}

	// Reference - https://blogs.sap.com/2015/03/17/jsontransformbean-part-1-converting-json-content-to-xml/

	def 'JSON -> XML - exception is thrown when documentName is not configured'() {
		given:
		this.properties << ['documentNamespace':'urn:equalize:com']

		when:
		process()

		then:
		ConverterException e = thrown()
		e.message == "Mandatory parameter 'documentName' is missing"
	}

	def 'JSON -> XML - exception is thrown when documentNamespace is not configured'() {
		given:
		this.properties << ['documentName':'MT_JSON2XML']

		when:
		process()

		then:
		ConverterException e = thrown()
		e.message == "Mandatory parameter 'documentNamespace' is missing"
	}

	def 'JSON -> XML - default with only mandatory parameters'() {
		given:
		this.properties << ['documentName':'MT_JSON2XML']
		this.properties << ['documentNamespace':'urn:equalize:com']
		this.inputFileName = 'JSON2XML_Scenario1_glossary.json'
		this.outputFileName = 'JSON2XML_Scenario1_glossary_output.xml'

		expect:
		process() == this.expectedOutputFile.getText('UTF-8')
	}

	def 'JSON -> XML - indentFactor set to 2'() {
		given:
		this.properties << ['indentFactor':'2']
		this.properties << ['documentName':'MT_JSON2XML']
		this.properties << ['documentNamespace':'urn:equalize:com']
		this.inputFileName = 'JSON2XML_Scenario1_glossary.json'
		this.outputFileName = 'JSON2XML_Scenario1_glossary_output_indent.xml'

		expect:
		process() == this.expectedOutputFile.getText('UTF-8')
	}

	def 'JSON -> XML - exception is thrown if topArrayName is not configured when allowArrayAtTop is set'() {
		given:
		this.properties << ['documentName':'MT_JSON2XML']
		this.properties << ['documentNamespace':'urn:equalize:com']
		this.properties << ['allowArrayAtTop':'Y']

		when:
		process()

		then:
		ConverterException e = thrown()
		e.message == "Mandatory parameter 'topArrayName' is missing"
	}

	def 'JSON -> XML - set allowArrayAtTop'() {
		given:
		this.properties << ['indentFactor':'2']
		this.properties << ['documentName':'MT_JSON2XML']
		this.properties << ['documentNamespace':'urn:equalize:com']
		this.properties << ['allowArrayAtTop':'Y']
		this.properties << ['topArrayName':'record']
		this.inputFileName = 'JSON2XML_Scenario3_array.json'
		this.outputFileName = 'JSON2XML_Scenario3_array_output.xml'

		expect:
		process() == this.expectedOutputFile.getText('UTF-8')
	}

	def 'JSON -> XML - allowArrayAtTop = Y but input do not have top array'() {
		given:
		this.properties << ['indentFactor':'2']
		this.properties << ['documentName':'MT_JSON2XML']
		this.properties << ['documentNamespace':'urn:equalize:com']
		this.properties << ['allowArrayAtTop':'Y']
		this.properties << ['topArrayName':'record']
		this.inputFileName = 'JSON2XML_Scenario3a.json'
		this.outputFileName = 'JSON2XML_Scenario3a_output.xml'

		expect:
		process() == this.expectedOutputFile.getText('UTF-8')
	}

	def 'JSON -> XML - exception is thrown if there is an invalid character in XML field name'() {
		given:
		this.properties << ['indentFactor':'2']
		this.properties << ['documentName':'MT_JSON2XML']
		this.properties << ['documentNamespace':'urn:equalize:com']
		this.inputFileName = 'JSON2XML_Scenario2.json'

		when:
		process()

		then:
		ConverterException e = thrown()
		e.message == "Invalid character in XML element name: 64bit"
	}

	def 'JSON -> XML - set escapeInvalidNameStartChar'() {
		given:
		this.properties << ['indentFactor':'2']
		this.properties << ['documentName':'MT_JSON2XML']
		this.properties << ['documentNamespace':'urn:equalize:com']
		this.properties << ['escapeInvalidNameStartChar':'Y']
		this.inputFileName = 'JSON2XML_Scenario2.json'
		this.outputFileName = 'JSON2XML_Scenario2_output.xml'

		expect:
		process() == this.expectedOutputFile.getText('UTF-8')
	}

	def 'JSON -> XML - set mangleInvalidNameChar'() {
		given:
		this.properties << ['indentFactor':'2']
		this.properties << ['documentName':'MT_JSON2XML']
		this.properties << ['documentNamespace':'urn:equalize:com']
		this.properties << ['mangleInvalidNameChar':'Y']
		this.inputFileName = 'JSON2XML_Scenario4.json'
		this.outputFileName = 'JSON2XML_Scenario4_output.xml'

		expect:
		process() == this.expectedOutputFile.getText('UTF-8')
	}

	def 'JSON -> XML - null in JSON field content'() {
		given:
		this.properties << ['indentFactor':'2']
		this.properties << ['documentName':'MT_JSON2XML']
		this.properties << ['documentNamespace':'urn:equalize:com']
		this.inputFileName = 'JSON2XML_Scenario5.json'
		this.outputFileName = 'JSON2XML_Scenario5_output.xml'

		expect:
		process() == this.expectedOutputFile.getText('UTF-8')
	}
}