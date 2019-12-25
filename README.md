# Metrics SDK
> A Java SDK where related to the project [metrics](https://github.com/mohistzh/metrics)

## Overview

The sdk was designed for collecting application data which enable  developers easy to integrate metrics functionality with their Java project. Here I provided two ways to upload application metrics data to kafka server. JVM metrics, and SLA percentage, for example, it will automatically upload to specified kafka, and if here is a requirement about collecting product data for some purpose, you could use custom metrics reporter as well.


## Refereces

To checkout application-sample.properties under the `resources` folder first, noted the project was built with Spring Framework and relying on Kafka.

**About properties**

Attribute  | Parameter | Description
------------- | ------------- | -------------
metrics.enabled  | true / false | turn on / off infra metrics collectors
metrics.server  | {kafka_host}:{kafka_port} | kafka server and port
metrics.topic  | {kafka_topic} | kafka topic name
custom.metrics.enabled  | true / false | turn on / off custom metrics collectors
custom.metrics.bu  | {business_unit} | An alias to separate multiple collectors
custom.metrics.server  | {kafka_host}:{kafka_port} | kafka server and port of custom metrics instance
custom.metrics.topic  | {custom-kafka_topic} | kafka topic name of custom metrics instance
