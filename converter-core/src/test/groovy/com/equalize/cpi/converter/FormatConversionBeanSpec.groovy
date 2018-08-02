package com.equalize.cpi.converter

import org.apache.camel.CamelContext
import org.apache.camel.Exchange
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.impl.DefaultExchange

import spock.lang.Specification

class FormatConversionBeanSpec extends Specification {
	Exchange exchange

	def setup() {
		// Setup the Camel context, Camel exchange
		CamelContext context = new DefaultCamelContext()
		this.exchange = new DefaultExchange(context)
	}

	def 'ClassNotFoundException is thrown when an invalid converter class is configured'(String inputfile, String outputfile, Map propertyMap) {
		given: 'input file is set as body of exchange'
		File inputFile = new File("src/test/resources/$inputfile")
		this.exchange.getIn().setBody(inputFile)

		when: 'conversion is executed'
		def fcb = new FormatConversionBean(this.exchange, propertyMap)
		String output = fcb.convert()

		then: 'exception is thrown'
		ClassNotFoundException e = thrown()
		e.message == 'dummyClassName is an invalid converter class'

		where:
		inputfile | outputfile | propertyMap
		'input.xml' | 'output.json' | ['converterClass':'dummyClassName']
	}
}