# Essayist

A blogging app for the tent.io protocol

## Quick start:

1. Pre-requisites: Java 1.6+ and [Maven](http://maven.apache.org)
1. Currently, Essayist uses some dependencies available only on Github: [TentClient](https://github.com/mwanji/tent-client-java), [Migrate4J](https://github.com/mwanji/migrate4j-maven) and [tent-text-java](https://github.com/mwanji/tent-text-java). Clone and `mvn install` each of these.
1. `git clone git://github.com/mwanji/essayist.git`
1. Deploy to your favourite servlet container.
1. [http://localhost:8080](http://localhost:8080)
1. Log in with your Tent address, for example https://my_name.tent.is

## Configuration

1. Put a file called essayist.properties in the src/main/resources folder. `essayist-example.properties` shows what can be configured. `essayist-defaults.properties` provides a number of defaults.
1. If using your own database, create an empty database corresponding to the value of db.url
