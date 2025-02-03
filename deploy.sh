#!/bin/bash

echo "maven deploy uniquedata-sdk-helper started";

mvn clean install -Pgithub-sdk-helper-profile

mvn clean deploy -Pgithub-sdk-helper-profile -X

echo "Finish success deploy!";


