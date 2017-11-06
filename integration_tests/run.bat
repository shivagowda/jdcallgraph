echo Run %1

javac -version

# rm -r target
# rm -r result
# mkdir -p target
# mkdir -p result

for /F %%G in (src\com\dkarv\verifier\*.java) do (
    javac -d target %%G
)

for /F %%G in (src\com\dkarv\testcases\%1\*.java) do (
    javac -d target %%G
)

#function run {
#    java -cp target \
#    -javaagent:../jdcallgraph/target/jdcallgraph-0.2-agent.jar=./$2 \
#    com/dkarv/testcases/$1/Main
#    if java -cp target com/dkarv/testcases/$1/Verification ; then
#        echo "Verification of >>  $1 with $2  << succeeded" >&2
#    else
#        echo "Verification of >>  $1 with $2  << failed" >&2
#        exit 1
#    fi
#}

#run $1 "bytebuddy.ini"

#if [[ "$version" < "1.9" ]]; then
#    mv result result2
#    mkdir result

#    run $1 "javassist.ini"

#    if [[ -f "src/com/dkarv/testcases/$1/.nodiff" ]]; then
#        diff -r -X src/com/dkarv/testcases/$1/.nodiff result/cg result2/cg >&2
#    else
#        diff -r result/cg result2/cg >&2
#    fi
#    # diff -r result/ddg result2/ddg
#fi
