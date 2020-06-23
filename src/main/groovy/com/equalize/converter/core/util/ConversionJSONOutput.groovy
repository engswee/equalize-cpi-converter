package com.equalize.converter.core.util

import groovy.json.JsonOutput

class ConversionJSONOutput {

  boolean forceArray = false
  Set<String> arrayFields
  Set<String> arrayGPaths
  Map<String, String> fieldConversions

  String generateJSONText(XMLElementContainer xmlElement, boolean skipRoot, int indentFactor) {
    def jsonObject = new LinkedHashMap()
    constructJSONContentfromXML(jsonObject, xmlElement)
    if (skipRoot) {
      return getJSONText(jsonObject, indentFactor)
    } else {
      Map rootJsonObject = new LinkedHashMap()
      rootJsonObject.put(xmlElement.getElementName(), jsonObject)
      return getJSONText(rootJsonObject, indentFactor)
    }
  }

  String generateJSONText(XMLElementContainer element, boolean skipRoot) {
    return generateJSONText(element, skipRoot, 0)
  }

  private void constructJSONContentfromXML(Map parent, XMLElementContainer element) {
    Map<String, List> jsonArrayTracker = new LinkedHashMap<String, List>()
    // Process all the child fields of the XML element
    element.getChildFields().each { childField ->
      // Check if it is an array first
      int count = element.getChildFieldList().get(childField.fieldName)

      Object fieldContent = childField.fieldContent
      switch (fieldContent) {
        case XMLElementContainer:
          // If it is a segment, create a JSON object for it first before
          // adding into parent
          Map childObject = new LinkedHashMap()
          constructJSONContentfromXML(childObject, (XMLElementContainer) fieldContent)
          putIntoJSONObject(count, jsonArrayTracker, parent, childField.fieldName, childObject)
          break
        default:
          // Check if any field conversion is required
          if (this.fieldConversions.containsKey(childField.fieldName)) {
            switch (this.fieldConversions.get(childField.fieldName)) {
              case 'null':
                putIntoJSONObject(count, jsonArrayTracker, parent, childField.fieldName, null)
                break
              case 'number':
                putIntoJSONObject(count, jsonArrayTracker, parent, childField.fieldName, fieldContent as BigDecimal)
                break
              case 'boolean':
                putIntoJSONObject(count, jsonArrayTracker, parent, childField.fieldName, fieldContent.toBoolean())
                break
            }
          } else
          // If it is a string, directly add it to parent
            putIntoJSONObject(count, jsonArrayTracker, parent, childField.fieldName, fieldContent)
          break
      }
      // If a JSONArray is created for this field and hasn't been added to
      // the parent, then add the JSONArray to the parent
      if (jsonArrayTracker.containsKey(childField.fieldName) && !parent.containsKey(childField.fieldName)) {
        parent.put(childField.fieldName, jsonArrayTracker.get(childField.fieldName))
      }
    }
  }

  private void putIntoJSONObject(int fieldCount, Map<String, List> jsonArrayTracker, Map parent,
                                 String fieldName, Object child) {
    // If it is an array, put it into the corresponding JSON array in the
    // map
    if (fieldCount > 1 || this.forceArray || (this.arrayFields != null && this.arrayFields.contains(fieldName))) {
      List jsonArray = getJSONArray(jsonArrayTracker, fieldName)
      jsonArray.add(child)
    } else {
      // Otherwise directly put it into the parent
      parent.put(fieldName, child)
    }
  }

  private String getJSONText(Map jo, int indentFactor) {
// TODO - Switch to JSON Generator in Groovy 2.5
//    JsonGenerator generator = new JsonGenerator.Options()
//            .excludeNulls()
//            .disableUnicodeEscaping()
//            .build()
    this.forceGPathArrays(jo)
    if (indentFactor)
      return JsonOutput.prettyPrint(JsonOutput.toJson(jo))
    else
      return JsonOutput.toJson(jo)
  }

  private List getJSONArray(Map<String, List> jsonArrayTracker, String arrayName) {
    // Get the current JSONArray for this key or create a new JSONArray
    if (jsonArrayTracker.containsKey(arrayName)) {
      return jsonArrayTracker.get(arrayName)
    } else {
      def jsonArray = []
      jsonArrayTracker.put(arrayName, jsonArray)
      return jsonArray
    }
  }

  private forceGPathArrays(Map rootJsonObject) {
    this.arrayGPaths.each { String gpath ->
      // Get the object at the GPath location
      def object = Eval.me('root', rootJsonObject, "root.${gpath}")
      if (!(object instanceof List)) {
        def parentObj
        def fieldName
        int index = gpath.lastIndexOf('.')
        if (index == -1) {
          parentObj = rootJsonObject
          fieldName = gpath
        } else {
          def parentObjPath = gpath.substring(0, index)
          fieldName = gpath.substring(index+1)
          // Get the parent object
          parentObj = Eval.me('root', rootJsonObject, "root.${parentObjPath}")
        }
        // Replace the object with an array that contains the object
        Eval.xy(parentObj, [object], "x.replace('${fieldName}', y)")
      }
    }
  }
}
