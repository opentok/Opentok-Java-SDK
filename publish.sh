#!/bin/bash

. ./secret.sh

./gradlew clean publish --info --build-cache \
  -PisReleaseVersion=1 \
  -Psigning.keyId="${GPG_KEYID}" \
  -Psigning.password="${GPG_PASSWORD}" \
  -Psigning.secretKeyRingFile="${HOME}/.gnupg/secring.gpg" \
  -PossrhUsername="${OSSRH_USERNAME}" \
  -PossrhPassword="${OSSRH_PASSWORD}"
