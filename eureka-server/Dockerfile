FROM circleci/jdk8:0.1.1
VOLUME /tmp
COPY target/eureka-server-1.0-SNAPSHOT.jar /eureka-server.jar
EXPOSE 8761
ENV JAVA_OPTS=""
ENTRYPOINT ["java", "-jar", "/eureka-server.jar"]
