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
  echo "Seeds SOK instance based on configuration in specified path"
  echo "Usage: $0 <api_endpoint> <configuration_path>"
  echo "Example arguments: http://localhost:4201 ./s4e-backend/seed-configs/seed-1"
  exit 1
}

function handle_error () {
  if [ $(grep "HTTP/1.1 200" $temp_file | wc -l) -ne 1 ]; then
    echo $1
    cat $temp_file
    echo
    exit 1
  fi
}

function wait-for-url() {
    echo "Testing $1"
    timeout -s TERM 45 bash -c \
    'while [[ "$(curl -s -o /dev/null -L -w ''%{http_code}'' ${0})" != "200" ]];\
    do echo "Waiting for ${0}" && sleep 2;\
    done' ${1}
    echo "OK!"
    curl -I $1
}

if [ $# -ne 2 ]; then
  usage
fi

api_endpoint=$1
configuration_path=$2

payload=$(
  jq -n '$ARGS.named' \
    --arg email ${ADMIN_EMAIL-admin@mail.pl} \
    --arg password ${ADMIN_PASSWORD-adminPass20}
)
echo -n "Authenticating"
timeout_threshold=$(expr $(date +%s) + ${TIMEOUT-60})
url=$api_endpoint/api/v1/token
# Wait for the backend to available, as this script is used in CI workflow
while : ; do
  result=$(curl --silent --fail \
    --url $url \
    --header "Content-Type: application/json" \
    --data "$payload")
  err=$?
  if [ $err -ne 0 ]; then
    if [ $(date +%s) -gt $timeout_threshold ]; then
      echo
      echo "Call to $url returned an error: curl return value $err"
      exit 1
    else
      echo -n "."
      sleep 1
    fi
  else
    echo
    break
  fi
done
unset timeout_threshold err

auth_token=$(echo $result | jq -r '.token')
unset payload result err

temp_file=$(mktemp)
bucket=${BUCKET-local-dataset-1}
echo "Setting bucket to '$bucket'"
payload=$(
  jq -n '$ARGS.named' \
    --arg value "mailto://$bucket/"
)
url=$api_endpoint/api/admin/properties/scene_granule_path_prefix
curl --silent --include --url $url \
  --request PUT \
  --header "Content-Type: application/json" \
  --header "Authorization: Bearer $auth_token" \
  --data "$payload" > $temp_file
# Fail if there was error
handle_error "Call to $url returned an error:"
unset payload

# Create schemas
echo "Creating schemas"
for schema_path in $(ls $configuration_path/schema); do
  # Get required parameters
  name=$schema_path
  type=$([[ $schema_path == *".scene."* ]] && echo "SCENE" || echo "METADATA")
  content=$(<$configuration_path/schema/$schema_path)
  echo "  Creating $name"
  # Construct payload
  payload=$(
    jq -n '$ARGS.named' \
      --arg name $name \
      --arg type $type \
      --arg content "$content"
  )
  # Send request, saving headers and http status codes to $temp_file
  url=$api_endpoint/api/admin/schemas
  curl --silent --include --url $url \
    --header "Content-Type: application/json" \
    --header "Authorization: Bearer $auth_token" \
    --data "$payload" > $temp_file
  # Fail if there was error
  handle_error "Call to $url returned an error for $schema_path:"
  unset name type content payload
done

# Create categories
echo "Creating categories"
url=$api_endpoint/api/admin/product-category/seed
curl --silent --include --url $url \
  --request POST \
  --header "Content-Type: application/json" \
  --header "Authorization: Bearer $auth_token" > $temp_file
# Fail if there was error
handle_error "Call to $url returned an error:"

# Create products
echo "Creating products"
count=$(jq 'length' < $configuration_path/products.json)
url=$api_endpoint/api/admin/products
for (( i=0; i<count; i++)); do
  product_name=$(jq -r ".[$i].name" $configuration_path/products.json)
  echo "  Creating $product_name"
  jq ".[$i]" $configuration_path/products.json | \
  curl --silent --include --url $url \
    --header "Content-Type: application/json" \
    --header "Authorization: Bearer $auth_token" \
    --data @- > $temp_file
  handle_error "Call to $url returned an error for $product_name:"
done
unset count

# Sync chosen prefixes
echo "Synchronizing prefixes"
name='seed-job'
while read prefix; do
  echo "  Synchronizing prefix: $prefix"
  # Construct new sync-job payload
  payload=$(
    jq -n '$ARGS.named' \
      --arg name $name \
      --arg prefix $prefix \
      --arg failFast false
  )

  # Create new sync-job
  echo "    Creating sync-job"
  url=$api_endpoint/api/admin/sync-jobs
  curl --silent --include --url $url \
    --request PUT \
    --header "Content-Type: application/json" \
    --header "Authorization: Bearer $auth_token" \
    --data "$payload" > $temp_file
  handle_error "Call to PUT $url returned an error for prefix $prefix:"
  unset payload

  # Run job
  echo "    Running sync-job"
  url=$api_endpoint/api/admin/sync-jobs/$name/run
  curl --silent --include --url $url \
    --request POST \
    --header "Content-Type: application/json" \
    --header "Authorization: Bearer $auth_token" > $temp_file
  handle_error "Call to POST $url returned an error for prefix $prefix:"

  # Wait for the job to finish
  echo "    Waiting for sync-job"
  state="RUNNING"
  while [ "$state" = "RUNNING" ]; do
    sleep 1
    state=$(curl --silent --url $api_endpoint"/api/admin/sync-jobs/"$name \
      --header "Content-Type: application/json" \
      --header "Authorization: Bearer $auth_token" | jq -r '.state')
  done
  unset state

  # Print finished job info
  curl --silent --url $api_endpoint"/api/admin/sync-jobs/"$name \
    --header "Authorization: Bearer $auth_token" | jq

  # Delete job
  echo "    Deleting sync-job"
  url=$api_endpoint/api/admin/sync-jobs/$name
  curl --silent --include --url $url \
    --request DELETE \
    --header "Authorization: Bearer $auth_token" > $temp_file
  handle_error "Call to DELETE $url returned an error:"
done < $configuration_path/prefixes.list
unset name

if [ -z "$SKIP_SEED_GEOSERVER" ]; then
  echo "Resetting GeoServer workspace"
  url=$api_endpoint/api/admin/geoserver/reset-workspace
  curl --silent --include --url $url \
    --request POST \
    --header "Authorization: Bearer $auth_token" > $temp_file
  handle_error "Call to POST $url returned an error:"
fi

echo "Seeding overlays"
params=$(test -z "$SKIP_SEED_GEOSERVER" && echo "" || echo "?syncGeoserver=false")
url=$api_endpoint/api/admin/geoserver/seed-overlays$params
unset params
curl --silent --include --url $url \
  --request POST \
  --header "Authorization: Bearer $auth_token" > $temp_file
handle_error "Call to POST $url returned an error:"

if [ -z "$SKIP_SEED_GEOSERVER" ]; then
  echo "Creating layers, one per product"
  count=$(jq 'length' < $configuration_path/products.json)
  for (( i=0; i<count; i++)); do
    layer_name=$(jq -r ".[$i].layerName" $configuration_path/products.json)
    product_name=$(jq -r ".[$i].name" $configuration_path/products.json)
    echo "  Creating layer $layer_name"
    url=$api_endpoint/api/admin/geoserver/product-layers/$layer_name
    curl --silent --include --url $url \
      --request POST \
      --header "Authorization: Bearer $auth_token" > $temp_file
    handle_error "Call to POST $url returned an error for $product_name:"
  done
  unset count
fi
