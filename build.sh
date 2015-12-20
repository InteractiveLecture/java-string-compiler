#!/bin/bash
./gradlew clean build
if [ "${TRAVIS_PULL_REQUEST}" = "false" ] && [ "${TRAVIS_REPO_SLUG}" = "InteractiveLecture/string-compiler" ] ; then
  ./gradlew bintrayUpload
fi
