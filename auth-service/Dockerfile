FROM maven:3.9.9-eclipse-temurin-24 AS build
WORKDIR /workspace

# Copy root context to allow building sibling module 'grpc'
COPY . .

# Build and install grpc module into local repo so auth-service can resolve it
RUN mvn -f grpc/pom.xml -DskipTests install

# Build auth-service
WORKDIR /workspace/auth-service
RUN mvn dependency:go-offline -B
RUN mvn clean package -DskipTests

FROM eclipse-temurin:24-jre
WORKDIR /app

COPY --from=build /workspace/auth-service/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
