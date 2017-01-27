FROM bitsdock/spark-sbt:1.6.2a

ADD . /app

RUN cd /app && \
    sbt assembly

