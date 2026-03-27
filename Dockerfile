FROM eclipse-temurin:17-jre-alpine

RUN apk add --no-cache curl

WORKDIR /home/spring

COPY build/libs/*.jar /home/spring/app.jar

ENV SPRING_PROFILE=prod

CMD ["sh", "-c", "exec java -Dspring.profiles.active=${SPRING_PROFILE} -Xmx512m -Xms256m -Xlog:gc*:file=/home/spring/log/gc.log:time,uptimemillis,tags,level:filecount=7,filesize=20m -jar /home/spring/app.jar"]
