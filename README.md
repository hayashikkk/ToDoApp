● Spring Boot Todo Application  
  
  概要  

  Spring Boot + MySQL + BCrypt認証を使用したTodoアプリケーション  
  
  技術スタック  
  
  - Backend: Spring Boot 3.5.3, Java 21  
  - Database: MySQL 8.0  
  - Security: Spring Security + BCryptPasswordEncoder  
  - Frontend: Thymeleaf, HTML5, CSS3, JavaScript  
  - Build: Maven  
  
  主要機能  
  
  - ユーザー登録・ログイン（BCrypt暗号化）  
  - Todo CRUD操作（作成・読取・更新・削除）  
  - フィルタリング（全て・未完了・完了済み）  
  - リアルタイム更新（Ajax）  
  - データ永続化（MySQL）  
  
  セットアップ  

  # リポジトリクローン  
  git clone https://github.com/hayashikkk/ToDoApp.git  

  # MySQL設定（application.properties）  
  spring.datasource.url=jdbc:mysql://localhost:3306/todoapp  
  spring.datasource.username=todouser  
  spring.datasource.password=todopassword  
  
  # アプリケーション実行  
  ./mvnw spring-boot:run  
  
  アクセス  
   
  http://localhost:8081  
  
