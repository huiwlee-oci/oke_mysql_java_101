#!/bin/sh
SCRIPT_DIR=$(cd -- "$(dirname -- "$0")" >/dev/null 2>&1 && pwd)
. "$SCRIPT_DIR/../../bin/env.sh"

DOCKER_PLATFORM="${DOCKER_PLATFORM:-linux/amd64}"

"$SCRIPT_DIR/../mvnw" clean package
docker buildx build --platform "$DOCKER_PLATFORM" --load -t ai-demo:v2 "$SCRIPT_DIR/.."
docker images | grep ai-demo
