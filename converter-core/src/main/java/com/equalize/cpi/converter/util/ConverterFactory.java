package com.equalize.cpi.converter.util;

import java.lang.reflect.Constructor;
import java.util.Map;

import org.apache.camel.Exchange;

public class ConverterFactory {
	// Private constructor to prevent direct instantiation of this factory
	private ConverterFactory() {
	}

	public static ConverterFactory newInstance() {
		return new ConverterFactory();
	}

	public AbstractConverter newConverter(Exchange exchange, Map<String,Object> properties) {
		PropertyHelper ph = new PropertyHelper(properties);
		String converterClassName = ph.getProperty("converterClass");
		try {
			// Dynamic loading and instantiation of converter class
			Class<?> converterClass = Class.forName(converterClassName);
			Constructor<?> constructor = converterClass.getConstructor(Exchange.class, Map.class);
			return (AbstractConverter) constructor.newInstance(exchange, properties);
		} catch(ClassNotFoundException e) {
			throw new RuntimeException(converterClassName + " is an invalid converter class");
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
}