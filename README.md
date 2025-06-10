# 선견지표 Backend API

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.java.net/projects/jdk/17/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

> 선견지표 실시간 선거 정보 플랫폼의 백엔드 API 서버

## 📋 프로젝트 개요

선견지표 백엔드는 Spring Boot 기반의 REST API 서버로, 디지털 선거 정보 플랫폼의 핵심 비즈니스 로직과 데이터 처리를 담당합니다.

**본 사이트는 교육 목적으로 제작된 모의 플랫폼으로, 특정 정당이나 후보를 지지하지 않습니다.**


## 프로젝트 화면 미리보기

###  메인 대시보드
![메인 대시보드](/src/main/resources/static/assets/readme/main.jpg)

---

## **핵심 기능 - 선거 정보 & 투표**

###  후보·정책 비교
![후보 정책 비교](/src/main/resources/static/assets/readme/candidate-compare.jpg)

###  실시간 모의투표 페이지
![모의투표 화면](/src/main/resources/static/assets/readme/mock-voting.jpg)

###  투표소 지도 
![투표소 지도](/src/main/resources/static/assets/readme/find-polling-station.jpg)

---

## **소통 & 커뮤니티 기능**

###  실시간 채팅
<table>
<tr>
<td><img src="/src/main/resources/static/assets/readme/chat1.jpg" width="400"></td>
<td><img src="/src/main/resources/static/assets/readme/chat2.jpg" width="400"></td>
</tr>
<tr>
<td align="center">비속어 필터링 시스템</td>
<td align="center">참가자 목록</td>
</tr>
</table>

###  커뮤니티
<table>
<tr>
<td><img src="/src/main/resources/static/assets/readme/community1.jpg" width="400"></td>
<td><img src="/src/main/resources/static/assets/readme/community2.jpg" width="400"></td>
</tr>
<tr>
<td align="center">게시글 목록형</td>
<td align="center">게시글 카드형</td>
</tr>
<tr>
<td colspan="2" align="center"><img src="/src/main/resources/static/assets/readme/community3.jpg" width="400"></td>
</tr>
</table>

---

##  **AI 지원 기능**


###  챗봇 QnA
![챗봇 QnA](/src/main/resources/static/assets/readme/chatbot.jpg)

###  정책 단어 검색
![정책 단어 검색](/src/main/resources/static/assets/readme/policy-terms.jpg)

---

## **교육 & 참여 기능**

###  정책 퀴즈
<table>
<tr>
<td><img src="/src/main/resources/static/assets/readme/electionQuiz1.jpg" width="400"></td>
<td><img src="/src/main/resources/static/assets/readme/electionQuiz2.jpg" width="400"></td>
</tr>
</table>

---

###  관리자 페이지
<table>
<tr>
<td><img src="/src/main/resources/static/assets/readme/admin1.jpg" width="400"></td>
<td><img src="/src/main/resources/static/assets/readme/admin2.jpg" width="400"></td>
</tr>
<tr>
<td align="center">대시보드</td>
<td align="center">회원 관리</td>
</tr>
<tr>
<td><img src="/src/main/resources/static/assets/readme/admin3.jpg" width="400"></td>
<td><img src="/src/main/resources/static/assets/readme/admin4.jpg" width="400"></td>
</tr>
<tr>
<td align="center">게시판 관리</td>
<td align="center">신고내역</td>
</tr>
</table>

---

## 🎯 주요 기능

- **후보·정책 비교**: 공약, 정책, 경력 등 항목별 후보 정보 제공
- **가상 투표 시뮬레이터**: 실제 투표처럼 해보고 결과를 통계로 확인
- **정책 퀴즈**: 재미있게 정치 리터러시 향상
- **정책 추천**: 관심사 기반 맞춤형 정책 탐색
- **투표소 위치 안내**: GPS와 주소 입력으로 쉽게 찾기
- **선거 일정 알림**: 중요한 날짜 푸시로 안내
- **실시간 채팅/커뮤니티**: 의견 나누고 소통하는 공간
- **챗봇 QnA**: 궁금한 정책, 바로 검색
- **웹 페이지 번역** : Google Translator API 활용 기능 구현
- **정책 단어 검색** : 팝업 기능 구현
- **마이페이지, 관리자 페이지** 구현 : 회원 정보 수정, 회원 관리, 게시판 관리, 신고 내역 관리

---

## 🛠 기술 스택

![기술 스택](/src/main/resources/static/assets/readme/techstack.jpg)
*프로젝트에서 사용된 주요 기술들*

### Backend
- **Spring Boot** 3.x
- **MyBatis** - SQL 매퍼 프레임워크
- **Spring MVC** - REST API
- **JPA/Hibernate** - 객체-관계 매핑
- **FastAPI** - Python 기반 추가 API 서버
- **WebSocket + STOMP** - 실시간 메시징 프로토콜
- **Client Secret, JWT**: 보안

### Database & ORM
- **MySQL** - 사용자, 투표, 후보자 데이터
- **MongoDB** - 채팅 메시지 저장
- **Redis** - 인메모리 캐시 (세션, 실시간 데이터)

 ### Frontend
- **React**:  18.x
- **Zustand**: 상태관리 라이브러리
- **styled-components**: React 컴포넌트 스타일링
- **Tiptap**: 리치 텍스트 에디터
- **Chart.js**: 데이터 시각화
- **ethers.js**: 블록체인 연동

### External APIs & Services
- **Google Perspective API** - 텍스트 분석 및 비속어 필터링
- **네이버 지도 API** - 지도 서비스 및 지오코딩
- **공공데이터 포털 API** - 투표소 정보 조회
- **YouTube Data API v3** - 유튜브 데이터 연동
- **Wikipedia API** - 위키피디아 데이터 조회
- **Google Translate API** - 번역 서비스
- **Google reCAPTCHA** - 봇 방지 및 보안

### Tools & Libraries
- **OpenAI API** - AI 기반 서비스
- **Elasticsearch** - 검색엔진 및 데이터 분석
- **Jsoup (Java), BeautifulSoup (Python)** - HTML 파싱 및 웹 스크래핑

### Infrastructure & DevOps
- **Docker** - 컨테이너화
- **Google Cloud Platform** - 클라우드 인프라
- **GitHub Actions** - CI/CD 파이프라인
- **Firebase** - 추가 백엔드 서비스

### Blockchain 
- **Solidity** - 스마트 컨트랙
- **MetaMask** - 지갑 연동

### Collaboration Tools
- **Visily** - 디자인/프로토타이핑
- **Notion** - 문서화
- **Slack** - 소통
- **Git, GitHub**: 버전 관리

---

## 🚀 프로젝트 설치 및 실행

### 필수 요구사항
- Java 17+, Maven 3.6+
- MySQL 8.0+, MongoDB 4.4+

### 실행하기

```bash
# 1. 저장소 클론
git clone https://github.com/kdt-proj1-team/mock-election-backend.git
cd mock-election-backend

# 2. 데이터베이스 설정 (MySQL)
mysql -u root -p
CREATE DATABASE election_db;

# 3. MongoDB 실행 (별도 터미널)
mongod

# 4. 애플리케이션 실행
./mvnw spring-boot:run
```

---

## 👥 팀 구성 및 역할

| 역할 | 이름 | 주요 담당 | 이메일 | GitHub |
|------|------|-----------|--------|--------|
| **팀장 & Full-stack** | 이종훈 | 정책 퀴즈, 챗봇, 문서화 | lonlove0032@gmail.com | [@jh1126-lee](https://github.com/jh1126-lee) |
| **Full-stack** | 이미르 | 투표 시스템, 인증, 정책 추천, Git 관리 | alfm2766@jbnu.ac.kr | [@lalala5772](https://github.com/lalala5772) |
| **Full-stack** | 강윤진 | 커뮤니티, Storage | riveryj.kang@gmail.com | [@yunjinkang](https://github.com/yunjinkang) |
| **Full-stack** | 김미랑 | 지도, 채팅, 문서화| codekr76@gmail.com | [@codekr76](https://github.com/codekr76) |
| **Full-stack** | 박주혁 | 토론 클립, 관리자, DB 설계 | ksoark0109@naver.com | [@AI-hyeok](https://github.com/AI-hyeok) |
| **Full-stack** | 이동엽 | 후보 비교, 프론트앤드 & 백앤드 배포, Git 관리 | booo7575@naver.com | [@LeeDongYup7575](https://github.com/LeeDongYup7575) |

---

## 📄 라이선스

```bash
이 프로젝트는 MIT 라이선스 하에 배포됩니다.
This project is licensed under the MIT License.
```
