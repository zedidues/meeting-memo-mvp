# API 스모크 테스트 예시

아래 예시는 Render 등에 배포된 HTTP 엔드포인트가 정상 동작하는지 빠르게 확인하기 위한 샘플입니다.

## Summary
```bash
curl -X POST "https://<your-render-service>.onrender.com/summary" \
  -H "Content-Type: application/json" \
  -d "{\"rawText\":\"오늘 회의에서는 안드로이드 앱 MVP 범위와 서버리스 요약 기능, 이메일 발송 흐름을 결정했습니다. 다음 주까지 홈 화면과 메모 저장 기능을 먼저 완성하기로 했습니다.\"}"
```

예상 응답 형태:
```json
{
  "title": "회의 메모 제목",
  "summary": "회의 내용 요약",
  "actionItems": ["액션 아이템 1", "액션 아이템 2"]
}
```

## Send Email
```bash
curl -X POST "https://<your-render-service>.onrender.com/sendEmail" \
  -H "Content-Type: application/json" \
  -d "{\"toEmail\":\"user@example.com\",\"title\":\"회의 메모\",\"rawText\":\"원문 내용\",\"summaryText\":\"요약 내용\"}"
```

예상 응답 형태:
```json
{
  "success": true,
  "message": "이메일을 발송했습니다."
}
```

## Process Audio
```bash
curl -X POST "https://<your-render-service>.onrender.com/processAudio" \
  -H "Content-Type: multipart/form-data" \
  -F "file=@sample-meeting.m4a"
```

예상 응답 형태:
```json
{
  "title": "회의 메모 제목",
  "transcript": "전사된 원문 텍스트",
  "summary": "요약된 내용",
  "actionItems": ["액션 아이템 1", "액션 아이템 2"]
}
```
