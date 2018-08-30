package com.equalize.cpi.converter.util;

public interface ClassTypeConverter {
	public <T> Object convertTo(Object classTo, Object objectFrom);
}
