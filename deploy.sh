#!/bin/bash

echo "maven deploy uniquedata-sdk-helper started";

mvn clean

mvn install

mvn deploy -Pgithub-sdk-helper-profile

echo "Finish success deploy!";


