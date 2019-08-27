FROM alpine:3.8

RUN apk add --no-cache curl

COPY loadgen.sh /loadgen.sh

ENTRYPOINT /loadgen.sh shopping.beachshirts.svc.cluster.local 10
