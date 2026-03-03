# 실행 확인 방법

1. **Mailtrap 설정**  
   [Mailtrap](https://mailtrap.io)에서 SMTP 인증 정보를 확인한 뒤, 환경 변수 `MAILTRAP_USERNAME`, `MAILTRAP_PASSWORD` 설정 또는 `application.yml`의 `spring.mail.username`/`password` 직접 입력.

2. **실행**  
   `./mvnw spring-boot:run` (또는 IDE에서 `MailingBackendApplication` 실행).

3. **구독 등록**  
   Postman 등으로 `POST http://localhost:8080/api/subscribe`  
   Body (JSON): `{"email":"user@example.com","categories":["IT","ECONOMY"]}` → 200 OK 확인.

4. **DB 확인**  
   H2 콘솔(필요 시 `spring.h2.console.enabled: true` 추가) 또는 애플리케이션 로그로 구독 저장 여부 확인.

5. **메일 발송 확인**  
   스케줄러는 기본 1분마다 실행. Mailtrap Inbox에서 수신 메일 확인.
