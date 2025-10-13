FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY .mvn/ .mvn
COPY mvnw .
COPY pom.xml .
COPY src ./src
COPY data ./data

RUN ./mvnw -B package -DskipTests

# jarファイル名は適宜修正
CMD ["java", "-jar", "target/demo-0.0.1-SNAPSHOT.jar"]