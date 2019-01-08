package com.equalize.converter.core.util

interface ClassTypeConverter {
	def <T> Object convertTo(Object classTo, Object objectFrom);
}
