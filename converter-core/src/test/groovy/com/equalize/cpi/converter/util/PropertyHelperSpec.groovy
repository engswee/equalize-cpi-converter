package com.equalize.cpi.converter.util

import spock.lang.Specification

class PropertyHelperSpec extends Specification {
	Map<String,Object> properties = [:]
	PropertyHelper ph

	def setup() {
		this.properties << ['stringParam':'abcDEF']
		this.properties << ['stringParamBlank':'']
		this.properties << ['intParam':'2']
		this.properties << ['intParamZero':'0']
		this.properties << ['booleanParamYes':'Y']
		this.properties << ['booleanParamNo':'N']
		this.ph = new PropertyHelper(this.properties)
	}

	def 'String - value for available property is returned'() {
		when: 'try to retrieve an existing property'
		String content = this.ph.getProperty('stringParam')
		String contentBlank = this.ph.getProperty('stringParamBlank')

		then: 'value of property is returned'
		content == 'abcDEF'
		contentBlank == ''
	}

	def 'String - runtime exception is triggered if mandatory parameter is not populated'() {
		when: 'try to retrieve a missing mandatory property'
		String content = this.ph.getProperty('mandatoryParam')

		then: 'Exception is thrown'
		RuntimeException e = thrown()
		e.message == "Mandatory parameter 'mandatoryParam' is missing"
	}

	def 'String - default value is returned if optional parameter is not populated'() {
		when: 'try to retrieve an missing optional property'
		String content = this.ph.getProperty('optParam','XYZ123')
		String contentBlank = this.ph.getProperty('optParamBlank','')

		then: 'default value of property is returned'
		content == 'XYZ123'
		contentBlank == ''
	}

	def 'Integer - value for available property is returned'() {
		when: 'try to retrieve an existing property'
		int content = this.ph.getPropertyAsInt('intParam')
		int contentZero = this.ph.getPropertyAsInt('intParamZero')

		then: 'value of property is returned'
		content == 2
		contentZero == 0
	}

	def 'Integer - runtime exception is triggered if mandatory parameter is not populated'() {
		when: 'try to retrieve a missing mandatory property'
		int content = this.ph.getPropertyAsInt('mandatoryIntParam')

		then: 'Exception is thrown'
		RuntimeException e = thrown()
		e.message == "Mandatory parameter 'mandatoryIntParam' is missing"
	}

	def 'Integer - default value is returned if optional parameter is not populated'() {
		when: 'try to retrieve an missing optional property'
		int content = this.ph.getPropertyAsInt('optIntParam','15')
		int contentZero = this.ph.getPropertyAsInt('optIntParamZero','0')

		then: 'default value of property is returned'
		content == 15
		contentZero == 0
	}

	def 'Boolean - value for available property is returned'() {
		when: 'try to retrieve an existing property'
		boolean content = this.ph.getPropertyAsBoolean('booleanParamYes')
		boolean contentNo = this.ph.getPropertyAsBoolean('booleanParamNo')

		then: 'value of property is returned'
		content == true
		contentNo == false
	}

	def 'Boolean - runtime exception is triggered if mandatory parameter is not populated'() {
		when: 'try to retrieve a missing mandatory property'
		boolean content = this.ph.getPropertyAsBoolean('mandatoryBoolParam')

		then: 'Exception is thrown'
		RuntimeException e = thrown()
		e.message == "Mandatory parameter 'mandatoryBoolParam' is missing"
	}

	def 'Boolean - default value is returned if optional parameter is not populated'() {
		when: 'try to retrieve an missing optional property'
		boolean content = this.ph.getPropertyAsBoolean('optBoolParamYes','Y')
		boolean contentNo = this.ph.getPropertyAsBoolean('optBoolParamNo','N')

		then: 'default value of property is returned'
		content == true
		contentNo == false
	}

	def 'checkValidValues - exception is not thrown if configured parameter is a valid value'() {
		when: 'enter a valid value for parameter'
		this.ph.checkValidValues('outputType', 'plain', ['plain','xml'] as Set)

		then: 'Exception is not thrown'
		RuntimeException e = notThrown()
	}

	def 'checkValidValues - exception is thrown if configured parameter is not a valid value'() {
		when: 'enter an invalid value for parameter'
		this.ph.checkValidValues('outputType', 'test', ['plain','xml'] as Set)

		then: 'Exception is thrown'
		RuntimeException e = thrown()
		e.message == "Value 'test' not valid for parameter outputType"
	}
}