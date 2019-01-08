package com.equalize.converter.core.util

import java.lang.reflect.Constructor

class ConverterFactory {
	// Private constructor to prevent direct instantiation of this factory
	private ConverterFactory() {
	}

	static ConverterFactory newInstance() {
		new ConverterFactory()
	}

	AbstractConverter newConverter(Object body, Map<String,Object> properties, ClassTypeConverter typeConverter) {
		PropertyHelper ph = new PropertyHelper(properties)
		String converterClassName = ph.retrieveProperty('converterClass')
		try {
			// Dynamic loading and instantiation of converter class
			Class<?> converterClass = Class.forName(converterClassName)
			Constructor<?> constructor = converterClass.getConstructor(Object, Map, ClassTypeConverter)
			return (AbstractConverter) constructor.newInstance(body, properties, typeConverter)
		} catch(ClassNotFoundException ignored) {
			throw new ClassNotFoundException("$converterClassName is an invalid converter class")
		}
	}
}