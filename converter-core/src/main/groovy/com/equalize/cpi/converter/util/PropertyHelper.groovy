package com.equalize.cpi.converter.util

class PropertyHelper {
	final Map<String,Object> properties

	PropertyHelper(Map<String,Object> properties) {
		this.properties = properties
	}

	String getProperty(String propertyName, String defaultValue) {
		String propertyValue = this.properties.get(propertyName)
		if(propertyValue == null) {
			if(defaultValue != null)
				propertyValue = defaultValue
			else
				throw new RuntimeException("Mandatory parameter '$propertyName' is missing")
		}
		return propertyValue
	}

	String getProperty(String propertyName) {
		getProperty(propertyName, null)
	}

	int getPropertyAsInt(String propertyName, String defaultValue) {
		getProperty(propertyName, defaultValue) as int
	}

	int getPropertyAsInt(String propertyName) {
		getPropertyAsInt(propertyName, null)
	}

	boolean getPropertyAsBoolean(String propertyName, String defaultValue) {
		getProperty(propertyName, defaultValue).toBoolean()
	}

	boolean getPropertyAsBoolean(String propertyName) {
		getPropertyAsBoolean(propertyName, null)
	}

	void checkValidValues(String propertyName, Object propertyValue, Set<Object> validValues) {
		if(!validValues.contains(propertyValue))
			throw new RuntimeException("Value '$propertyValue' not valid for parameter $propertyName")
	}
}