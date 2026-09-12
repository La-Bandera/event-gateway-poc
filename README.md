# event-gateway-poc

A small, real, deployed proof of concept built for [Red Hat Summit: Connect NYC](https://www.redhat.com/en/summit) (2026-09-17) by [La Bandera Consulting](https://labanderaconsulting.com).

## What this is

An event-driven integration gateway: a REST endpoint accepts an operational event, publishes it to Kafka, and an [Apache Camel](https://camel.apache.org/) route consumes it, transforms it, and republishes it to a downstream topic. It's the same pattern used to bridge legacy message-queue traffic onto a modern event backbone — the kind of integration work behind [UPS's CIPE platform](https://www.redhat.com/en/success-stories/ups), the **2018 Red Hat Innovators of the Year**, where La Bandera's founder built the adapters bridging legacy IBM WebSphere MQ into a Red Hat Fuse / ActiveMQ messaging backbone.

```
POST /ingest ──▶ Kafka (edge.events.raw) ──▶ Camel route (transform) ──▶ Kafka (edge.events.processed)
```

## Stack

- **Quarkus** 3.39 — Red Hat's cloud-native Java runtime.
- **Apache Camel** (via `camel-quarkus-kafka`) — the integration/routing layer.
- **AMQ Streams** (Red Hat's distribution of [Strimzi](https://strimzi.io/), running Kafka) — the event backbone, deployed on OpenShift.
- **OpenShift Pipelines** (Tekton) — builds and deploys this repo to the [Red Hat Developer Sandbox](https://developers.redhat.com/developer-sandbox).

Deployed in **JVM mode**, not GraalVM native — the Sandbox's free-tier resource budget (~4 vCPU / ~7 GB total) comfortably fits a single-broker KRaft Kafka cluster plus this application in JVM mode, so native compilation isn't needed and isn't worth its added build risk under a compressed timeline. See `docs/` (added by the deployment) for the resource math.

## Running locally

```bash
./mvnw quarkus:dev
```

This starts the application on `http://localhost:8080`. Note: the Kafka-consuming route and the event publisher are both gated behind `gateway.kafka-route.enabled`, which is `true` by default — running locally without a Kafka broker reachable at `localhost:9092` means `POST /ingest` will accept and validate requests but fail to actually publish. Point `KAFKA_BOOTSTRAP_SERVERS` at a real broker to exercise the full path.

## Testing

```bash
./mvnw test
```

The test suite runs without any external infrastructure: `gateway.kafka-route.enabled=false` in the `test` profile means the Kafka consumer route never starts and the publisher never tries to connect, so the transform logic and the REST layer are both fully unit-tested in isolation. **End-to-end Kafka delivery is verified against the live, deployed AMQ Streams cluster on OpenShift** — not reproduced here with a Testcontainers broker — because the point of this PoC is a real deployment, not a simulated one.

## API

| Endpoint | Method | Purpose |
|---|---|---|
| `/ingest` | `POST` | Accepts `{"id", "source", "type", "payload"}`; publishes to Kafka. Returns `202` on success, `400` if `id`/`source`/`type` are missing. |
| `/status` | `GET` | Human-facing status: service name, health, configured topic names. |
| `/q/health` | `GET` | Standard Kubernetes liveness/readiness probes (SmallRye Health). |

## Portability note

This deploys to Red Hat's free Developer Sandbox, which runs on AWS — that's simply where the free trial happens to be hosted, and is irrelevant to the architecture. What matters: this is built entirely on OpenShift primitives (standard Kubernetes manifests, AMQ Streams/Strimzi, OpenShift Pipelines), so it lifts and shifts with no rework to Google Kubernetes Engine or Red Hat OpenShift on Google Cloud Dedicated when it's time to run this for a client.

## Status

**This is a proof of concept, not a production system.** It demonstrates a real, working pattern under real constraints (a free-tier resource budget, a compressed build timeline) — it does not claim production-scale, enterprise-grade, or any customer outcome. See the [architecture review](https://labanderaconsulting.com) on La Bandera's site for an automated read on this repo's structure.
