# API GATE WAY

---

# 팀바인드 API 게이트웨이 구현 Docs

- Spring reactive 진영의 기술들을 활용하여, 비동기 및 멀티쓰래드를 활용하여 api 를 구현

---

# 요구사항

- [x ] 비동기 및 논블로킹 형싱으로, 게이트웨이의 성능 최적화 작업을 진행 해야한다.
- [x ] 일관된 응답 및 디버깅을 위한 최적화된 Base Response 구현을 한다.
- [] 인증 및 인가에 대해서 spring security 를 활용하여, 필터단에서 처리를 한다.
- [] 게이트웨이 진영(로드벨런서 등등을 포함) 에서 각 로드밸런서 및 비정상 요청에 대한 처리를 할 수 있어야한다.
- [] 해외 요청에 대해선 reject를 구현한다.

------

## GATE WAY 응답 형식

![정상응답.png](images/%EC%A0%95%EC%83%81%EC%9D%91%EB%8B%B5.png)
![처리되ㅣㅈ않은 에러.png](images/%EC%B2%98%EB%A6%AC%EB%90%98%E3%85%A3%E3%85%88%EC%95%8A%EC%9D%80%20%EC%97%90%EB%9F%AC.png)
![처리된 에러.png](images/%EC%B2%98%EB%A6%AC%EB%90%9C%20%EC%97%90%EB%9F%AC.png)
