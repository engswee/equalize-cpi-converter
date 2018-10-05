# equalize-cpi-converter
CPI Converter

Setting up Clover for Maven (http://openclover.org/doc/manual/latest/maven--quick-start-guide.html)
Update Maven settings - add org.openclover to pluginGroup


Maven command goals
----------------------

1) Clean and test

mvn clean test

2) Test and generate coverage report

mvn clover:instrument-test clover:clover

3) Full process - clean, test, check coverage %, build and install JAR

mvn clean clover:instrument-test clover:check clover:clover install

