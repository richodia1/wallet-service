# Use an official OpenJDK image
FROM openjdk:17-jdk-alpine

# Set the working directory
WORKDIR /app

# Copy the built JAR file (adjust the filename if needed)
COPY target/service.prototype-0.0.1-SNAPSHOT.jar app.jar

# Copy the Elastic APM agent
COPY elastic-apm-agent.jar /elastic-apm-agent.jar

# Expose the Spring Boot port
EXPOSE 8098

# Set APM environment variables and start the app
ENV JAVA_OPTS="-javaagent:/elastic-apm-agent.jar \
 -Delastic.apm.server_urls=http://apm-server:8200 \
 -Delastic.apm.service_name=wallet-service \
 -Delastic.apm.environment=dev \
 -Delastic.apm.application_packages=com.wallet"

CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
