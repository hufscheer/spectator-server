FROM eclipse-temurin:17-jre-alpine

RUN apk add --no-cache curl

WORKDIR /home/spring

COPY build/libs/*.jar /home/spring/app.jar

ENV SPRING_PROFILE=prod

STOPSIGNAL SIGTERM

CMD ["sh", "-c", "exec java -Dspring.profiles.active=${SPRING_PROFILE} -Xmx512m -Xms256m -jar /home/spring/app.jar"]
