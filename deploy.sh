#!/usr/bin/env bash
# This script pushes to maven central.
#
# For authentication it relies on a nubmer of environment variables:
# SONATYPE_USER: User used for the sonatype nexus instance.
# SONATYPE_PASSWORD: Password used for the sonatype nexus instance.
# GPG_KEY: The gpg key to be used to sign the artifacts.
# GPG_PASSPHRASE: Passphrase for the gpg key.

if [[ ! -v SONATYPE_USER ]]; then
   echo SONATYPE_USER needs to be specified in environment. >&2
fi

if [[ ! -v SONATYPE_PASSWORD ]]; then
   echo SONATYPE_PASSWORD needs to be specified in environment. >&2
fi

if [[ ! -v GPG_KEY ]]; then
   echo GPG_KEY needs to be specified in environment. >&2
fi

if [[ ! -v GPG_PASSPHRASE ]]; then
   echo GPG_PASSPHRASE needs to be specified in environment. >&2
fi

set -e
set -u
set -o pipefail

mill core.publish \
    --sonatypeCreds $SONATYPE_USER:$SONATYPE_PASSWORD \
    --gpgArgs --passphrase=$GPG_PASSPHRASE,--batch,--yes,-a,-b,--default-key=$GPG_KEY,--pinentry-mode=loopback \
    --release true
