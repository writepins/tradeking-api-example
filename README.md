# tradeking-api-example
Example how to use Java library for Tradeking API (https://github.com/lrimkus/tradeking-api-consumer)

Documentation of this library: http://www.miserablemind.com/tradeking-api-client-docs/

It consists of three files: 

1. pom.xml for maven to import the library
2. credentials.properties to enter your access and consumer tokens with passwords
3. Example class that demonstrates some of library's functionality (including the streaming part)


How to run it:

1. Make sure you have maven installed
2. Clone this repository
3. ```cd tradeking-api-example; vim src/main/resources/credentials.properties```
4. Enter your api credentials that you need to get from TradeKing. Exit editor (:wq)
5. ```mvn clean package exec:java -Dexec.mainClass="Example"```
