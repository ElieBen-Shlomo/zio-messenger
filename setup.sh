#!/usr/bin/env bash

readonly DATABASE_NAME="chatbox"
readonly CONTAINER_NAME="postgres-db"

function log {
    echo "[$(date)]: $*"
}

function waitForClusterConnection() {
    container_name=$1

    log "Waiting for db connection..."
    retryCount=0
    maxRetry=20
    docker exec $container_name /bin/sh -c 'PASSWORD=password psql -U postgres -c "SELECT datname FROM pg_database;"' &>/dev/null
    while [ $? -ne 0 ] && [ "$retryCount" -ne "$maxRetry" ]; do
        log 'db not reachable yet. sleep and retry. retryCount =' $retryCount
        sleep 5
        ((retryCount+=1))
        docker exec $container_name /bin/sh -c 'PASSWORD=password psql -U postgres -c "SELECT datname FROM pg_database;"' &>/dev/null
    done

    if [ $? -ne 0 ]; then
      log "Not connected after " $retryCount " retry. Abort the migration."
      exit 1
    fi

    docker exec $container_name /bin/sh -c "PASSWORD=password psql -U postgres -c \"CREATE DATABASE $DATABASE_NAME;\""
    log "Connected to db"
}

function executeScripts() {
    local container_name=$1
    local file_pattern=$2
    for sql_file in $file_pattern; do
        file_name=$(basename $sql_file)
        log "Executing sql from $file_name"
        docker cp $sql_file $container_name:/var/tmp
        docker exec $container_name /bin/sh -c "PASSWORD=password psql -U postgres -d $DATABASE_NAME -a -f /var/tmp/$file_name"
    done
}

docker run -p 5432:5432 --name postgres-db -e POSTGRES_PASSWORD=password -d postgres
waitForClusterConnection $CONTAINER_NAME
executeScripts $CONTAINER_NAME "database/evolutions/*.sql"

sbt run
