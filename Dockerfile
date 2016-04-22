FROM ubuntu:14.04

MAINTAINER franklinsagini@gmail.com

RUN   apt-get update && \
      apt-get install -y --no-install-recommends  wget  unzip   openjdk-6-jre  openjdk-6-jdk  && \

WORKDIR  ofbizApp 

RUN   ./ant  && \
      ./ant load-demo  && \
      ./ant load-extseed   

EXPOSE 8080

CMD  ./ant load-demo start 