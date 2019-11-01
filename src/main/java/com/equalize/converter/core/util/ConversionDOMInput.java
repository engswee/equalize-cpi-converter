package com.equalize.converter.core.util;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ConversionDOMInput {
	private final Document doc;
	private XPathFactory xpathFac;
	private boolean trim;

	public ConversionDOMInput(InputStream inStream) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		this.doc = docBuilder.parse(inStream);
	}

	public Document getDocument() {
		return this.doc;
	}

	public XMLElementContainer extractDOMContent(boolean trim) {
		this.trim = trim;
		Node root = this.doc.getDocumentElement();
		return (XMLElementContainer) parseNode(root);
	}

	public Node evaluateXPathToNode(String xpath) throws XPathExpressionException {
		if (this.xpathFac == null) {
			this.xpathFac = XPathFactory.newInstance();
		}
		XPath xp = this.xpathFac.newXPath();
		XPathExpression xpe = xp.compile(xpath);
		return (Node) xpe.evaluate(this.doc, XPathConstants.NODE);
	}

	public String evaluateXPathToString(String xpath) throws XPathExpressionException, ConverterException {
		Node node = evaluateXPathToNode(xpath);
		if (node == null) {
			throw new ConverterException("XPath " + xpath + " does not exist");
		}
		return node.getTextContent();
	}

	private Object parseNode(Node node) {
		boolean hasChildElements = false;
		String textContent = "";

		// Recursively parse the children nodes
		XMLElementContainer element = new XMLElementContainer(node.getNodeName());
		NodeList nl = node.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node child = nl.item(i);
			switch (child.getNodeType()) {
			case Node.ELEMENT_NODE:
				hasChildElements = true;
				element.addChildField(child.getNodeName(), parseNode(child));
				break;
			case Node.TEXT_NODE:
				textContent = child.getNodeValue();
				break;
			}
		}
		// If an element node has no further child element nodes, then it is a
		// leaf node
		// If it has child text node, then it should extract that text node
		if (!hasChildElements)
			return this.trim ? textContent.trim() : textContent;
		else
			return element;
	}
}
