# 실행 전 체크리스트

## 1. Render 준비
- Render 계정을 만들고 Web Service를 생성한다.
- 루트의 `render.yaml` 또는 Render 대시보드 설정을 기준으로 `backend/`를 서비스 루트로 잡는다.
- `backend/.env.example`를 참고해 Render 환경변수를 입력한다.

## 2. 백엔드 배포 확인
- `backend/`에서 `npm install`을 실행한다.
- `npm run build`가 통과하는지 확인한다.
- Render 배포가 성공하고 `/health`가 응답하는지 확인한다.
- 배포 후 `summary`, `processAudio`, `sendEmail` URL을 확인한다.

## 3. 앱 연결
- `gradle.properties`의 `MEETING_MEMO_API_BASE_URL`을 Render URL 베이스로 변경한다.
- Android Studio에서 Gradle Sync가 성공하는지 확인한다.
- 실기기 또는 에뮬레이터에서 앱이 설치되는지 확인한다.

## 4. 스모크 테스트
- 음성 입력 권한 허용 후 텍스트가 원문 필드에 반영된다.
- 20자 미만 텍스트에서 요약 생성 시 검증 메시지가 나온다.
- 충분한 길이의 텍스트로 요약 생성이 성공한다.
- 음성 파일 가져오기 화면에서 `m4a`, `mp3`, `wav` 파일을 선택할 수 있다.
- 25MB를 초과하는 오디오 파일은 업로드 전에 차단되거나 오류 메시지가 표시된다.
- 음성 파일 업로드 후 전사와 요약이 성공한다.
- 가져온 결과를 메모 작성 화면으로 넘길 수 있다.
- 저장 후 홈 목록과 상세 화면에서 메모가 보인다.
- 잘못된 이메일 주소 입력 시 발송이 차단된다.
- 올바른 이메일 주소 입력 시 발송 성공 메시지가 표시된다.

## 5. 문제 발생 시 우선 점검
- `MEETING_MEMO_API_BASE_URL`이 실제 Render 주소와 맞는지 확인한다.
- Render 환경변수에 키가 누락되지 않았는지 확인한다.
- STT 모델과 API 키가 실제 공급자 설정과 맞는지 확인한다.
- Resend 발신 주소가 서비스 설정과 일치하는지 확인한다.
- 네트워크 보안이나 CORS 오류가 없는지 확인한다.
