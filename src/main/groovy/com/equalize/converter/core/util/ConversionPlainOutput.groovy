package com.equalize.converter.core.util

class ConversionPlainOutput {

    String generateLineText(List<Field> childFields, String fieldSeparator, String[] fixedLengths,
                            String endSeparator, String fixedLengthTooShortHandling, String enclosureSign,
                            String enclosureSignEscape) throws Exception {
        if (fixedLengths == null) {
            return generateDelimitedLine(childFields, fieldSeparator, endSeparator, enclosureSign, enclosureSignEscape)
        } else {
            return generateFixedLengthLine(childFields, fixedLengths, endSeparator, fixedLengthTooShortHandling)
        }
    }

    private String generateDelimitedLine(List<Field> childFields, String fieldSeparator, String endSeparator,
                                         String enclosureSign, String enclosureSignEscape) {
        StringBuilder sb = new StringBuilder()
        int leafFieldCount = 0
        // Process all child elements that are fields
        for (Field childField : childFields) {
            Object fieldContent = childField.fieldContent
            if (fieldContent instanceof String) {
                if (enclosureSign)
                    fieldContent = encloseFieldContent(fieldContent, enclosureSign, enclosureSignEscape)
                if (leafFieldCount == 0) {
                    sb.append(fieldContent)
                } else {
                    sb.append(fieldSeparator).append(fieldContent)
                }
                leafFieldCount++
            }
        }
        if (leafFieldCount > 0) {
            sb.append(endSeparator)
        }
        return sb.toString()
    }

    private String generateFixedLengthLine(List<Field> childFields, String[] fixedLengths, String endSeparator,
                                           String fixedLengthTooShortHandling) throws ConverterException {
        StringBuilder sb = new StringBuilder()
        int leafFieldCount = 0
        // Process all child elements that are fields
        for (Field childField : childFields) {
            Object fieldContent = childField.fieldContent
            if (fieldContent instanceof String) {
                int fieldLength = Integer.parseInt(fixedLengths[leafFieldCount])
                leafFieldCount++
                String fieldValue = (String) fieldContent
                // Handle case if field value is longer than configured length
                if (fieldValue.length() > fieldLength) {
                    if (fixedLengthTooShortHandling.equalsIgnoreCase('cut')) {
                        fieldValue = fieldValue.substring(0, fieldLength)
                    } else if (fixedLengthTooShortHandling.equalsIgnoreCase('ignore')) {
                        // Do nothing
                    } else {
                        // Default is error
                        throw new ConverterException("Field value '${fieldValue}' longer than allowed length ${fieldLength} for field '${childField.fieldName}'")
                    }
                }
                sb.append(padRight(fieldValue, fieldLength))
            }
        }
        if (leafFieldCount > 0) {
            sb.append(endSeparator)
        }
        return sb.toString()
    }

    private String padRight(String input, int width) {
        return String.format('%1$-' + width + 's', input)
    }

    private String encloseFieldContent(String input, String enclosureSign, String enclosureSignEscape) {
        String enclosedContent = input.replaceAll(enclosureSign, enclosureSignEscape)
        return "${enclosureSign}${enclosedContent}${enclosureSign}"
    }
}
