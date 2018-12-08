# create_snapshot <repo> <snapshot>
function create_snapshot() {
  REPO=$1
  SNAPSHOT=$2
  if [[ -z $REPO ]] ; then echo "Missing Repo paramter." ; exit 1 ; fi
  if [[ -z $SNAPSHOT ]] ; then echo "Missing Snapshot paramter." ; exit 1 ; fi
  curl -XPUT "localhost:9200/_snapshot/$REPO/$SNAPSHOT?wait_for_completion=true"
  echo ""
}

# restore_snapshot <repo> <snapshot>
function restore_snapshot() {
  REPO=$1
  SNAPSHOT=$2
  if [[ -z $REPO ]] ; then echo "Missing Repo paramter." ; exit 1 ; fi
  if [[ -z $SNAPSHOT ]] ; then echo "Missing Snapshot paramter." ; exit 1 ; fi
  curl -XPOST "localhost:9200/_snapshot/$REPO/$SNAPSHOT/_restore?wait_for_completion=true"
  echo ""
}

# create_remote_repo
function create_remote_repo() {
  curl -XPUT \
    -H 'Content-Type: application/json' \
    localhost:9200/_snapshot/remote \
    -d '{
      "type": "url",
      "settings": {
        "url": "http://net.rouly.employability.s3-website-us-east-1.amazonaws.com/"
      }
    }'
  echo ""
}

# create_local_repo
function create_local_repo() {
  curl -XPUT \
    -H 'Content-Type: application/json' \
    localhost:9200/_snapshot/local \
    -d '{
      "type": "fs",
      "settings": {
        "location": "/usr/share/elasticsearch/snapshots/local"
      }
    }'
  echo ""
}
