 # Start with Amazon Corretto 17 Alpine base image
 FROM amazoncorretto:17-alpine

 # Install curl on Alpine
 RUN apk update && apk add --no-cache curl

 # Copy the jar file into the image
 COPY target/app.jar /app.jar

# Run the Populator before starting the main application
# RUN java -cp app.jar app.utils.Populator

 # Expose the port your app runs on
 EXPOSE 7070

 # Command to run your app
 CMD ["java", "-jar", "/app.jar"]

