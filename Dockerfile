FROM ubuntu:14.04

MAINTAINER franklinsagini@gmail.com

COPY sources.list.trusty  /etc/apt/sources.list 

RUN   apt-get update && \
      apt-get install -y --no-install-recommends  wget  unzip   openjdk-6-jre  openjdk-6-jdk  && \

WORKDIR  chaisacco 

RUN   ./ant  && \
      ./ant load-demo  && \
      ./ant load-extseed   

EXPOSE 8080

CMD  ./ant load-demo start 