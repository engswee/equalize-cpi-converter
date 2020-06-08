package com.equalize.converter.core

import org.apache.camel.CamelContext
import org.apache.camel.Exchange
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.impl.DefaultExchange
import com.equalize.converter.core.util.ConverterException
import com.equalize.cpi.converter.FormatConversionBean

import spock.lang.Specification

class Base64DecodeConverterSpec extends Specification {
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
		this.properties = ['converterClass':'com.equalize.converter.core.Base64DecodeConverter']
	}

	private byte[] process() {
		this.exchange.getIn().setBody(new File("$filePath/$inputFileName"))
		this.expectedOutputFile = new File("$filePath/$outputFileName")

		def fcb = new FormatConversionBean(this.exchange, properties)
		fcb.convert()
	}

	// Reference - https://blogs.sap.com/2015/04/30/base64decodeconverter-base64-decoding-made-easy/

	def 'Base64 Decode - exception is thrown when inputType is not configured'() {
		when:
		process()

		then:
		ConverterException e = thrown()
		e.message == "Mandatory parameter 'inputType' is missing"
	}

	def 'Base64 Decode - exception is thrown when xpath is not configured for XML input'() {
		given:
		this.properties << ['inputType':'xml']

		when:
		process()

		then:
		ConverterException e = thrown()
		e.message == "Mandatory parameter 'xpath' is missing"
	}

	def 'Base64 Decode - exception is thrown when invalid inputType is configured'() {
		given:
		this.properties << ['inputType':'text']

		when:
		process()

		then:
		ConverterException e = thrown()
		e.message == "Value 'text' not valid for parameter inputType"
	}

	def 'Base64 Decode - Plain input containing compressed content'() {
		given:
		this.properties << ['inputType':'plain']
		this.properties << ['zippedContent':'Y']
		this.inputFileName = 'Base64Decode_Scenario1.txt'
		this.outputFileName = 'Base64Decode_Scenario1_output.xml'

		expect:
		new String(process(), 'UTF-8') == this.expectedOutputFile.getText('UTF-8').normalize()
	}

	def 'Base64 Decode - Plain input containing binary uncompressed content'() {
		given:
		this.properties << ['inputType':'plain']
		this.inputFileName = 'Base64Decode_Scenario2.txt'
		this.outputFileName = 'Base64Decode_Scenario2_output.jpg'

		expect:
		process() == this.expectedOutputFile.bytes
	}

	def 'Base64 Decode - exception is thrown when trying to decompress uncompressed input'() {
		given:
		this.properties << ['inputType':'plain']
		this.properties << ['zippedContent':'Y']
		this.inputFileName = 'Base64Decode_Scenario2.txt'

		when:
		process()

		then:
		ConverterException e = thrown()
		e.message == "Unable to decompress as content is not zipped"
	}

	def 'Base64 Decode - XML input containing compressed content'() {
		given:
		this.properties << ['inputType':'xml']
		this.properties << ['zippedContent':'Y']
		this.properties << ['xpath':'/MT_TransferSAEFile/SAEZipContent']
		this.inputFileName = 'Base64Decode_Scenario3.xml'
		this.outputFileName = 'Base64Decode_Scenario3_output.txt'

		expect:
		new String(process(), 'UTF-8') == this.expectedOutputFile.getText('UTF-8').normalize()
	}

	def 'Base64 Decode - exception is thrown when XPath expression is invalid'() {
		given:
		this.properties << ['inputType':'xml']
		this.properties << ['zippedContent':'Y']
		this.properties << ['xpath':'/MT_TransferSAEFile/dummy']
		this.inputFileName = 'Base64Decode_Scenario3.xml'

		when:
		process()

		then:
		ConverterException e = thrown()
		e.message == "XPath /MT_TransferSAEFile/dummy does not exist"
	}

	def 'Base64 Decode - Plain input containing uncompressed content with Hebrew characters'() {
		given:
		this.properties << ['inputType':'plain']
		this.inputFileName = 'Base64Decode_Scenario4_WithHebrew.txt'
		this.outputFileName = 'Base64Decode_Scenario4_WithHebrew_output.txt'

		expect:
		new String(process(), 'UTF-8') == this.expectedOutputFile.getText('UTF-8')
	}
}