#!/usr/bin/env bash

#
# Copyright 2020 ACC Cyfronet AGH
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
#

function usage () {
  echo "Copies scenes with metadata and touches other artifacts in specified location"
  echo "Usage: $0 <base_path> <from> <to>"
  echo "Example arguments: ~/tmp/pr1/ ~/tmp/pr1/Sentinel-1/GRDH/2020-01-01/ ~/src/sat4envi/tmp/minio-data-3/dataset-1/"
  exit 1
}

if [ $# -ne 3 ]; then
  usage
fi

base_path=$1
from=$2
to=$3

for path in $(find $from -name "*.scene"); do
  echo $path
  # Copy the scene file, creating non-existent directories if needed
  relative_path=$(realpath --relative-to=$base_path $path)
  absolute_to=$(realpath -m $to/$relative_path)
  absolute_to_dir=$(realpath -m $absolute_to/..)
  mkdir -p $absolute_to_dir
  cp $path $absolute_to_dir

  for artifact_path in $(jq -r '.artifacts[]' $path); do
    # Touch all the artifacts
    absolute_to=$(realpath -m $to/$artifact_path)
    absolute_to_dir=$(realpath -m $absolute_to/..)
    mkdir -p $absolute_to_dir
    touch $absolute_to
  done

  # Actually copy the metadata file
  metadata_path=$(jq -r '.artifacts.metadata' $path)
  absolute_to=$(realpath -m $to/$metadata_path)
  absolute_to_dir=$(realpath -m $absolute_to/..)
  cp $base_path/$metadata_path $absolute_to_dir
done
