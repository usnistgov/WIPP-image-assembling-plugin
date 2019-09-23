#!/bin/bash
version=$(<VERSION)
docker build . -t wipp/wipp-image-assembling-plugin:${version}