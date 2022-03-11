# cs305-2022
You can open this project in IntelliJ.
If you just want to run it, then you may create the dist ZIP as: 
1. `./gradlew shadowDistZip` (it will create a dist ZIP having an uber/fat jar including all dependencies).
2. Create the DB schema with sample routing table as follows:
```
CREATE TABLE RoutingTable (
  	id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  	sender TEXT NOT NULL,
  	messageType TEXT NOT NULL,
  	destination TEXT NOT NULL,
  	CONSTRAINT RoutingTable_UN UNIQUE (sender,messageType,destination)
  );

  CREATE TABLE MessageLog (
  	id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  	routeId INTEGER NOT NULL,
  	eventType TEXT NOT NULL,
  	eventTime TEXT NOT NULL
  );

  DELETE FROM RoutingTable;
  INSERT INTO RoutingTable
  (sender, messageType, destination)
  VALUES('http://127.0.0.1:8201/foo1', 'COMPRESS', 'http://127.0.0.1:8202/bar1'),
  ('http://127.0.0.1:8201/foo2', 'GET_STOCK_QUOTE', 'http://127.0.0.1:8202/bar2'),
  ('http://127.0.0.1:8202/bar1', 'REPLY_FOO1', 'http://127.0.0.1:8201/foo1'),
  ('http://127.0.0.1:8202/bar1', 'REPLY_FOO2', 'http://127.0.0.1:8201/foo2'),
  ('http://127.0.0.1:8202/bar2', 'REPLY_FOO1', 'http://127.0.0.1:8201/foo1'),
  ('http://127.0.0.1:8202/bar2', 'REPLY_FOO2', 'http://127.0.0.1:8201/foo2')
  ;
  ```
  3. Start the message router service by running `./gradlew run < PUT PORT No. HERE> 'jdbc:sqlite:PATH/TO/mydatabase.db'` 
  4. Start the dummy senders/receivers: `java -cp <PATH TO>/MyJavalin-1.0-SNAPSHOT-all.jar dev.cs305.DummyDestination ~/temp/cs305-2022/cs305-midsem/config.json`

Following is an example run:
```
someuser@OX:~/temp/cs305-2022$ cd cs305-midsem/
someuser@OX:~/temp/cs305-2022/cs305-midsem$ ./gradlew clean

BUILD SUCCESSFUL in 644ms
1 actionable task: 1 executed
someuser@OX:~/temp/cs305-2022/cs305-midsem$ ./gradlew shadowDistZip

BUILD SUCCESSFUL in 3s
5 actionable tasks: 5 executed
someuser@OX:~/temp/cs305-2022/cs305-midsem$ cd build/distributions/
someuser@OX:~/temp/cs305-2022/cs305-midsem/build/distributions$ unzip Router-shadow-1.0.zip 
Archive:  Router-shadow-1.0.zip
   creating: Router-shadow-1.0/
   creating: Router-shadow-1.0/lib/
  inflating: Router-shadow-1.0/lib/Router-1.0-all.jar  
   creating: Router-shadow-1.0/bin/
  inflating: Router-shadow-1.0/bin/Router.bat  
  inflating: Router-shadow-1.0/bin/Router  
someuser@OX:~/temp/cs305-2022/cs305-midsem/build/distributions$ cd Router-shadow-1.0
someuser@OX:~/temp/cs305-2022/cs305-midsem/build/distributions/Router-shadow-1.0$ 
someuser@OX:~/temp/cs305-2022/cs305-midsem/build/distributions/Router-shadow-1.0$ java -cp ./lib/Router-1.0-all.jar dev.cs305.DummyDestination
!!! Required CLI missing: Path to config.json file.
someuser@OX:~/temp/cs305-2022/cs305-midsem/build/distributions/Router-shadow-1.0$ java -cp ./lib/Router-1.0-all.jar dev.cs305.MessageRouter
Required two params: 1) port number for this service, 2) DB URL

someuser@OX:~/temp/cs305-2022/cs305-midsem/build/distributions/Router-shadow-1.0$
```
