package com.equalize.cpi.converter

import org.apache.camel.CamelContext
import org.apache.camel.Exchange
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.impl.DefaultExchange

import spock.lang.Specification

class FormatConversionBeanSpec extends Specification {
	static final String filePath = 'src/test/resources'

	Exchange exchange
	Map<String,Object> properties

	String inputFileName
	String outputFileName
	File expectedOutputFile

	def setup() {
		// Setup the Camel context, Camel exchange
		CamelContext context = new DefaultCamelContext()
		this.exchange = new DefaultExchange(context)
		this.properties = [:]
	}

	private process() {
		this.exchange.getIn().setBody(new File("$filePath/$inputFileName"))
		this.expectedOutputFile = new File("$filePath/$outputFileName")

		def fcb = new FormatConversionBean(this.exchange, properties)
		fcb.convert()
	}

	def 'Exception is thrown when converterClass is not configured'() {
		when:
		process()

		then:
		RuntimeException e = thrown()
		e.message == "Mandatory parameter 'converterClass' is missing"
	}

	def 'Exception is thrown when an invalid converter class is configured'() {
		given:
		this.properties << ['converterClass':'dummyClassName']

		when:
		process()

		then:
		RuntimeException e = thrown()
		e.message == 'dummyClassName is an invalid converter class'
	}
}