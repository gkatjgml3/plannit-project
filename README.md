# 📅 PLANNIT

> **AI와 함께하는 스마트 일정 관리 서비스**

학교에서 받은 수행평가 안내문, 시험 안내 자료, 공지문 등의 이미지를 업로드하면 AI가 이미지 속 일정 정보를 분석하고 캘린더에 자동으로 등록해 주는 서비스입니다.

---

## 📌 프로젝트 소개

학생들은 학교에서 제공되는 수행평가 안내문, 공지사항, 시험 일정 등을 직접 확인하고 캘린더나 플래너에 하나씩 입력해야 하는 불편함이 있습니다.

**PLANNIT은 이러한 문제를 해결하기 위해 AI 이미지 분석 기술을 활용합니다.**

사용자가 안내문이나 공지문 사진을 업로드하면 AI가 이미지 속 정보를 분석하여 일정 제목, 날짜, 시간 등의 정보를 추출하고 캘린더에 자동으로 등록합니다.

또한 등록된 일정은 수정 및 관리할 수 있으며, 친구들과 일정을 공유하여 조별 과제나 동아리 활동 등의 일정도 함께 관리할 수 있습니다.

---

## 🎯 주요 기능

### 🖼️ 이미지 기반 일정 등록

학교에서 받은 수행평가 안내문이나 공지문을 사진으로 업로드합니다.

AI가 이미지 속 내용을 분석하여 일정 정보를 자동으로 추출합니다.

---

### 🤖 AI 일정 분석

Gemini API를 활용하여 이미지 속 정보를 분석합니다.

다음과 같은 정보를 추출하여 일정 데이터로 변환합니다.

- 일정 제목
- 날짜
- 시간
- 장소
- 일정 상세 내용

Gemini는 이미지 입력을 처리할 수 있으며 구조화된 JSON 형식으로 데이터를 반환하도록 구성할 수 있어 일정 정보 추출에 활용할 수 있습니다. citeturn0search0turn0search1

---

### 📅 자동 캘린더 등록

AI가 분석한 일정 정보를 캘린더에 자동으로 등록합니다.

사용자는 등록된 일정을 확인하고 필요한 경우 직접 수정할 수 있습니다.

---

### 🔔 일정 알림

시험, 수행평가, 과제 등 중요한 일정이 가까워지면 알림을 제공합니다.

사용자가 중요한 일정을 놓치지 않고 계획적으로 준비할 수 있도록 돕습니다.

---

### 👥 일정 공유

친구, 조별 과제 팀, 동아리 등의 그룹과 일정을 공유할 수 있습니다.

여러 사람이 함께 관리해야 하는 일정을 더욱 편리하게 확인하고 관리할 수 있습니다.

---

## 🏗️ System Architecture

```text
┌──────────────────────────────┐
│          Frontend            │
│      HTML / CSS / JS         │
│      Mobile Optimized Web    │
└──────────────┬───────────────┘
               │
           REST API
            / JSON
               │
               ▼
┌──────────────────────────────┐
│           Backend            │
│      Java / Spring Boot      │
│                              │
│  일정 관리 · 사용자 데이터     │
│  이미지 업로드 · API 처리      │
└──────────────┬───────────────┘
               │
           Gemini API
               │
               ▼
┌──────────────────────────────┐
│          AI Engine           │
│                              │
│    Image Understanding       │
│    Schedule Extraction       │
│    Structured JSON Output    │
└──────────────────────────────┘
```

---

## 🔄 Service Flow

```text
📷 안내문 사진 업로드
        ↓
🤖 Gemini AI 이미지 분석
        ↓
📄 일정 정보 추출
        ↓
🔍 사용자 확인 및 수정
        ↓
📅 캘린더 자동 등록
        ↓
🔔 일정 알림 및 공유
```

---

## 🛠 Tech Stack

### Frontend

- HTML
- CSS
- JavaScript

### Backend

- Java
- Spring Boot

### AI

- Gemini API
- Image Understanding
- Structured JSON Output

### Tools

- Figma
- GitHub
- Visual Studio Code
- IntelliJ IDEA
- Notion

---

## 📂 Project Structure

```text
plannit-project
│
├── frontend/
│   ├── html/
│   ├── css/
│   └── js/
│
├── backend/
│   └── Spring Boot
│
├── docs/
│   ├── planning/
│   └── meeting/
│
└── README.md
```

> 실제 폴더 구조에 맞게 추후 수정 예정

---

## 👥 Team

| 이름 | 역할 |
|---|---|
| 김래원 | 기획 및 개발 |
| 김윤수 | 기획 및 개발 |
| 손예준 | 기획 및 개발 |
| 함서희 | 프론트엔드 및 주요 디자인 설계 |

---

## 🎨 Design

프로젝트의 UI/UX 디자인은 Figma를 기반으로 제작되었습니다.

[Figma Design](https://www.figma.com/design/gwXMAeV2ZnSeJLfUhU3TX4/plannit?node-id=0-1&t=KMMbWhjt1i0npws9-1&utm_source=chatgpt.com)

---

## 💻 Repository

[PLANNIT GitHub Repository](https://github.com/gkatjgml3/plannit-project?utm_source=chatgpt.com)

---

## 📌 Ground Rules

### 코드 컨벤션

변수명과 함수명은 `camelCase` 형식으로 작성합니다.

```javascript
uploadImage()
getScheduleData()
createCalendarEvent()
```

---

### 주석 작성

함수 선언문 위에는 다음 내용을 작성합니다.

```javascript
// 입력값, 출력값, 기능 요약
```

---

### 진척도 공유

개발이 완료된 작업은 팀 Notion 보드의 진행 상황을 업데이트합니다.

---

## 📅 Development Progress

| 구분 | 내용 | 상태 |
|---|---|---|
| 서비스 기획 | 아이디어 및 기능 구체화 | 진행 |
| UI/UX 디자인 | Figma 화면 설계 | 진행 |
| Frontend | 모바일 최적화 웹 구현 | 진행 |
| Backend | Spring Boot 서버 구축 | 예정 |
| AI | Gemini API 이미지 분석 기능 | 예정 |
| Integration | Frontend · Backend · AI 연동 | 예정 |
| Testing | 기능 테스트 및 오류 수정 | 예정 |

---

## 🛠 Error & Solution Log

프로젝트 진행 중 발생한 오류와 해결 방법을 기록합니다.

| 제목 | 날짜 | 원인 | 해결 방법 | 작성자 |
|---|---|---|---|---|
| 추후 작성 | | | | |

---

## 📝 Meeting Log

### 2026.07.15

**시간:** 09:50 ~ 11:00

- 프로젝트 문서 및 노션 구성 확인
- 와이어프레임 및 웹 화면 구성 논의

**진행 상태:** 와이어프레임 제작 진행

---

### 2026.08.26

**시간:** 10:00 ~ 11:00

- 주요 기능 구체화
- 세부 기능 정의
- 서비스 화면 구성 논의

---

## 🚀 Future Plans

- AI 이미지 일정 분석 기능 구현
- 자동 캘린더 등록 기능 구현
- 일정 수정 및 삭제 기능 구현
- 일정 알림 기능 구현
- 그룹 일정 공유 기능 구현
- 사용자 테스트 및 UI/UX 개선

---

## 💡 Project Goal

**"사진 한 장으로 시작하는 스마트한 일정 관리"**

PLANNIT은 반복적인 일정 입력의 불편함을 줄이고, 사용자가 중요한 일정을 놓치지 않도록 돕는 AI 기반 일정 관리 서비스를 목표로 합니다.
