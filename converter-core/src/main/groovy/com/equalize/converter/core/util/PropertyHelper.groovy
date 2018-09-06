package com.equalize.converter.core.util

class PropertyHelper {
	final Map<String,Object> properties

	PropertyHelper(Map<String,Object> properties) {
		this.properties = properties
	}

	String retrieveProperty(String propertyName, String defaultValue) {
		String propertyValue = this.properties.get(propertyName)
		if(propertyValue == null) {
			if(defaultValue != null)
				propertyValue = defaultValue
			else
				throw new ConverterException("Mandatory parameter '$propertyName' is missing")
		}
		return propertyValue
	}

	String retrieveProperty(String propertyName) {
		retrieveProperty(propertyName, null)
	}

	int retrievePropertyAsInt(String propertyName, String defaultValue) {
		retrieveProperty(propertyName, defaultValue) as int
	}

	int retrievePropertyAsInt(String propertyName) {
		retrievePropertyAsInt(propertyName, null)
	}

	boolean retrievePropertyAsBoolean(String propertyName, String defaultValue) {
		retrieveProperty(propertyName, defaultValue).toBoolean()
	}

	boolean retrievePropertyAsBoolean(String propertyName) {
		retrievePropertyAsBoolean(propertyName, null)
	}

	void checkValidValues(String propertyName, Object propertyValue, Set<Object> validValues) {
		if(!validValues.contains(propertyValue))
			throw new ConverterException("Value '$propertyValue' not valid for parameter $propertyName")
	}
}