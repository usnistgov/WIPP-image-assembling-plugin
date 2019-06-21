FROM openjdk:8-jdk-alpine
LABEL maintainer="National Institute of Standards and Technology"

ENV DEBIAN_FRONTEND noninteractive
ARG EXEC_DIR="/opt/executables"
ARG DATA_DIR="/data"

# Create folders
RUN mkdir -p ${EXEC_DIR} \
    && mkdir -p ${DATA_DIR}/inputs \
    && mkdir ${DATA_DIR}/outputs

# Copy wipp-image-assembling-plugin JAR
COPY target/wipp-image-assembling-plugin*.jar ${EXEC_DIR}/wipp-image-assembling-plugin.jar

# Set working directory
WORKDIR ${EXEC_DIR}

# Default command. Additional arguments are provided through the command line
ENTRYPOINT ["/usr/bin/java", "-jar", "wipp-image-assembling-plugin.jar"]