FROM eclipse-temurin:25-jre

WORKDIR /app

# 复制 jar
COPY ./sr-inference-1.0.0.jar app.jar
# 复制当前目录配置
COPY ./application.properties application.properties

RUN mkdir -p /app/data/model

COPY ./model /app/data/model

#设置 volume 路径
VOLUME ["/app/data/model"]

# 默认端口
ENV SERVER_PORT=11000
EXPOSE 11000

ENTRYPOINT ["java","-jar","/app/app.jar","--spring.config.location=/app/application.properties"]