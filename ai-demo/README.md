# OCI GenAI Chat on OKE

Spring Boot chatbot UI that calls OCI Generative AI Inference and is ready to run on Oracle Kubernetes Engine.

## Configure

Edit `ai-demo-cfg.yaml`:

- `OCI_GENAI_REGION`: OCI region for Generative AI, for example `us-chicago-1`
- `OCI_GENAI_COMPARTMENT_ID`: compartment OCID used for inference
- `OCI_GENAI_MODEL_ID`: on-demand chat model ID available in your tenancy and region
- `OCI_GENAI_ENDPOINT_ID`: optional dedicated endpoint OCID; if set, it is used instead of `OCI_GENAI_MODEL_ID`
- `OCI_GENAI_CHAT_FORMAT`: `cohere` for Cohere chat models, or `generic` for OCI generic chat-compatible models

The OKE manifest uses workload identity:

```text
Allow any-user to use generative-ai-chat in compartment <compartment-name> where all {
  request.principal.type = 'workload',
  request.principal.namespace = 'default',
  request.principal.service_account = 'ai-demo',
  request.principal.cluster_id = '<cluster-ocid>'
}
```

OKE workload identity requires an enhanced cluster. For local development, use `OCI_GENAI_AUTH_MODE=config_file` with your OCI SDK config file.

## Build And Run Locally

```sh
cd ai-demo
./mvnw spring-boot:run \
  -Dspring-boot.run.arguments="--oci.genai.region=us-chicago-1 --oci.genai.compartment-id=<compartment-ocid> --oci.genai.model-id=<model-id>"
```

Open `http://localhost:8080`.

## Build, Push, Deploy

From the repo root, make sure `bin/env.sh` exists from `bin/env.sh.example` and points to your OCIR region/namespace.

```sh
cd ai-demo
./bin/build.sh
./bin/push.sh
./bin/config.sh
./bin/deploy.sh
kubectl get svc ai-demo-service
kubectl rollout restart deployment/ai-demo-deployment
kubectl rollout status deployment/ai-demo-deployment
```

`./bin/build.sh` builds `linux/amd64` by default, which matches most OKE worker nodes. If your OKE node pool uses Arm shapes, run `DOCKER_PLATFORM=linux/arm64 ./bin/build.sh` instead.

Update the `image:` in `ai-demo.yaml` to match the OCIR path printed by `./bin/push.sh`.

```sh
kubectl port-forward service/ai-demo-service 8080:80
```

```sh
http://localhost:8080
```

## References

- [OCI SDK for Java](https://docs.oracle.com/en-us/iaas/Content/API/SDKDocs/javasdk.htm)
- [OCI Generative AI Inference Java chat example](https://docs.oracle.com/en-us/iaas/tools/java-sdk-examples/latest/generativeaiinference/ChatExample.java.html)
- [Granting workloads access to OCI resources from OKE](https://docs.oracle.com/en-us/iaas/Content/ContEng/Tasks/contenggrantingworkloadaccesstoresources.htm)
- [OCI Generative AI IAM permissions](https://docs.oracle.com/en-us/iaas/Content/generative-ai/model-permissions.htm)
