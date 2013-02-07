mvn clean jaxb2:xjc  install
mvn exec:java -Dexec.mainClass=com.kevinearls.hudson.SummarizeBuildResults
open workbook.xls
