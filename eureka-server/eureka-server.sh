#!/bin/bash
set -e

docker run -d  -p 8888:8761  -v /home/ubuntu/logs:/data/logs --name eureka-server eureka-server

