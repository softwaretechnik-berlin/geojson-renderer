#!/usr/bin/env bash

set -e
set -u
set -o pipefail

mill core.publish \
    --sonatypeCreds $SONATYPE_USER:$SONATYPE_PASSWORD \
    --gpgArgs --passphrase=$GPG_PASSPHRASE,--batch,--yes,-a,-b,--default-key=$GPG_KEY,--pinentry-mode=loopback \
    --release true
