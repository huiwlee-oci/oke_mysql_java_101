#!/bin/sh
SCRIPT_DIR=$(cd -- "$(dirname -- "$0")" >/dev/null 2>&1 && pwd)
. "$SCRIPT_DIR/../../bin/docker_login.sh"

IMAGE_REPO="${AI_DEMO_IMAGE_REPO:-$DOCKER_PREFIX/ai-demo}"

docker tag ai-demo:v1 "$IMAGE_REPO:v1"
docker push "$IMAGE_REPO:v1"
