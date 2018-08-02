package com.equalize.cpi.converter

import org.apache.camel.CamelContext
import org.apache.camel.Exchange
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.impl.DefaultExchange

import spock.lang.Specification
import spock.lang.Unroll

class XML2JSONConverterSpec extends Specification {
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
		String output = fcb.convert()

		then: 'contents of conversion output is same as output file'
		File outputFile = new File("src/test/resources/$outputfile")
		output == outputFile.text

		where:
		inputfile | outputfile | propertyMap
		'XML2JSON_Scenario1.xml' | 'XML2JSON_Scenario1_output.json' | ['converterClass':'com.equalize.cpi.converter.XML2JSONConverter','indentFactor':'2','skipRootNode':'Y']
		'XML2JSON_Scenario3.xml' | 'XML2JSON_Scenario3_output.json' | ['converterClass':'com.equalize.cpi.converter.XML2JSONConverter','indentFactor':'2','skipRootNode':'Y','arrayFieldList':'single,SortAs,oneparentmanychild,title']
		'XML2JSON_Scenario3.xml' | 'XML2JSON_Scenario3a_output.json' | ['converterClass':'com.equalize.cpi.converter.XML2JSONConverter','indentFactor':'2','skipRootNode':'Y','forceArrayAll':'Y']
	}
}