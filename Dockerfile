FROM openjdk:17-oracle

WORKDIR /home/spring

COPY build/libs/*.jar /home/spring/app.jar

CMD ["java",
     "-Dspring.profiles.active=prod",
     "-Xlog:gc*:file=/home/spring/log/gc.log:time,uptimemillis,tags,level:filecount=7,filesize=20m",
     "-jar","/home/spring/app.jar"]
