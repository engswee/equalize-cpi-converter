package com.equalize.converter.core

import org.apache.camel.CamelContext
import org.apache.camel.Exchange
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.impl.DefaultExchange

import com.equalize.cpi.converter.FormatConversionBean

import spock.lang.Specification

class XML2JSONConverterSpec extends Specification {
	static final String filePath = 'src/test/resources/JSON'

	Exchange exchange
	Map<String,Object> properties

	String inputFileName
	String outputFileName
	File expectedOutputFile

	def setup() {
		// Setup the Camel context, Camel exchange
		CamelContext context = new DefaultCamelContext()
		this.exchange = new DefaultExchange(context)
		this.properties = ['converterClass':'com.equalize.converter.core.XML2JSONConverter']
	}

	private String process() {
		this.exchange.getIn().setBody(new File("$filePath/$inputFileName"))
		this.expectedOutputFile = new File("$filePath/$outputFileName")

		def fcb = new FormatConversionBean(this.exchange, properties)
		fcb.convert()
	}

	// Reference - https://blogs.sap.com/2015/03/18/jsontransformbean-part-2-converting-xml-to-json-content/

	def 'XML -> JSON - set skipRootNode'() {
		given:
		this.properties << ['indentFactor':'2']
		this.properties << ['skipRootNode':'Y']
		this.inputFileName = 'XML2JSON_Scenario1.xml'
		this.outputFileName = 'XML2JSON_Scenario1_output.json'

		expect:
		process() == this.expectedOutputFile.getText('UTF-8')
	}

	def 'XML -> JSON - default no indent and skip'() {
		given:
		this.inputFileName = 'XML2JSON_Scenario2.xml'
		this.outputFileName = 'XML2JSON_Scenario2_output.json'

		expect:
		process() == this.expectedOutputFile.getText('UTF-8')
	}

	def 'XML -> JSON - default no indent and skip using DOM'() {
		given:
		this.properties << ['useDOM':'Y']
		this.inputFileName = 'XML2JSON_Scenario2.xml'
		this.outputFileName = 'XML2JSON_Scenario2_output.json'

		expect:
		process() == this.expectedOutputFile.getText('UTF-8')
	}

	def 'XML -> JSON - selected list of array fields'() {
		given:
		this.properties << ['indentFactor':'2']
		this.properties << ['skipRootNode':'Y']
		this.properties << ['arrayFieldList':'single,SortAs,oneparentmanychild,title']
		this.inputFileName = 'XML2JSON_Scenario3.xml'
		this.outputFileName = 'XML2JSON_Scenario3_output.json'

		expect:
		process() == this.expectedOutputFile.getText('UTF-8')
	}

	def 'XML -> JSON - set forceArrayAll'() {
		given:
		this.properties << ['indentFactor':'2']
		this.properties << ['skipRootNode':'Y']
		this.properties << ['forceArrayAll':'Y']
		this.inputFileName = 'XML2JSON_Scenario3.xml'
		this.outputFileName = 'XML2JSON_Scenario3a_output.json'

		expect:
		process() == this.expectedOutputFile.getText('UTF-8')
	}

	def 'XML -> JSON - handle empty XML fields and left-right whitespaces'() {
		given:
		this.properties << ['indentFactor':'2']
		this.properties << ['skipRootNode':'Y']
		this.properties << ['trim':'Y']
		this.inputFileName = 'XML2JSON_Scenario1b.xml'
		this.outputFileName = 'XML2JSON_Scenario1b_output.json'

		expect:
		process() == this.expectedOutputFile.getText('UTF-8')
	}

	def 'XML -> JSON - input with umlaut'() {
		given:
		this.properties << ['indentFactor':'2']
		this.properties << ['skipRootNode':'Y']
		this.inputFileName = 'material.xml'
		this.outputFileName = 'material.json'

		expect:
		process() == this.expectedOutputFile.getText('UTF-8')
	}

	def 'XML -> JSON - apply custom field conversions for output in boolean, number and null format'() {
		given:
		this.properties << ['indentFactor':'2']
		this.properties << ['skipRootNode':'Y']
		this.properties << ['fieldConversions':'title:number,SortAs:null,GlossSeeAlso:boolean']
		this.inputFileName = 'XML2JSON_Scenario4.xml'
		this.outputFileName = 'XML2JSON_Scenario4_output.json'

		expect:
		process() == this.expectedOutputFile.getText('UTF-8')
	}
}