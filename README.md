Network unreachable 뜨면 경로 문제인 거 알았고, tenant not found 뜨면 계정 이름 규칙 문제 ㅠ < railway 서버 배포문제

db 제대로 작동되다가 아예 연결이 안되는 문제 : firebase 무료버전이 7일동안 사용이 없을시 죽어버리는 문제. 직접 들어가서 다시 활성화 완료. 

Supabase 연결 실패
Direct Connection은 IPv6 전용이라 Railway/PGAdmin 같은 IPv4 환경에서는 접속 불가.
그래서 계속 “Not IPv4 compatible” 에러발생
>> Direct Connection 대신 Session Pooler 정보를 사용.
Host/User를 Session Pooler용으로 바꾸자 IPv4 환경에서도 정상 연결됨.

Firebase 에러 (Invalid PKCS#8 data)
서비스 계정 키 JSON의 private_key를 잘못 저장하거나 줄바꿈(\n)을 지운 게 원인.
PKCS#8 형식이 깨져서 Admin SDK 초기화가 실패.
>> 서비스 계정 키 JSON을 다시 생성.
private_key를 헤더(-----BEGIN PRIVATE KEY-----), 푸터(-----END PRIVATE KEY-----), 줄바꿈(\n)까지 그대로 유지해서 Railway 환경변수에 저장.
코드에서 replace(/\\n/g, '\n') 처리로 실제 줄바꿈으로 변환 → 에러 해결.
