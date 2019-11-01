package com.equalize.converter.core.util

import groovy.util.slurpersupport.GPathResult
import groovy.util.slurpersupport.Node

class ConversionSAXInput {
	final GPathResult root

	ConversionSAXInput(Reader reader) {
		this.root = new XmlSlurper().parse(reader)
	}

	XMLElementContainer extractXMLContent(boolean trim) {
		// Define recursive trampoline closure for parsing child nodes
		def parseNode
		parseNode = { Node node, XMLElementContainer parentElement ->
			XMLElementContainer element
			// It's a segment node if it has further nodes under it
			boolean hasChildNode = node.children.any { it instanceof Node }
			if (hasChildNode) {
				element = new XMLElementContainer(node.name())
				parentElement.addChildField(node.name(), element)
			}
			// If there are no more children under it, then it's a leaf node with empty content
			if (node.children.size() == 0)
				parentElement.addChildField(node.name(), '')
			else {
				// Otherwise either process the child recursively, or extract the text content
				node.children.each {
					switch (it) {
						case Node:
							parseNode.trampoline(it, element).call()
							break
						case String:
							parentElement.addChildField(node.name(), trim ? it.trim() : it)
							break
					}
				}
			}
		}.trampoline()

		// Parse root node and process all child nodes
		XMLElementContainer rootElement = new XMLElementContainer(this.root.name())
		Iterator iter = this.root.childNodes()
		while(iter.hasNext()) {
			Node child = iter.next()
			parseNode(child, rootElement)
		}
		return rootElement
	}
}
