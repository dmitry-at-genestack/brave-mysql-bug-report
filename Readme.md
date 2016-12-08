Test for reproducing bug in [openzipkin/brave/brave-mysql](https://github.com/openzipkin/brave/tree/master/brave-mysql).

Contains 2 tests, each doing batch insert of multiple (int, string) pairs in a MySQL table.
Failing test uses `rewriteBatchedStatements=true` JDBC connection parameter, 
passing test sets `rewriteBatchedStatements=false`.

First test does not fail for batch insert of integers (without strings).
 
Test uses [embedded MariaDB](https://github.com/vorburger/MariaDB4j), 
but this problem also happens with Oracle MySQL 5.7.13.

[MySQL Connector/J](https://dev.mysql.com/downloads/connector/j/) version 5.1.40 (latest GA) used.
