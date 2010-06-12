#!/bin/sh

mvn -f junit-javaee6/pom.xml clean archetype:create-from-project -Darchetype.properties=../junit-javaee6-archetype.properties
mvn -f junit-javaee6/target/generated-sources/archetype/pom.xml install
