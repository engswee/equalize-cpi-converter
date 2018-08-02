package com.equalize.cpi.converter.util;

import org.apache.camel.Exchange;
import org.apache.camel.TypeConverter;

public class TypeConverterHelper {
	private final Exchange exchange;

	public TypeConverterHelper(Exchange exchange) {
		this.exchange = exchange;
	}

	@SuppressWarnings("unchecked")
	public <T> Object convertTo(Object classTo, Object objectFrom) {
		TypeConverter tc = this.exchange.getContext().getTypeConverter();
		return tc.convertTo((Class<T>) classTo, objectFrom);
	}
}