
alias d='docker'
alias docker-compose='docker compose'
alias dn='docker network'
alias kt='kafka-topics --bootstrap-server localhost:19092'

if ! [ -x "$(command -v docker)" ]; then
    echo "docker is not installed." >&2
    exit 1
fi

docker info > /dev/null 2>&1
if [ $? -ne 0 ]; then
  echo "docker server is not running." >&2
  exit
fi

#
# creates a network unique to this project that can be shared between docker compose instances
# kafka-streams-201 -> ks201
#
NETWORK=$(docker network inspect -f '{{.Name}}' ks201 2>/dev/null)
if [ "$NETWORK" != "ks201" ]; then
  (docker network create ks201 >/dev/null)
fi

(cd cluster_zk; docker-compose up -d --wait)
#(cd cluster; docker-compose up -d --wait)

./gradlew build

kt --create --replication-factor 3 --partitions 4 --topic bag-check --if-not-exists
kt --create --replication-factor 3 --partitions 4 --topic bag-check-expedite --if-not-exists
kt --create --replication-factor 3 --partitions 4 --topic output-bag-check --if-not-exists


(cd applications; docker-compose up -d producer)
#(cd applications; docker-compose up -d)
(cd monitoring; docker-compose up -d)

