# Meeting Memo MVP

안드로이드에서 회의 메모를 음성으로 입력하고, 요약 생성 후 저장/조회 및 이메일 발송까지 처리하는 MVP 프로젝트입니다.

## 구성
- `app/`: Android 앱 (`Kotlin`, `Compose`, `MVVM`, `Room`, `Retrofit`, `Hilt`)
- `backend/`: Render 배포용 Node 백엔드 (`Express`, `TypeScript`)
- `functions/`: 기존 Firebase Functions 기반 백엔드 초안 및 로컬 참고 코드

## Android 앱 설정
1. Android Studio에서 `E:\AI\meeting-memo-mvp`를 엽니다.
2. `gradle.properties`의 `MEETING_MEMO_API_BASE_URL`을 배포된 Render 백엔드 URL로 바꿉니다.
3. Sync Gradle 후 앱을 실행합니다.

예시:
```properties
MEETING_MEMO_API_BASE_URL=https://your-render-service.onrender.com/
```

## Render 백엔드 설정
1. `backend/.env.example`을 참고해 로컬 또는 Render 환경변수를 준비합니다.
2. `backend/`에서 의존성을 설치합니다.
3. 로컬에서는 `npm run dev`, 배포는 Render Web Service로 진행합니다.
4. 상세한 배포 절차는 `docs/render-deploy.md`를 참고합니다.

필수 환경변수:
- `LLM_API_KEY`
- `LLM_MODEL`
- `LLM_API_BASE_URL`
- `STT_API_KEY`
- `STT_MODEL`
- `STT_API_BASE_URL`
- `RESEND_API_KEY`
- `RESEND_FROM_EMAIL`

## 권장 배포 흐름
1. Render Web Service 생성
2. `backend/.env.example` 기준으로 비밀값 설정
3. Render에 `backend/`를 배포
4. Render 도메인을 `MEETING_MEMO_API_BASE_URL`에 반영
5. 안드로이드 실기기에서 마이크 권한, 요약, 메일 발송 확인

## MVP 테스트 체크
- 마이크 권한 거부 후 안내 메시지 표시
- 음성 입력 결과가 비어 있을 때 예외 처리
- 요약 최소 길이 검증
- 저장된 음성 파일 가져오기와 전사/요약 처리
- 저장 후 목록/상세 반영
- 이메일 형식 검증
- 메일 발송 성공/실패 처리

## 추가 문서
- `docs/render-deploy.md`: Render 설치/배포 절차
- `functions/README.md`: Firebase Functions 기반 참고 구현
- `docs/run-checklist.md`: 앱/백엔드 연결 전 최종 점검 체크리스트
- `docs/api-smoke-test.md`: 배포 후 API 스모크 테스트 예시
- `docs/privacy-policy-draft.md`: 개인정보처리방침 초안
- `docs/release-checklist.md`: 릴리즈 및 Play Store 제출 체크리스트
