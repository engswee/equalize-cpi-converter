# Equalize CPI Converter a.k.a. FormatConversionBean for CPI

[![Build Status](https://travis-ci.org/engswee/equalize-cpi-converter.svg?branch=master)](https://travis-ci.org/engswee/equalize-cpi-converter)
[![Codecov](https://img.shields.io/codecov/c/github/engswee/equalize-cpi-converter.svg)](https://codecov.io/gh/engswee/equalize-cpi-converter)
[![GitHub release](https://img.shields.io/github/release/engswee/equalize-cpi-converter.svg)](https://github.com/engswee/equalize-cpi-converter/releases/latest)
[![Github All Releases](https://img.shields.io/github/downloads/engswee/equalize-cpi-converter/total.svg)](https://www.somsubhra.com/github-release-stats/?username=engswee&repository=equalize-cpi-converter)
[![License: MIT](https://img.shields.io/badge/License-MIT-orange.svg)](https://github.com/engswee/equalize-cpi-converter/blob/master/LICENSE)
[![HitCount](http://hits.dwyl.io/engswee/equalize-cpi-converter.svg)](http://hits.dwyl.io/engswee/equalize-cpi-converter)

FormatConversionBean is a collection of converters for transforming data content from one format to another, e.g. JSON to XML, XML to Excel, etc. It was originally developed as a [complementary open-source solution for SAP Process Integration (PI)](https://blogs.sap.com/2015/03/25/formatconversionbean-one-bean-to-rule-them-all/) and is now [also available for SAP Cloud Platform Integration (CPI)](https://blogs.sap.com/2018/09/04/formatconversionbean-arrives-in-cpi/).

## Getting Started

If your intention is to use the converters as-is, then it is as easy as downloading the [latest release](https://github.com/engswee/equalize-cpi-converter/releases/latest) and configuring the CPI integration flow as detailed in this [blog post](https://blogs.sap.com/2018/09/04/formatconversionbean-arrives-in-cpi/).

The following are only applicable if you plan to either enhance the converters privately or collaborate publicly on this project.

### Prerequisites

[Maven](http://maven.apache.org/) or [M2Eclipse](http://www.eclipse.org/m2e/) - for dependency management and build automation

[Groovy Eclipse](https://github.com/groovy/groovy-eclipse) - for Groovy development in Eclipse

### Installation

The following steps will get you a copy of the project up and running on your local machine for development and testing purposes. This assumes the development will be done in an Eclipse environment.


1. Fork this Git repository
2. Clone the forked Git repository into your local machine
3. Import Maven project into Eclipse
4. Execute Maven command ```mvn clean install```

## Maven Goals

Following are some of the common Maven goals used in this project:-

1. Clean up output directory and execute unit tests

```
mvn clean test
```

2. Execute unit tests using OpenClover to instrument and generate code coverage report

```
mvn initialize clover:instrument-test clover:clover
```

3. Full cycle - clean, test, check coverage %, build and install JAR

```
mvn clean initialize clover:instrument-test clover:check clover:clover install
```

## Deployment

To deploy this into a CPI tenant:-

1. Generate JAR file
2. Upload into a CPI integration flow as an Archive

## Built With

* [Maven](https://maven.apache.org/) - Used for dependency management and build automation
* [Spock](http://spockframework.org/) - Used as testing and specification framework
* [GMavenPlus](https://github.com/groovy/GMavenPlus) - Used to integrate Groovy into Maven projects
* [OpenClover](http://openclover.org/) - Used to generate coverage for Java and Groovy source codes
* [Travis CI](https://travis-ci.org/) - Used for Continuous Integration
* [CodeCov](https://codecov.io/) - Used to compile OpenClover code coverage statistics for GitHub

## Versioning

[SemVer](https://semver.org/) is used for versioning.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details
