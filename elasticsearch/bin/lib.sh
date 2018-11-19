function create_remote_repo() {
  curl -XPUT \
    -H 'Content-Type: application/json' \
    localhost:9200/_snapshot/global \
    -d '{
      "type": "url",
      "settings": {
        "url": "http://employability.rouly.net/snapshots/"
      }
    }'
}

function create_local_repo() {
  curl -XPUT \
    -H 'Content-Type: application/json' \
    localhost:9200/_snapshot/global \
    -d '{
      "type": "fs",
      "settings": {
        "location": "/usr/share/elasticsearch/snapshots/global"
      }
    }'
}

function restore_snapshot() {
  SNAPSHOT_ID=$1
  if [[ -z $SNAPSHOT_ID ]] ; then echo "Missing Snapshot ID paramter." ; exit 1 ; fi
  curl -XPOST "localhost:9200/_snapshot/global/$SNAPSHOT_ID/_restore"
}

function create_snapshot() {
  SNAPSHOT_ID=$1
  if [[ -z $SNAPSHOT_ID ]] ; then echo "Missing Snapshot ID paramter." ; exit 1 ; fi
  curl -XPUT "localhost:9200/_snapshot/global/$SNAPSHOT_ID?wait_for_completion=true"
}
