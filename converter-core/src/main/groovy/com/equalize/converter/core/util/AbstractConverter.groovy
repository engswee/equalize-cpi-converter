package com.equalize.converter.core.util

abstract class AbstractConverter {
	protected final Object body
	protected final Map<String,Object> properties
	protected final ClassTypeConverter typeConverter
	protected final PropertyHelper ph

	AbstractConverter(Object body, Map<String,Object> properties, ClassTypeConverter typeConverter) {
		this.body = body
		this.properties = properties
		this.typeConverter = typeConverter
		this.ph = new PropertyHelper(properties)
	}

	abstract void getParameters()

	abstract void parseInput()

	abstract Object generateOutput()
}