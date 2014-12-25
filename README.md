ClentServer
===========

Network task SPbAU Fall 2014

Requirements
=========
* Java 8
* maven
* protobuf 2.6.1 (https://code.google.com/p/protobuf/)
 
How to compile
=========
* mvn compile

How to run
=========
* mvn exec:java

Configuration
=========
In file pom.xml
```
<configuration>
    <arguments>
        <argument>SERVER_IP_ADDRESS</argument>
        <argument>SERVER_PORT</argument>
        <argument>CLIENTS_COUNT</argument>
        <argument>MATRIX_SIZE</argument>
        <argument>VERBOSE_PARAMETER</argument>
        <argument>CLIENT TYPE (b = blocking, n = nonblocking client)</argument>
    </arguments>
</configuration>
```
