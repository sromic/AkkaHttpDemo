#!/usr/bin/env sh

tag=latest

: ${HOST:=$(ipconfig getifaddr en0)}
: ${HOST:=$(ipconfig getifaddr en1)}
: ${HOST:=$(ipconfig getifaddr en2)}
: ${HOST:=$(ipconfig getifaddr en3)}
: ${HOST:=$(ipconfig getifaddr en4)}

docker run \
  --detach \
  --name akkaHttpDemo \
  --publish 8080:8080 \
  sromic/akka-http-docker:${tag} \
  -Dcassandra-journal.contact-points.0=${HOST}:9042 \
  -Dconstructr.coordination.host=${HOST}