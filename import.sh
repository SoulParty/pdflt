#!/usr/bin/env bash
mvn clean install
mvn install:install-file -DgroupId=lt.nortal.pdflt -DartifactId=pdflt -Dversion=0.1 -Dpackaging=jar -Dfile=target/pdflt-0.1.jar