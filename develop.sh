#!/bin/bash

mvn versions:update-child-modules
mvn versions:set -DnewVersion=$1
