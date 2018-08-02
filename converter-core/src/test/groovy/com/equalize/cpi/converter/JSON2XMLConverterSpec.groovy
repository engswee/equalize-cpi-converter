package com.equalize.cpi.converter

import org.apache.camel.CamelContext
import org.apache.camel.Exchange
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.impl.DefaultExchange

import spock.lang.Specification
import spock.lang.Unroll

class JSON2XMLConverterSpec extends Specification {
	Exchange exchange

	def setup() {
		// Setup the Camel context, Camel exchange
		CamelContext context = new DefaultCamelContext()
		this.exchange = new DefaultExchange(context)
	}

	@Unroll
	def 'body of #outputfile is generated from input of #inputfile'(String inputfile, String outputfile, Map propertyMap) {
		given: 'input file is set as body of exchange'
		File inputFile = new File("src/test/resources/$inputfile")
		this.exchange.getIn().setBody(inputFile)

		when: 'conversion is executed'
		def fcb = new FormatConversionBean(this.exchange, propertyMap)
		byte[] output = fcb.convert()

		then: 'contents of conversion output is same as output file'
		File outputFile = new File("src/test/resources/$outputfile")
		new String(output) == outputFile.text

		where:
		inputfile | outputfile | propertyMap
		'JSON2XML_Scenario1_glossary.json' | 'JSON2XML_Scenario1_glossary_output.xml' | ['converterClass':'com.equalize.cpi.converter.JSON2XMLConverter','indentFactor':'2','documentName':'MT_JSON2XML','documentNamespace':'urn:equalize:com']
		'JSON2XML_Scenario3_array.json' | 'JSON2XML_Scenario3_array_output.xml' | ['converterClass':'com.equalize.cpi.converter.JSON2XMLConverter','indentFactor':'2','documentName':'MT_JSON2XML','documentNamespace':'urn:equalize:com','allowArrayAtTop':'Y','topArrayName':'record']
	}
}