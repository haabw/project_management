# 개발 환경 설정 가이드

이 문서는 프로젝트 협업 서비스 개발을 위한 환경 설정 방법을 설명합니다.

## 1. 필수 소프트웨어

- **JDK 17**:
    - 다운로드: [oracleJDK 17.0.14](https://www.oracle.com/java/technologies/javase/jdk17-0-13-later-archive-downloads.html)
    - 환경 변수: `JAVA_HOME` 설정
    - 자바 컴파일러 설정: `Preferences > Java > Compiler` 에서 JDK 17로 변경
    - 자바 JRE 설정: `Preferences > Java > Installed JREs` 에서 `edit > 자바 설치 경로 > C:\Program Files\Java\jdk-17` 추가 후 jdk-17선택하고 `Apply`
- **Eclipse 2025-03**:
    - 패키지: Eclipse IDE for Enterprise Java and Web Developers
    - 다운로드: [Eclipse Downloads](https://www.eclipse.org/downloads/)
- **Apache Tomcat 10.0.1**:
    - 다운로드: [Tomcat 10.1.40](https://tomcat.apache.org/download-10.cgi)
    - Eclipse 서버 설정: `Preferences > Server > Runtime Environments`
- **MySQL 8.4.1**:
    - 설치 후 `collab` 데이터베이스 생성
- **Maven**:
    - Eclipse 내장 또는 별도 설치

## 2. 프로젝트 설정

1. 팀장이 만든 레포를 fork
2. fork 레포지토리를 로컬에 클론: `git clone <각자 레포지토리 링크>`
3. 로컬에서 돌아가게 필수 소프트웨어 점검 및 세팅
    - Dynamic Web Module Version: 5.0
    - Target Runtime: Tomcat 10.0.1
4. `pom.xml` 확인 및 `mvn clean install`
5. 서버 돌아가는지 실행

## 3. 주의사항

- Tomcat 10.0.1은 Jakarta EE 9.1 사용 (`jakarta.*` 패키지).
- pull했는 시작부터 오류가 난다
    - JDK 17이 프로젝트 Build Path에 설정되었는지 확인.
    - Apache Tomcat이 `Preferences > Server > Runtime Environments`
- `git add → git commit and push`는 각자 레포지토리에 하기.
