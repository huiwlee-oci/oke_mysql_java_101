SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
. $SCRIPT_DIR/env.sh

kubectl create secret docker-registry ocir-secret --docker-server=$OCI_REGION --docker-username="$OCI_NAMESPACE/$OCI_USERNAME" --docker-password="$OCI_TOKEN" --docker-email="$OCI_EMAIL"
