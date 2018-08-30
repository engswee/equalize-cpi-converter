package com.equalize.converter.core.util;

public interface ClassTypeConverter {
	public <T> Object convertTo(Object classTo, Object objectFrom);
}
