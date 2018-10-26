# employability

A study of postsecondary graduate employability using topic modeling.

### project overview

#### core

Core components, models, and glue code.

```scala
libraryDependencies += "net.rouly" % "employability-core" % "x.x.x"
```

#### elasticsearch

Elasticsearch read/write services.
Interaction is defined using [Reactive Streams](http://www.reactive-streams.org/).

```scala
libraryDependencies += "net.rouly" % "employability-elasticsearch" % "x.x.x"
```

#### postgres

Postgres read/write services.
Interaction is defined using [Reactive Streams](http://www.reactive-streams.org/).

```scala
libraryDependencies += "net.rouly" % "employability-postgres" % "x.x.x"
```

#### ingest

Entry point application to ingest raw data into Elasticsearch.

Raw data is accepted from the following data providers:
* [data.world](https://data.world): add data set definitions under `resources/datasets/data.world/`

```scala
libraryDependencies += "net.rouly" % "employability-ingest" % "x.x.x"
```

#### analysis

Entry point application to read raw data from Elasticsearch and execute the primary topic modeling steps.
Data is pre-processed in this step as well.
Topics are output to Postgres.

```scala
libraryDependencies += "net.rouly" % "employability-analysis" % "x.x.x"
```

#### web

User facing web application to explore the generated topics and render various statistics about them.

```scala
libraryDependencies += "net.rouly" % "employability-web" % "x.x.x"
```
