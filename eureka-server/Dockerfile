FROM circleci/jdk8:0.1.1
VOLUME /tmp
ADD target/eureka-server-1.0-SNAPSHOT.jar eureka-server.jar
RUN sh -c 'touch /eureka-server.jar'
ENV JAVA_OPTS=""
ENTRYPOINT ["java", "-Djava.security.egd=file:/./urandom", "/eureka-server.jar"]