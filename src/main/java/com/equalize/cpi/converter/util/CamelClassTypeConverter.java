package com.equalize.cpi.converter.util;

import org.apache.camel.Exchange;
import org.apache.camel.TypeConverter;

import com.equalize.converter.core.util.ClassTypeConverter;

public class CamelClassTypeConverter implements ClassTypeConverter {
	private final Exchange exchange;

	public CamelClassTypeConverter(Exchange exchange) {
		this.exchange = exchange;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Object convertTo(Object classTo, Object objectFrom) {
		TypeConverter tc = this.exchange.getContext().getTypeConverter();
		return tc.convertTo((Class<T>) classTo, objectFrom);
	}
}