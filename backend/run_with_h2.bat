@echo off
echo Starting backend with H2 database (no PostgreSQL needed)...
echo.

REM Create a temporary application.properties with H2
copy src\main\resources\application.properties src\main\resources\application.properties.postgresql.bak >nul

(
echo # Server Configuration
echo server.port=8080
echo.
echo # Database Configuration - H2 for development
echo spring.datasource.url=jdbc:h2:mem:campusconnect
echo spring.datasource.driverClassName=org.h2.Driver
echo spring.datasource.username=sa
echo spring.datasource.password=
echo.
echo # JPA Configuration
echo spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
echo spring.jpa.hibernate.ddl-auto=update
echo spring.jpa.show-sql=true
echo spring.jpa.properties.hibernate.format_sql=true
echo.
echo # H2 Console
echo spring.h2.console.enabled=true
echo spring.h2.console.path=/h2-console
echo.
echo # JWT Configuration
echo jwt.secret=your-secret-key-change-this-in-production-to-a-secure-random-string
echo jwt.expiration=86400000
echo.
echo # CORS Configuration
echo spring.web.cors.allowed-origins=http://localhost:3000
echo spring.web.cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
echo spring.web.cors.allowed-headers=*
echo.
echo # Logging
echo logging.level.com.campusconnect=DEBUG
echo logging.level.org.springframework.security=DEBUG
) > src\main\resources\application.properties

echo Starting Spring Boot...
mvnw.cmd spring-boot:run

