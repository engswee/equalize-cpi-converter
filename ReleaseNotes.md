# Release Notes

----------------------------------------------------------------------------------------------------
2.4.0 - Released 5 Jun 2025
----------------------------------------------------------------------------------------------------
i) Update Camel version to 3.14.7 to be synchronised with tenant versions

ii) Update Apache POI library (used for Excel conversion) to 5.4.0

iii) Add new parameter `password` for Excel2XMLConverter to handle reading password protected Excel files

----------------------------------------------------------------------------------------------------
2.3.0 - Released 12 Mar 2021
----------------------------------------------------------------------------------------------------
i) Update Groovy and Camel versions to be synchronised with tenant versions
- Groovy > 2.4.21
- Camel > 2.24.2

----------------------------------------------------------------------------------------------------
2.2.1 - Released 20 Jan 2021
----------------------------------------------------------------------------------------------------
i) Fix encoding issue for DeepPlain2XML conversion, where it uses default platform encoding which
   potentially is not always UTF-8, therefore the content extracted is incorrect. Example, Nordic
   characters ö, ä

----------------------------------------------------------------------------------------------------
2.2.0 - Released 23 Jun 2020
----------------------------------------------------------------------------------------------------
i) Switch JSON parsing in JSON2XMLConverter to use built-in JsonSlurper in Groovy

ii) Switch JSON generation in XML2JSONConverter to use built-in JsonOutput in Groovy

iii) Remove dependency on org.json library

iv) Fix issue #5 - add parameter 'arrayGPathList' to XML2JSONConverter
- this enables forcing of specific nodes to a JSON Array based on GPath notation

----------------------------------------------------------------------------------------------------
2.1.1 - Released 5 Nov 2019
----------------------------------------------------------------------------------------------------
i) Fix issue #4 - XML text nodes no longer automatically trimmed as they are considered
   significant whitespace

ii) Add parameters 'trim' (defaulted to N) to all XML2xxx converters
- this enables trimming of XML text nodes when set to Y

----------------------------------------------------------------------------------------------------
2.1.0 - Released 29 May 2019
----------------------------------------------------------------------------------------------------
i) Add parameters 'defaultEnclosureSign' & 'defaultEnclosureSignEscape' to XML2DeepPlainConverter
- this enables generating delimited flat files with an enclosure sign

----------------------------------------------------------------------------------------------------
2.0.0 - Released 13 May 2019
----------------------------------------------------------------------------------------------------
i) Update to JDK 8 to synchronise with JRE/JVM version update on CPI tenant

ii) Update Apache POI library (used for Excel conversion) to 4.1.0 (which requires Java 8)

iii) Update miscellaneous other libraries - JUnit, Maven, OpenClover, GMavenPlus

----------------------------------------------------------------------------------------------------
1.4.0 - Released 1 May 2019
----------------------------------------------------------------------------------------------------
i) Add new parameter 'fieldConversions' to XML2JSONConverter
- by default JSON output are generated as string, using this new parameter, the configured
      fields can be generated as number, boolean or null

----------------------------------------------------------------------------------------------------
1.3.1 - Released 17 Apr 2019
----------------------------------------------------------------------------------------------------
i) Revert CamelClassTypeConverter back to Java class due to runtime issue reported by multiple users
   caused by java.lang.reflect.MalformedParameterizedTypeException error

----------------------------------------------------------------------------------------------------
1.3.0 - Released 8 Apr 2019
----------------------------------------------------------------------------------------------------
i) Update Groovy libraries based on CPI's latest version update (~ Mar 2019)
- groovy-all => 2.4.12
- spock-core => 1.3-groovy-2.4

ii) Misc Java and Groovy code cleanup based on IntelliJ IDEA code inspection results

iii) Convert CamelClassTypeConverter to Groovy class as previously it wasn't working
     due to Reflection API issue on older Groovy version

iv) Add IntelliJ IDEA files to .gitignore

v) Add Codecov YAML file to disable coverage check on patch

----------------------------------------------------------------------------------------------------
1.2.0 - Released 2 Nov 2018
----------------------------------------------------------------------------------------------------
i) Switch default XML parsing from DOM to SAX-based XmlSlurper
- XML2DeepPlainConverter
- XML2ExcelConverter
- XML2JSONConverter

ii) Restructure project folder to root of Git repository

iii) Added Travis CI for Continuous Integration

iv) Misc minor fixes

----------------------------------------------------------------------------------------------------
1.1.0 - Released 2 Oct 2018
----------------------------------------------------------------------------------------------------
Complete migration of Excel converters for CPI

i) Migrated Excel <> XML converters. Includes new Excel related classes:-
- ConversionExcelInput
- ConversionExcelOutput

ii) Added endSeparator parameter for DeepPlain2XML

iii) Incorporated OpenClover for code coverage, along with updated Spock tests and misc refactoring

----------------------------------------------------------------------------------------------------
1.0.0 - Released 4 Sep 2018
----------------------------------------------------------------------------------------------------
Initial release of FormatConversionBean for SAP Cloud Platform Integration (CPI)

Includes following converters

i) XML <> JSON

ii) Base64 Decoder/Encoder

iii) Plain <> XML