# 1. 애플리케이션이 Java 21로 빌드되므로 런타임도 같은 메이저 버전을 사용
FROM eclipse-temurin:21-jre-jammy

# 2. 빌드된 jar 파일의 위치를 변수로 지정
ARG JAR_FILE=build/libs/*.jar

# 3. jar 파일을 컨테이너 내부의 app.jar라는 이름으로 복사
COPY ${JAR_FILE} app.jar

# 4. 서버가 떴을 때 실행할 명령어
ENTRYPOINT ["java", "-jar", "/app.jar"]
