logcapture
=========================

[![Build Status](https://api.travis-ci.org/mp911de/logcapture.svg)](https://travis-ci.org/mp911de/logcapture)

Capture log entries during your integration test and correlate those with you your integration tests.

Supported are log4j and JUL logging.


Configuration
--------------
Using System-Properties, JNDI or Context-Parameters.

Keys:
* logcapture-configuration: Store-specific configuration.
    * In-Memory-Store: not needed
    * Redis: Redis-URL, like redis://(credential@)HOST(:port)(/database)
* logcapture-format-pattern: Log-framework specific format pattern

API
--------------

* SOAP API
* REST API (to come)

JBoss AS7/Wildfly 8
--------------

Grab the final WAR `logcapture-jbossas7x.war` and drop it into your JBoss. Done.
For Redis: Add a system-property `logcapture-configuration` containing a Redis-URI (redis://host:port/database).

Including it in your project
--------------

Maven:

    <dependency>
        <groupId>biz.paluch.logging</groupId>
        <artifactId>logcapture-common</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>

    <dependency>
        <groupId>biz.paluch.logging</groupId>
        <artifactId>logcapture-inmemory</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>

    <dependency>
        <groupId>biz.paluch.logging</groupId>
        <artifactId>logcapture-redis</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>

    <dependency>
        <groupId>biz.paluch.logging</groupId>
        <artifactId>logcapture-soap-api</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>

    <dependency>
        <groupId>biz.paluch.logging</groupId>
        <artifactId>logcapture-client</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>


Versions/Dependencies
--------------
This project is built against following dependencies/versions:

* json-simple 1.1.1
* log4j 1.2.14
* Java Util Logging JDK Version 1.6
* lettuce 3.0.1.Final


License
-------
* [The MIT License (MIT)] (http://opensource.org/licenses/MIT)

Contributing
-------
Github is for social coding: if you want to write code, I encourage contributions through pull requests from forks of this repository. 
Create Github tickets for bugs and new features and comment on the ones that you are interested in.


