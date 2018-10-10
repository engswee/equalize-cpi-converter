# equalize-cpi-converter
CPI Converter

[![Build Status](https://travis-ci.org/engswee/equalize-cpi-converter.svg?branch=master)](https://travis-ci.org/engswee/equalize-cpi-converter)
[![codecov](https://codecov.io/gh/engswee/equalize-cpi-converter/branch/master/graph/badge.svg)](https://codecov.io/gh/engswee/equalize-cpi-converter)
[![GitHub release](https://img.shields.io/github/release/engswee/equalize-cpi-converter.svg)](https://github.com/engswee/equalize-cpi-converter/releases/latest)
[![Github All Releases](https://img.shields.io/github/downloads/engswee/equalize-cpi-converter/total.svg)](https://www.somsubhra.com/github-release-stats/?username=engswee&repository=equalize-cpi-converter)
[![License: MIT](https://img.shields.io/badge/License-MIT-orange.svg)](https://github.com/engswee/equalize-cpi-converter/blob/master/LICENSE)

Setting up Clover for Maven (http://openclover.org/doc/manual/latest/maven--quick-start-guide.html)
Update Maven settings - add org.openclover to pluginGroup


Maven command goals
----------------------

1) Clean and test

mvn clean test

2) Test and generate coverage report

mvn initialize clover:instrument-test clover:clover

3) Full process - clean, test, check coverage %, build and install JAR

mvn clean initialize clover:instrument-test clover:check clover:clover install

