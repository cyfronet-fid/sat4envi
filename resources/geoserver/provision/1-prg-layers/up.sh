#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"

. $DIR/../common.sh

STYLE_NAME="wojewodztwa"
PRG_STORE_NAME="prg"
PRG_GEOSERVER_PATH="/opt/geoserver/prg/"

curl -u $CREDS \
    --data @$DIR/$STYLE_NAME".json" \
    -H "Content-Type: application/json" \
    $GEOSERVER_PREFIX/styles

curl -u $CREDS \
    -X PUT \
    --data @$DIR/$STYLE_NAME".sld" \
    -H "Content-Type: application/vnd.ogc.sld+xml" \
    $GEOSERVER_PREFIX/styles/$STYLE_NAME

curl -u $CREDS \
    -XPUT \
    -H "Content-Type: text/plain" \
    -d "file://"$PRG_GEOSERVER_PATH \
    $GEOSERVER_PREFIX/workspaces/$WORKSPACE_NAME/datastores/$PRG_STORE_NAME/external.shp?configure=all

curl -u $CREDS \
    -XPUT \
    -H "Content-Type: application/json" \
    -d "{layer:{defaultStyle:{name:'wojewodztwa'}}}" \
    "$GEOSERVER_PREFIX/layers/$WORKSPACE_NAME:wojew%25C3%25B3dztwa"

