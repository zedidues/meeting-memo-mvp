# Render 배포 가이드

## 1. Render 서비스 생성
- Render 대시보드에서 `New +` -> `Web Service`를 선택합니다.
- Git 저장소를 연결합니다.
- 서비스 루트는 `backend/`를 사용합니다.

## 2. 기본 설정
- Runtime: `Node`
- Build Command: `npm install && npm run build`
- Start Command: `npm run start`
- Health Check Path: `/health`

`render.yaml`을 사용할 경우 루트 저장소 기준으로 자동 설정할 수 있습니다.

## 3. 환경변수 설정
`backend/.env.example`를 기준으로 아래 값을 Render Environment에 넣습니다.

- `PORT`
- `LLM_API_KEY`
- `LLM_MODEL`
- `LLM_API_BASE_URL`
- `STT_API_KEY`
- `STT_MODEL`
- `STT_API_BASE_URL`
- `RESEND_API_KEY`
- `RESEND_FROM_EMAIL`

예시:
```text
PORT=3000
LLM_MODEL=gpt-4.1-mini
LLM_API_BASE_URL=https://api.openai.com/v1
STT_MODEL=gpt-4o-mini-transcribe
STT_API_BASE_URL=https://api.openai.com/v1
```

## 4. 배포 후 확인
- `https://<your-render-service>.onrender.com/health` 호출 시 `{ "ok": true }`가 보여야 합니다.
- API 경로는 아래와 같습니다.
  - `POST /summary`
  - `POST /processAudio`
  - `POST /sendEmail`

## 5. Android 앱 연결
- `gradle.properties`의 `MEETING_MEMO_API_BASE_URL`을 아래처럼 변경합니다.

```properties
MEETING_MEMO_API_BASE_URL=https://<your-render-service>.onrender.com/
```

- 값을 바꾼 뒤 앱을 다시 빌드하고 재설치합니다.

## 6. 무료 플랜 참고
- Render 무료 플랜은 일정 시간 요청이 없으면 슬립될 수 있습니다.
- 첫 요청 시 콜드스타트로 응답이 느릴 수 있습니다.
- 오디오 업로드와 외부 STT/LLM 호출 비용은 Render 무료 여부와 별개로 발생할 수 있습니다.
