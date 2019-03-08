#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"

if [ -z $GRANULE_NAME ]; then
    echo "Specify granule name"
    exit 1
    GRANULE_NAME="201810040800_Merkator_Europa_ir_108m"
fi

. $DIR/../common.sh
STORE_NAME=$GRANULE_NAME
LAYER_NAME=$GRANULE_NAME
S3_URL="cyfro\:\/\/s4e-test-1\/"$GRANULE_NAME".tif"
TMP_FILENAME=`mktemp`

cat $DIR/store.json | sed -e "s:WORKSPACE_NAME:$WORKSPACE_NAME:g" \
                     -e "s:STORE_NAME:$STORE_NAME:g" \
                     -e "s:S3_URL:$S3_URL:g" > $TMP_FILENAME

curl -u $CREDS \
    --data @$TMP_FILENAME \
    -H "Content-Type: application/json" \
    $GEOSERVER_PREFIX/workspaces/$WORKSPACE_NAME/coveragestores

cat $DIR/layer.json | sed -e "s:WORKSPACE_NAME:$WORKSPACE_NAME:g" \
                     -e "s:STORE_NAME:$STORE_NAME:g" \
                     -e "s:LAYER_NAME:$LAYER_NAME:g" > $TMP_FILENAME

curl -u $CREDS \
    --data @$TMP_FILENAME \
    -H "Content-Type: application/json" \
    $GEOSERVER_PREFIX/workspaces/$WORKSPACE_NAME/coveragestores/$STORE_NAME/coverages

rm $TMP_FILENAME
