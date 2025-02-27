#!/bin/bash

cd /Users/jadersonberti/workspace/uniquedata-sdk-helper;

echo "maven clean install deploy uniquedata-sdk-helper starting ...";

mvn clean

mvn install

echo "===================================================================================="
echo "===================================================================================="
echo ""
echo "maven deploy for maven [central.sonatype.org] > uniquedata-sdk-helper starting ...";
echo ""
echo "===================================================================================="
echo "===================================================================================="

sleep 1;

mvn deploy -U

echo "Finish success deploy!";


