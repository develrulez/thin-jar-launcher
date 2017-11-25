#!/usr/bin/env bash
if ([ "$TRAVIS_BRANCH" = 'master' ] || [ "$TRAVIS_BRANCH" = 'develop' ]) && [ "$TRAVIS_PULL_REQUEST" == 'false' ]; then
    ./mvnw --settings .travis/settings.xml clean deploy -B -V -P run-its,code-coverage,sonatype-oss-release
fi