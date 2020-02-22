package com.equalize.converter.core.util

import groovy.json.JsonSlurper

class ConversionJSONInput {
  private final Map topElement

  ConversionJSONInput(Reader reader) {
    this.topElement = new JsonSlurper().parse(reader) as Map
  }

  ConversionJSONInput(Reader reader, String topArrayName) {
    this.topElement = ["${topArrayName}": new JsonSlurper().parse(reader)]
  }

  List<Field> extractJSONContent() {
    def parseJSON
    parseJSON = { Object element ->
      switch (element) {
        case Map:
          def childElements = []
          element.each { key, value ->
            if (value != null)
              childElements << new Field(key, parseJSON.trampoline(value).call())
            else
              childElements << new Field(key, null)
          }
          return childElements
        case List:
          List childElements = []
          element.each {
            if (it != null)
              childElements << parseJSON.trampoline(it).call()
            else
              childElements << null
          }
          return childElements.toArray()
        default:
          return element
      }
    }.trampoline()

    return parseJSON(this.topElement) as List
  }
}