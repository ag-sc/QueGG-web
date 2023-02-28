#!/bin/bash
set -e

docker run -p "8089:8089" -e "QUEGG_ALLOW_UPLOADS=true" agsc/quegg-web:latest
