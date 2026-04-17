# Functions Setup

## 1. 환경 변수 준비
`functions/.env.example`를 복사해 `functions/.env`를 만듭니다.

필수 값:
- `LLM_API_KEY`
- `LLM_MODEL`
- `LLM_API_BASE_URL`
- `STT_API_KEY`
- `STT_MODEL`
- `STT_API_BASE_URL`
- `RESEND_API_KEY`
- `RESEND_FROM_EMAIL`

## 2. 의존성 설치
```bash
npm install
```

## 3. 로컬 에뮬레이터 실행
```bash
npm run serve
```

## 4. 배포
루트의 `.firebaserc.example`를 참고해 `.firebaserc`를 만든 뒤 Firebase 프로젝트 ID를 설정합니다.

```bash
npm run build
npm run deploy
```

## 5. 엔드포인트
- `POST /summary`
- `POST /processAudio`
- `POST /sendEmail`

## 6. 오디오 처리 제한
- 권장 형식: `m4a`, `mp3`, `wav`, `aac`, `ogg`
- 최대 파일 크기: `25MB`
- `processAudio`는 음성 파일 업로드 후 전사와 요약을 한 번에 수행합니다.

Firebase Functions v2를 사용하므로 실제 URL은 일반적으로 아래 형식입니다.

```text
https://<region>-<project-id>.cloudfunctions.net/<functionName>
```
