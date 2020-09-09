#!/bin/bash

# TODO: Check if it's main catalog of sat4envi then pass

sudo docker-compose down

services_to_kill=$(sudo docker ps -a -q | wc -l)
if [ "$services_to_kill" -gt 0 ]; then
	sudo su - root -c 'docker rm -f $(docker ps -a -q)'
fi
	
volumes_to_remove=$(sudo docker volume ls -q | wc -l)
if [ "$volumes_to_remove" -gt 0 ]; then
	sudo su - root -c 'docker volume rm $(docker volume ls -q)'
fi

# TODO: Get services from input or provide options to choose

sudo docker-compose up -d broker db geoserver gs-gateway minio
sudo docker-compose -f docker-compose-test.yml up -d db-test

bash ./s4e-backend/scripts/seed.sh
