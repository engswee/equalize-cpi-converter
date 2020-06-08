package com.equalize.converter.core

import org.apache.camel.CamelContext
import org.apache.camel.Exchange
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.impl.DefaultExchange

import com.equalize.converter.core.util.ConversionBase64Decode
import com.equalize.converter.core.util.ConverterException
import com.equalize.cpi.converter.FormatConversionBean

import spock.lang.Specification

class Base64EncodeConverterSpec extends Specification {
	static final String filePath = 'src/test/resources/Base64'

	Exchange exchange
	Map<String,Object> properties

	String inputFileName
	String outputFileName
	File expectedOutputFile

	def setup() {
		// Setup the Camel context, Camel exchange
		CamelContext context = new DefaultCamelContext()
		this.exchange = new DefaultExchange(context)
		this.properties = ['converterClass':'com.equalize.converter.core.Base64EncodeConverter']
	}

	private byte[] process() {
		if (inputFileName) {
			this.exchange.getIn().setBody(new File("$filePath/$inputFileName").text.normalize())
			this.expectedOutputFile = new File("$filePath/$outputFileName")
		}

		def fcb = new FormatConversionBean(this.exchange, properties)
		fcb.convert()
	}

	// Reference - https://blogs.sap.com/2015/05/19/base64encodeconverter-base64-encoding-made-easy/

	def 'Base64 Encode - exception is thrown when outputType is not configured'() {
		when:
		process()

		then:
		ConverterException e = thrown()
		e.message == "Mandatory parameter 'outputType' is missing"
	}

	def 'Base64 Encode - exception is thrown when documentName is not configured for XML output'() {
		given:
		this.properties << ['outputType':'xml']
		this.properties << ['documentNamespace':'urn:dummy']

		when:
		process()

		then:
		ConverterException e = thrown()
		e.message == "Mandatory parameter 'documentName' is missing"
	}

	def 'Base64 Encode - exception is thrown when documentNamespace is not configured for XML output'() {
		given:
		this.properties << ['outputType':'xml']
		this.properties << ['documentName':'MT_Base64']

		when:
		process()

		then:
		ConverterException e = thrown()
		e.message == "Mandatory parameter 'documentNamespace' is missing"
	}

	def 'Base64 Encode - exception is thrown when invalid outputType is configured'() {
		given:
		this.properties << ['outputType':'text']

		when:
		process()

		then:
		ConverterException e = thrown()
		e.message == "Value 'text' not valid for parameter outputType"
	}

	def 'Base64 Encode - Plain output'() {
		given:
		this.properties << ['outputType':'plain']
		this.inputFileName = 'Base64Encode_Scenario1.txt'
		this.outputFileName = 'Base64Encode_Scenario1_output.txt'

		expect:
		new String(process(), 'UTF-8') == this.expectedOutputFile.getText('UTF-8')
	}

	def 'Base64 Encode - Plain output with compression'() {
		given:
		this.properties << ['outputType':'plain']
		this.properties << ['compress':'Y']
		this.inputFileName = 'Base64Encode_Scenario1.txt'
		this.outputFileName = 'Base64Encode_Scenario1.txt'

		when:
		byte[] encoded = process()
		// Output of encoding can't be compared directly due to differences in output caused by compression
		// So instead just reverse the process and see if it can decode back the encoded content
		// to get back the original file
		ConversionBase64Decode decoder = new ConversionBase64Decode(new String(encoded, 'UTF-8'), true)
		String decodedText = new String(decoder.decode(), 'UTF-8')

		then:
		decodedText == this.expectedOutputFile.getText('UTF-8').normalize()
	}

	def 'Base64 Encode - XML output with default field name'() {
		given:
		this.properties << ['outputType':'xml']
		this.properties << ['documentName':'MT_Base64']
		this.properties << ['documentNamespace':'urn:dummy']
		this.inputFileName = 'Base64Encode_Scenario2.txt'
		this.outputFileName = 'Base64Encode_Scenario2_default_output.xml'

		when:
		String generatedOutput = new String(process(), 'UTF-8')

		then:
		generatedOutput == this.expectedOutputFile.getText('UTF-8')
	}

	def 'Base64 Encode - XML output with configured field name'() {
		given:
		this.properties << ['outputType':'xml']
		this.properties << ['documentName':'MT_Base64']
		this.properties << ['documentNamespace':'urn:dummy']
		this.properties << ['base64FieldName':'b64string']
		this.inputFileName = 'Base64Encode_Scenario2.txt'
		this.outputFileName = 'Base64Encode_Scenario2_output.xml'

		when:
		String generatedOutput = new String(process(), 'UTF-8')

		then:
		generatedOutput == this.expectedOutputFile.getText('UTF-8')
	}
}