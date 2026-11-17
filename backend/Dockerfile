# ---------- build stage ----------
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build

# 先只拷贝 pom，单独下载依赖，利用 Docker 层缓存加速重复构建
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---------- runtime stage ----------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 注意：这里用 root 运行，是为了让容器内进程能写入挂载进来的 ./uploads 目录。
# 若要保持非 root 运行，需要先 chown 宿主机目录给 uid 1000，或用 entrypoint 修正属主。
RUN mkdir -p /app/uploads

COPY --from=builder /build/target/app.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
