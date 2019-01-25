package com.equalize.cpi.converter.util

import org.apache.camel.Exchange
import org.apache.camel.TypeConverter

import com.equalize.converter.core.util.ClassTypeConverter

class CamelClassTypeConverter implements ClassTypeConverter {
	final Exchange exchange

	CamelClassTypeConverter(Exchange exchange) {
		this.exchange = exchange
	}

	@SuppressWarnings("unchecked")
	@Override
	<T> Object convertTo(Object classTo, Object objectFrom) {
		TypeConverter tc = this.exchange.getContext().getTypeConverter()
		return tc.convertTo((Class<T>) classTo, objectFrom)
	}
}