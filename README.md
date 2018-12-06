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

    docker-compose up -d elasticsearch

## Execution

### Running the vis server

You can import a static dataset to run the vis server locally yourself without ingesting and processing the raw data.
Start up elasticsearch and the vis server, then import the latest dataset.

    docker-compose up -d elasticsearch web
    ./elasticsearch/bin/import-data 500-topics

You should be able to acccess the vis server at [localhost:9000](localhost:9000) once the import completes.

### Data Ingestion

If you would like to run the whole ingestion and analysis process, there are a few more steps.

Register for an account at [Data World](https://data.world) and export your API token.

    export DATA_WORLD_API_TOKEN=

Start up all background data services.

    docker-compose up -d elasticsearch kibana postgres

Execute the data pipelines.

    sbt ingest/run
    sbt preprocess/run

Execute LDA.

    sbt analysis/run

#### Exporting data

In the same way that you can import an online snapshot of the Elasticsearch data, you can also export your own snapshot.

    ./elasticsearch/bin/export-data <new-snapshot-id>

This will do several things:

  * create a `local` snapshot repository in your Elasticsearch cluster
  * create a new snapshot in the `local` repository
  * compress your snapshot repository to a portable `./snapshots.tar.gz` file

##### Configuring LDA

You can modify the behavior of LDA through environment variables.
Some pre-defined configurations are made available for you.

    # Source one of these before running analysis/run.
    source ./analysis/config/small
    source ./analysis/config/medium
    source ./analysis/config/large

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

### `preprocess`

Entry point application to pre-process and clean ingested data.
Cleaned and prepared data is exported to Postgres.

```scala
"net.rouly" % "employability-ingest" % "x.x.x"
```

### `analysis`

Entry point application to read processed data from Postgres and execute the primary topic modeling steps.
Topics are output to Elasticsearch.

```scala
"net.rouly" % "employability-analysis" % "x.x.x"
```

### `web`

User facing web application to explore the generated topics and render various statistics about them.

```scala
"net.rouly" % "employability-web" % "x.x.x"
```
