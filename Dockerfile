FROM maven:3.8.3-openjdk-17

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY . .

CMD ["java", "-jar", "target/BLPS_Lab1-0.0.1-SNAPSHOT.jar"]