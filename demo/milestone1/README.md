# milestone 1

Screen recording: [milestone-1.gif](https://michel.rouly.net/public/employability/milestone-1.gif)

The video above demonstrates execution of the ingestion pipeline (downloading thousands of documents from the Internet) and the analysis pipeline (preprocessing, cleaning, and topic modeling a subset of the documents).
The commands issued are:

    sbt ingest/run
    sbt analysis/run

The Elasticsearch index monitoring software is Kibana, which is included in the `docker-compose` bundle at the root of the repository.

## Tables from the end of the video

### Topics

The topic table shows a selection of 5 topics, where each topic is a distribution of words (represented as `termIndices` in a vocabulary array) with a weight for each word (`termWeights`).

    +-----+--------------------+--------------------+
    |topic|         termIndices|         termWeights|
    +-----+--------------------+--------------------+
    |    0|[19, 2, 16, 41, 7...|[0.00333076645010...|
    |    1|[53, 31, 28, 93, ...|[0.00488431466038...|
    |    2|[2657, 2262, 688,...|[0.00156617310875...|
    |    3|[354, 21, 63, 93,...|[0.00852675371490...|
    |    4|[1700, 3746, 3711...|[0.00313528666399...|
    +-----+--------------------+--------------------+
    only showing top 5 rows

### Documents

Documents are modeled as a mixture of topics.
Each document has a `topicDistribution` over all the topics with certain weights for each topic.

    +--------------------+--------------------+--------------------+--------------------+--------------------+--------------------+--------------------+--------------------+
    |                  id|                 raw|             content|              tokens|            filtered|         rawFeatures|            features|   topicDistribution|
    +--------------------+--------------------+--------------------+--------------------+--------------------+--------------------+--------------------+--------------------+
    |af507419-b383-324...| Apply now The Ro...|appli|role|london...|[appli, role, lon...|[appli, role, lon...|(45955,[0,3,4,7,8...|(45955,[0,3,4,7,8...|[0.87759014716285...|
    |418d42b9-0c4d-327...| Apply now Financ...|appli|financi|con...|[appli, financi, ...|[appli, financi, ...|(45955,[0,2,3,4,6...|(45955,[0,2,3,4,6...|[0.77975380642597...|
    |9ad692c6-3a76-300...| Apply now Senior...|appli|senior|pens...|[appli, senior, p...|[appli, senior, p...|(45955,[0,1,2,3,4...|(45955,[0,1,2,3,4...|[0.80705425457022...|
    |534d3cb7-5c60-3f0...|Robert Half Techn...|robert|half|techn...|[robert, half, te...|[robert, half, te...|(45955,[1,6,7,9,1...|(45955,[1,6,7,9,1...|[0.42586453134305...|
    |e2e69587-b8b3-3f1...|Amazing Opportuni...|amaz|opportun|cus...|[amaz, opportun, ...|[amaz, opportun, ...|(45955,[1,4,6,9,1...|(45955,[1,4,6,9,1...|[0.98997975368628...|
    +--------------------+--------------------+--------------------+--------------------+--------------------+--------------------+--------------------+--------------------+
    only showing top 5 rows

### Interpretation

These tables will be much easier to parse when the vocabulary is added back in for reference.
