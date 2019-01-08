package com.equalize.cpi.converter

import org.apache.camel.CamelContext
import org.apache.camel.Exchange
import org.apache.camel.impl.DefaultCamelContext
import org.apache.camel.impl.DefaultExchange

/*
 Sample arguments for script 
 C:\Users\yeohe\Desktop\XML2JSON_Scenario1.xml
 C:\Users\yeohe\Desktop\output.txt
 converterClass=com.equalize.converter.core.XML2JSONConverter&indentFactor=2&skipRootNode=Y
 */
if(args.size() < 3)
	throw new RuntimeException('Less than 3 arguments provided')

String inputFileName = args[0]
String outputFileName = args[1]
String propertyList = args[2]

// ----------------------------------------
// Closures
// ----------------------------------------
def decodeQueryString = { String queryString ->
	URI uri = new URI("http://localhost?" + queryString)
	return uri.getQuery()
}
def parseQueryString = { String queryString ->
	Map<String, String> queryPairs = new LinkedHashMap<String, String>()
	String[] pairs = queryString.split("&")
	pairs.each {
		int idx = it.indexOf("=")
		String key = it.substring(0, idx)
		String value = it.substring(idx + 1)
		queryPairs.put(key, decodeQueryString(value))
	}
	return queryPairs
}

// ----------------------------------------
// Setup the Camel context, Camel exchange
// ----------------------------------------
CamelContext context = new DefaultCamelContext()
Exchange exchange = new DefaultExchange(context)
Map<String,Object> properties = [:]

println "Setting input file to $inputFileName"
exchange.getIn().setBody(new File(inputFileName))

if(propertyList) {
	properties << parseQueryString(propertyList)
}
println "Setting properties to $properties"

println "Processing conversion"
def fcb = new FormatConversionBean(exchange, properties)
def outputContent = fcb.convert()

// ----------------------------------------
// Generate output file
// ----------------------------------------
println "Generating output file at $outputFileName:-"
def outputFile = new File(outputFileName)
switch (outputContent) {
	case String:
		outputFile.write outputContent
		println outputContent
		break
	case byte[]:
		outputFile.setBytes(outputContent)
		break
}