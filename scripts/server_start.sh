#!/bin/sh
currentDir=$(
  cd $(dirname "$0")
  pwd
)

echo script $currentDir
echo "target Path:"
targetDir="$currentDir/../target"
echo "Java version:"
java -version
echo ${targetDir}
cd $targetDir

java -jar EarningManagement-1.0-SNAPSHOT-jar-with-dependencies.jar