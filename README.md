# equalize-cpi-converter
CPI Converter

Setting up Clover for Maven
Reference - http://openclover.org/doc/manual/latest/maven--quick-start-guide.html

Update Maven settings
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                      https://maven.apache.org/xsd/settings-1.0.0.xsd">
  <pluginGroups>
    <pluginGroup>org.openclover</pluginGroup>
  </pluginGroups>
  <proxies>
    <proxy>
    </proxy>
  </proxies>
</settings>

Maven command
clean clover:setup install clover:aggregate clover:clover
