package com.equalize.xpi.util.converter;

import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;

public class NamespaceContextImpl implements NamespaceContext {
    private Map<String, String> namespaceMapping;
    
    public NamespaceContextImpl(Map<String, String> namespaceMapping) {
      this.namespaceMapping = namespaceMapping;
    }
    
	@Override
	public String getNamespaceURI(String prefix) {
		return (String)this.namespaceMapping.get(prefix);
	}

	@Override
	public String getPrefix(String namespaceURI) {
		return (String)this.namespaceMapping.get(namespaceURI);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Iterator getPrefixes(String namespaceURI) {
		return null;
	}
}
