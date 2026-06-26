FROM --platform=linux/amd64  eclipse-temurin:17-jre-jammy

# 라즈베리파이 server 배포 시 arm 적용
#FROM --platform=linux/arm64 eclipse-temurin:17-jre-jammy

RUN mkdir /app/data -p
RUN mkdir /app/etc -p

ENV MEMORY="1024M"
ENV CPUS=2
ARG JAR_FILE
ADD target/${JAR_FILE} /myapp/app.jar
VOLUME ["/app/log", "/app/etc" ]
CMD java -Xmx$MEMORY -XX:+HeapDumpOnOutOfMemoryError -XX:OnOutOfMemoryError="kill -9 %p" -XX:CICompilerCount="$(($CPUS>2?$CPUS:2))" -XX:+UseSerialGC -jar /myapp/app.jar
