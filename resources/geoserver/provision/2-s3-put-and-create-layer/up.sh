#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"

#TIFF_PATHS=`find ~/tmp/sat4envi-prometheus/MSG_Products/Merkator_Europa_WV-IR/20181004 -name "*20181004*00_*"`

#s3cmd put $TIFF_PATHS s3://s4e-test-1

for GRANULE_NAME_SUFFIX in {"_Merkator_Europa_ir_108m","_Merkator_Europa_ir_108_setvak","_Merkator_WV-IR"}; do
    for T in {201810040000..201810042300..100}; do
        GRANULE_NAME=$T$GRANULE_NAME_SUFFIX
        echo "Creating store and layer for $GRANULE_NAME"
        GRANULE_NAME=$GRANULE_NAME $DIR/../1-create-store/create-store-and-layer.sh
        echo ""
    done
done
