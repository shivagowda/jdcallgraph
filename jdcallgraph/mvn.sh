GIT_REPO_ROOT=/Users/shiv/git/portal
CG_FILE="/Users/shiv/callgraph/portal/cg/coverage.csv"
JAVA_AGENT="/Users/shiv/.m2/repository/com/dkarv/jdcallgraph/0.2/jdcallgraph-0.2.jar"



rm -rf $GIT_REPO_ROOT/12-commons/target/surefire-reports/*
CONFIG_FILE="$GIT_REPO_ROOT/config.ini"
rm $CG_FILE
mvn clean test -Dmaven.test.failure.ignore=true  -DargLine="-javaagent:$JAVA_AGENT=$CONFIG_FILE"

echo "Location of call-graph file: $CG_FILE"

