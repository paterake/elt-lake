
# intelliJ

## 
Apache Spark 3.3.0 breaks on Java 17 with "cannot access class sun.nio.ch.DirectBuffer"
Under VM Options add the line:
--add-exports java.base/sun.nio.ch=ALL-UNNAMED
--add-opens java.base/java.io=ALL-UNNAMED
--add-opens java.base/sun.nio.ch=ALL-UNNAMED
--add-opens java.base/sun.nio.cs=ALL-UNNAMED
