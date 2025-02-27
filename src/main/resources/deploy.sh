#!/bin/bash

echo "maven clean install deploy uniquedata-sdk-helper starting ...";

mvn clean

mvn install

echo "===================================================================================="
echo "===================================================================================="
echo ""
echo "maven deploy for github uniquedata-sdk-helper starting ...";
echo ""
echo "===================================================================================="
echo "===================================================================================="

sleep 1;

mvn deploy -Pgithub-sdk-helper-profile

echo "===================================================================================="
echo "===================================================================================="
echo ""
echo "maven deploy for maven [central.sonatype.org] > uniquedata-sdk-helper starting ...";
echo ""
echo "===================================================================================="
echo "===================================================================================="

sleep 1;

mvn deploy 

echo "Finish success deploy!";


