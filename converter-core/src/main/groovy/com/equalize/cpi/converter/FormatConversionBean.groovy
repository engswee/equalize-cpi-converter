package com.equalize.cpi.converter

import org.apache.camel.Exchange
import com.equalize.cpi.converter.util.AbstractConverter
import com.equalize.cpi.converter.util.ConverterFactory

class FormatConversionBean {
	Exchange exchange
	Map<String, Object> properties

	FormatConversionBean(Exchange exchange, Map<String, Object> properties) {
		this.exchange = exchange
		this.properties = properties
	}

	Object convert() {
		// Dynamically load Converter class
		ConverterFactory cf = ConverterFactory.newInstance()
		AbstractConverter converter = cf.newConverter(this.exchange, this.properties)

		// Retrieve configured parameters
		converter.getParameters()

		// Parse input
		converter.parseInput()

		// Generate output
		converter.generateOutput()
	}
}
