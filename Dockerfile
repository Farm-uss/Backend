# 1. 자바 17 환경 사용
FROM openjdk:17-jdk-slim

# 2. 빌드된 jar 파일의 위치를 변수로 지정
ARG JAR_FILE=build/libs/*.jar

# 3. jar 파일을 컨테이너 내부의 app.jar라는 이름으로 복사
COPY ${JAR_FILE} app.jar

# 4. 서버가 떴을 때 실행할 명령어
ENTRYPOINT ["java", "-jar", "/app.jar"]