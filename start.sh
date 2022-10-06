HTTP_PORT="8080"
CSCARDS_ENDPOINT="https://app.clearscore.com/api/global/backend-tech-test/v1/cards"
SCOREDCARDS_ENDPOINT="https://app.clearscore.com/api/global/backend-tech-test/v2/creditcards"

export HTTP_PORT
export CSCARDS_ENDPOINT
export SCOREDCARDS_ENDPOINT

sbt "run"

