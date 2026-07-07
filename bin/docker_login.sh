SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
. $SCRIPT_DIR/env.sh

# Login to docker
printf '%s\n' "$OCI_TOKEN" | docker login "$OCI_REGION" -u "$OCI_NAMESPACE/$OCI_USERNAME" --password-stdin
# OCI Repository prefix
export DOCKER_PREFIX=$OCI_REGION/$OCI_NAMESPACE/oke-registry
