#!/bin/bash
set -e

# note that the QueGG system has to be started with environment variable QUEGG_ALLOW_UPLOADS set to "true"
curl \
  -X "POST" \
  -F "file=@nounppframearco.csv" \
  -F "config=@arco.json" \
  -F "targetType=nouns" \
  -F "lang=it" \
  "http://localhost:8089/quegg/import"
