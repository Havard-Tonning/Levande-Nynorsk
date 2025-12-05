FROM eclipse-temurin:20-jdk-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN apk add --no-cache maven
RUN mvn clean package -DskipTests

FROM eclipse-temurin:20-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
COPY src/main/java/preprocessing/translation.csv translation.csv
COPY src/main/java/preprocessing/dictionary.csv dictionary.csv
COPY src/main/java/preprocessing/names.txt names.txt
COPY src/main/java/preprocessing/NNDictionary.csv NNDictionary.csv
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]