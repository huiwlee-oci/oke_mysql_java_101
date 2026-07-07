#!/bin/sh
SCRIPT_DIR=$(cd -- "$(dirname -- "$0")" >/dev/null 2>&1 && pwd)

kubectl apply -f "$SCRIPT_DIR/../ai-demo.yaml"
