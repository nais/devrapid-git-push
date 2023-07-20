FROM gcr.io/distroless/java17-debian11:nonroot

COPY build/libs/*.jar /app/

ENV LOG_FORMAT="logstash"

CMD ["/app/app.jar"]