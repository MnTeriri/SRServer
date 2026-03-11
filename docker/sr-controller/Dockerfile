FROM eclipse-temurin:25-jre

WORKDIR /app

# 复制 jar
COPY ./sr-controller-1.0.0.jar app.jar
# 复制当前目录配置
COPY ./application.properties application.properties

RUN mkdir -p /app/data/image/inputs /app/data/image/outputs

#设置 volume 路径
VOLUME ["/app/data/image"]

# 默认端口
ENV SERVER_PORT=10000
EXPOSE 10000

ENTRYPOINT ["java","-jar","/app/app.jar","--spring.config.location=/app/application.properties"]