mvn clean jaxb2:xjc  install
mvn exec:java -Dexec.mainClass=com.kevinearls.hudson.SummarizeBuildResults -Dexec.args=/Users/kearls/mytools/HudsonResultsAnalyzer/jobs
#open result.html
#ls -tr result*.html | tail -1 | xargs open
