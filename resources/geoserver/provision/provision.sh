#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"

. $DIR/common.sh

TMP_FILENAME=`mktemp`

cat $DIR/workspace.json | sed -e "s:WORKSPACE_NAME:$WORKSPACE_NAME:g" > $TMP_FILENAME

curl -u $CREDS \
    --data @$TMP_FILENAME \
    -H "Content-Type: application/json" \
    $GEOSERVER_PREFIX/workspaces

rm $TMP_FILENAME

$DIR/1-prg-layers/up.sh
