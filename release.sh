#!/bin/bash
# Release script

# Exit whenever a command fails
set -e

if [ $# != 2 ]; then
  echo "usage: ./release.sh <releaseVersion> <developmentVersion>"
  exit
fi

RELEASE_VERSION=$1
DEVEL_VERSION=$2
TAG=v$RELEASE_VERSION

read -r -p "Release details:
* releaseVersion = $RELEASE_VERSION
* developmentVersion = $DEVEL_VERSION
Are you sure? [y/N]: " response
response=$(echo $response | tr '[:upper:]' '[:lower:]') # tolower

if [[ $response =~ ^(yes|y) ]]; then
	git checkout main
	git pull origin main
	mvn -B -DscmCommentPrefix=":bookmark: " release:prepare -DreleaseVersion=$RELEASE_VERSION -DdevelopmentVersion=$DEVEL_VERSION
	mvn release:perform
	echo "Done!"
else
	echo "Canceled!"
fi
