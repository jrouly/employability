# employability

A study of postsecondary graduate employability using topic modeling.

## Project Overview

The research goal of this project is to determine the nature of the overlap of those core, academic skills being taught at the postsecondary level and those being expected at the entry level in the workforce.
In a nutshell, the research goal is to study how well universities are preparing students for the workforce: to what degree are they promoting graduate employability?

This repository defines an open source software tool to perform that analysis.
The core concept leveraged is topic modeling, a method from machine learning and natural language processing.
Topic modeling is used to infer concepts from large datasets of job postings and course descriptions.

## Installation

Install the following dependencies:

* [`java`](https://www.java.com/en/)
* [`sbt`](https://www.scala-sbt.org/)
* [`docker`](https://www.docker.com/) and [`docker-compose`](https://docs.docker.com/compose/)

Using `docker-compose`, start the background services:

    docker-compose up -d

Using `sbt`, execute the data pipelines.

    sbt ingest/run      # ingest raw data
    sbt preprocess/run  # prepare the data for LDA
    sbt analysis/run    # perform topic modeling

Start up the visualization server to interact with the inferred topics.

    sbt web/run

## Data Sources

Data sources can be defined in `ingest/src/main/resources/datasets/`.
Currently, only `csv` data sourced from [`data.world`](https://data.world) are supported, but extending the ingestion pipeline to additional data sources is straightforward.

Register for an account at [`data.world`](https://data.world) to get an [API token](https://data.world/settings/advanced).
Export the API token as an environment variable prior to ingestion.

    export DATA_WORLD_API_TOKEN=...

### Future Work

This project has spawned an [additional effort](https://github.com/jrouly/data.world-scala) to built a native Scala client for [`data.world`](https://data.world) using [Reactive Stream](http://www.reactive-streams.org/) technology.

## Project Modules

### `core`

Core components, models, and glue code.

```scala
"net.rouly" % "employability-core" % "x.x.x"
```

### `elasticsearch`

Elasticsearch read/write services.
Interaction is defined using [Reactive Streams](http://www.reactive-streams.org/).

```scala
"net.rouly" % "employability-elasticsearch" % "x.x.x"
```

### `postgres`

Postgres read/write services.
Interaction is defined using [Reactive Streams](http://www.reactive-streams.org/).

```scala
"net.rouly" % "employability-postgres" % "x.x.x"
```

### `ingest`

Entry point application to ingest raw data into Elasticsearch.

Raw data is accepted from the following data providers:
* [data.world](https://data.world): add data set definitions under `resources/datasets/data.world/`

```scala
"net.rouly" % "employability-ingest" % "x.x.x"
```

### `analysis`

Entry point application to read raw data from Elasticsearch and execute the primary topic modeling steps.
Data is pre-processed in this step as well.
Topics are output to Postgres.

```scala
"net.rouly" % "employability-analysis" % "x.x.x"
```

### `web`

User facing web application to explore the generated topics and render various statistics about them.

```scala
"net.rouly" % "employability-web" % "x.x.x"
```
