package com.equalize.cpi.converter.util

import org.apache.camel.Exchange

abstract class AbstractConverter {
	protected final Exchange exchange
	protected final Map<String,Object> properties
	protected final TypeConverterHelper tch
	protected final PropertyHelper ph

	AbstractConverter(Exchange exchange, Map<String,Object> properties) {
		this.exchange = exchange
		this.properties = properties
		this.tch = new TypeConverterHelper(exchange)
		this.ph = new PropertyHelper(properties)
	}

	abstract void getParameters()

	abstract void parseInput()

	abstract Object generateOutput()
}