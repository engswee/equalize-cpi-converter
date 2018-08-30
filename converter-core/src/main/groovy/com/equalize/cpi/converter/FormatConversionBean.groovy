package com.equalize.cpi.converter

import org.apache.camel.Exchange

import com.equalize.converter.core.util.AbstractConverter
import com.equalize.converter.core.util.ClassTypeConverter
import com.equalize.converter.core.util.ConverterFactory
import com.equalize.cpi.converter.util.CamelClassTypeConverter

class FormatConversionBean {
	final Object body
	final Map<String, Object> properties
	final ClassTypeConverter typeConverter

	FormatConversionBean(Exchange exchange, Map<String, Object> properties) {
		this.body = exchange.getIn().getBody()
		this.properties = properties
		this.typeConverter = new CamelClassTypeConverter(exchange)
	}

	Object convert() {
		// Dynamically load Converter class
		ConverterFactory cf = ConverterFactory.newInstance()
		AbstractConverter converter = cf.newConverter(this.body, this.properties, this.typeConverter)

		// Retrieve configured parameters
		converter.getParameters()

		// Parse input
		converter.parseInput()

		// Generate output
		converter.generateOutput()
	}
}
