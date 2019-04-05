FROM alpine:3.6

COPY fetch_molgenis_config /etc/crontabs/root

CMD ["crond", "-f", "-d", "8"]


