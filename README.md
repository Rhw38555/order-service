# 주문 관리 API 서버 
주문 관리 API 주요 기능으로는 다음과 같은 기능이 포함됩니다.

- 주문 접수처리 : 사용자가 선택한 상품을 기반으로 주문 접수 처리를 진행한다. 만약 사용자가 결제를 완료한 상황이라면 주문 상태는 접수가 아니라 완료 상태로 변경된다.
- 주문 완료처리 : 사용자가 통장 입금, 신용카드 등의 결제가 완료 되면 기존의 접수 중인 주문을 완료 처리한다.
- 단일 주문조회 : 사용자가 주문에 대한 상세 정보를 조호할 수 있다.
- 주문 목록조회 : 사용자가 주문 목록 정보를 조회할 수 있다.

### 기술 스택
- 개발 언어 : kotlin 1.6.21, java 11
- 서비스 프레임 워크 : Spring Boot 2.7.16
- 디비 :  Mysql
- 기타 라이브러리 : JPA 인터페이스(Spring Data JPA)
- 개발 툴 : IntelliJ IDEA 2022.2.3

### 테이블 설계
- Customer : 고객(사용자) 테이블, 식별자 id, 사용자 id, 비밀번호 
- Product : 상품 테이블, 식별자 id, 상품명, 가격, 상품 수량
- Order : 주문 테이블, 식별자 id, 주문시간, 결제방법, 주문상태, 고객 id
- OrderItem : 주문 상품 정보 테이블, 식별자 id, 주문 수량, 주문 id, 상품 id

Order 엔티티는 주문 정보를 저장하며, 일관성과 독립성을 위해 OrderItem 엔티티에서 주문에 포함된 상품과 수량 정보를 저장합니다.  
Order와 OrderItem은 일대다 관계입니다. 하나의 주문에 여러 개의 주문 상품이 포함될 수 있습니다.  
OrderItem 엔티티의 order 속성을 사용하여 외래키(Foreign Key) 관계를 설정하여 어떤 주문에 어떤 상품이 포함되었는지를 추적합니다.  
Order의 정보가 변경되거나 삭제되면 OrderItem 주문 상품 정보도 변경되거나 삭제될 수 있도록 설정하였습니다.   

### API 테스트 방법
IntelliJ http plugin을 이용해 테스트를 진행할 수 있습니다.  

1. 테스트 도구 설치: 개발 툴 중 하나인 IntelliJ의 'HTTP Client' 플러그인을 설치합니다.  
   IntelliJ에서 'Settings' - 'Plugins'로 이동하여 'HTTP Client'를 검색하고 설치합니다.  
2. API 테스트 예제 가져오기: 'http' 디렉토리에서 'order.http' 파일을 개발 툴로 복사합니다.  
   만약 이미 'order-service' 프로젝트를 IntelliJ에 세팅한 경우, 'order.http' 파일이 이미 존재할 것입니다.  
3. API 테스트 서버 변경: http 파일을 열어보면 각 API에 대한 샘플 테스트가 등록되어 있습니다.  
   'order-service' 프로젝트를 로컬 환경에서 구동하고 테스트하려면, 샘플 예제에서 'localhost'를 그대로 사용합니다.  
   다른 서버의 'order-service' 프로젝트를 테스트하려면, 'localhost' 대신 해당 서버의 IP나 도메인을 입력합니다.  
4. API 테스트 진행: 원하는 테스트를 위해 API 문서 및 데이터베이스 정보를 확인한 후, 테스트 데이터를 변경합니다.  
   예를 들어, 'Pathvariable' 값인 '{orderId}'를 변경하거나 JSON 요청의 값을 수정합니다.  
   값이 설정되면, 파일 왼쪽에 있는 실행 버튼을 클릭하여 각 API를 테스트합니다.  
5. API 결과 확인: 테스트 버튼을 누른 후, HTTP 테스트 결과가 'Services' 탭에서 자동으로 출력됩니다.   
   API 문서를 확인하여 결과 값이 올바르게 출력되는지 확인합니다.  

### 개선점
1. 원본데이터 저장소인 mysql의 부하 및 DeadLock을 방지하기위해 Redis와 Redission을 사용하여 분산 Lock을 관리  
2. 추후 실제 결제 처리 연동이 필요합니다.  
3. 추후 주문 상태 확장 및 알림 기능이 필요합니다.  

### 기타
1. 주문 서비스는 상품의 동시성 문제가 많이 발생할 것이라고 예측되어 비관적 Lock 방식과 Mysql로 분산락을 관리하는 서비스를 구현 하였습니다  .
2. 추후 원본 RDB 저장소 자체에 Lock에 대한 부하를 덜어주고 Rock 관리에 대한 부담을 덜 수 있는 Redis 사용하여 Lock관리를 개선할 수 있습니다.  
   추가직인 인프라를 사용해야되지만 Redis pub/sub 기능을 이용해 Lock이 해제되었을 경우 한,두번의 요청으로 Lock 해제하기 때문에   
   Lock을 지속적으로 확인할 필요가 없어 Lock 관리 부담이 적기 때문에 Redis에서 Lock을 관리해주는 Ression 클라이언트를 이용해 동시성 소스를 구현할 수 있습니다.  
3. JUnit Test를 호환할 수 있고 kotlin DSL을 활용해 가독성 높은 테스트 코드를 작성할 수 있는 Kotest를 이용해 테스트코드를 작성하였습니다.  
4. mysql(RDB) 서비스는 docker-compose.yml(docker-compose up -d)을 통해 실행할 수 있습니다. docker 서비스 실행 후 테스트 및 애플리케이션 실행이 가능합니다.  
