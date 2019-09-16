#!/bin/sh

echo "Send requests to $1 every $2 seconds"

while [ true ]
do
	quantity=$((1 + RANDOM % 20))
	style=$((1 + RANDOM % 20))
	curl_cmd="curl -X POST http://$1:50050/shop/order -H 'Content-Type: application/json' -d '{\"styleName\": \"beachops$style\", \"quantity\": '$quantity'}'"
	echo $curl_cmd
	eval $curl_cmd
	sleep $2
	printf "\n"
done
