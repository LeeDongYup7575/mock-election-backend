# 선견지표 Backend API

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.java.net/projects/jdk/17/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

> 선견지표 실시간 선거 여론조사 플랫폼의 백엔드 API 서버

## 📋 프로젝트 개요

선견지표의 백엔드는 Spring Boot 기반의 REST API 서버로 유권자들이 후보자와 정책에 대한 정보를 쉽고 정확하게 이해하고 비교할 수 있도록 돕는 **디지털 선거 정보 플랫폼**입니다.

**본 사이트는 교육 목적으로 제작된 모의 플랫폼으로, 특정 정당이나 후보를 지지하지 않습니다.**



### 🎯 주요 기능
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


## 🛠 기술 스택

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
