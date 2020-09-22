#!/bin/bash

# TODO: Check if it's main catalog of sat4envi then pass

countdown()
(
  IFS=:
  set -- $*
  secs=$(( ${1#0} * 3600 + ${2#0} * 60 + ${3#0} ))
  while [ $secs -gt 0 ]
  do
    sleep 1 &
    printf "\r%02d:%02d:%02d" $((secs/3600)) $(( (secs/60)%60)) $((secs%60))
    secs=$(( $secs - 1 ))
    wait
  done
  echo
)

RUN_BACKEND=0
for option in "$@"
do
    	case $option in
		-b)
			RUN_BACKEND=1
		;;
    	esac
done

sudo docker-compose down

services_to_kill=$(sudo docker ps -a -q | wc -l)
if [ "$services_to_kill" -gt 0 ]; then
	sudo su - root -c 'docker rm -f $(docker ps -a -q)'
fi
	
volumes_to_remove=$(sudo docker volume ls -q | wc -l)
if [ "$volumes_to_remove" -gt 0 ]; then
	sudo su - root -c 'docker volume rm $(docker volume ls -q)'
fi

# Uncomment if error with seed.sh will be fixed
sudo docker-compose up -d broker db geoserver gs-gateway minio # s4e-backend
sudo docker-compose -f docker-compose-test.yml up -d db-test

# echo 'WAITING FOR BACKEND CONTAINER...'
# countdown "00:01:00"

# s4e-backend/src/scripts/seed.sh http://localhost:4201 s4e-backend/seed-configs/seed-1

# if [ "$RUN_BACKEND" -eq 0 ]; then
#	 sudo su - root -c 'docker rm -f $(docker ps -a -f "name=s4e-backend" -q)'
# fi



