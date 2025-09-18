FROM openjdk:17-oracle

WORKDIR /home/spring

COPY build/libs/*.jar /home/spring/app.jar

CMD ["java", "-Dspring.profiles.active=prod", "-jar", "/home/spring/app.jar"]
