
# A Dockerfile is used to describe the image of the application so we can reliably recreate it anywhere we want

# First thing we need to do is to provide a BASE IMAGE to start FROM
FROM amazoncorretto:11

# We'll need to COPY our packaged JAR file from here to our containers storage
COPY target/app.jar app.jar

# Next, we need to EXPOSE a port for HTTP traffic. The container will sit here.
EXPOSE 8085

# The final thing we need to do is provide the CMD to start the application itself
CMD ["java", "-jar", "app.jar"]