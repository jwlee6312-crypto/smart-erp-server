FROM andrius/asterisk:latest
# 데비안(Debian) 계열이므로 apt-get을 사용해야 함
RUN apt-get update &&     apt-get install -y unixodbc odbc-postgresql libmariadb-dev-compat libmariadb-dev &&     rm -rf /var/lib/apt/lists/*
EXPOSE 5038 5060/udp 5060/tcp 10000-10050/udp
CMD ["/usr/sbin/asterisk", "-f"]
