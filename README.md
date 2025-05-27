## Para empezar

### Prerequisitos

- Versión de maven 3.9.9
- Versión de java jdk-17
- Instalar ngrok para liberar los puertos

### Instalación

1. Clona el repositorio

   ```sh
   git https://github.com/alvaro-cozano/taskor-react.git
   ```

2. Crea el application.properties en la ruta "src\main\resources"

   ```sh
   spring.application.name=

    spring.jpa.properties.hibernate.jdbc.time_zone=Europe/Madrid

    spring.datasource.url=
    spring.datasource.username=
    spring.datasource.password=
    spring.datasource.driver-class-name=
    spring.jpa.database-platform=
    spring.jpa.show-sql=
    spring.jpa.hibernate.ddl-auto=create

    app.base-url=http://localhost:8080
    app.front-url=http://localhost:5173

    spring.security.oauth2.client.registration.google.client-id=
    spring.security.oauth2.client.registration.google.client-secret=
    spring.security.oauth2.client.registration.google.scope=profile,email
    spring.security.oauth2.client.registration.google.redirect-uri=http://localhost:5173/auth/login/google
    spring.security.oauth2.client.registration.google.authorization-grant-type=authorization_code

    spring.security.oauth2.client.provider.google.authorization-uri=https://accounts.google.com/o/oauth2/v2/auth
    spring.security.oauth2.client.provider.google.token-uri=https://oauth2.googleapis.com/token
    spring.security.oauth2.client.provider.google.user-info-uri=https://www.googleapis.com/oauth2/v3/userinfo
    spring.security.oauth2.client.provider.google.jwk-set-uri=https://www.googleapis.com/oauth2/v3/certs

    spring.mail.host=
    spring.mail.port=
    spring.mail.username=
    spring.mail.password=
    spring.mail.properties.mail.smtp.auth=true
    spring.mail.properties.mail.smtp.starttls.enable=true

    stripe.secret.key=
    stripe.publishable.key=

    stripe.webhook.secret=
    stripe.price.id=
   ```

3. Configuracion de inicio de sesión con Google ( https://console.cloud.google.com/auth/clients/953403003242-r97lfvcorglqk3pvccnq2410ndvtkkjn.apps.googleusercontent.com?hl=es&inv=1&invt=Abyhcg&project=project-management-457508 )

   ```sh
    Orígenes autorizados de JavaScript

    URI 1
    http://localhost:5173

    URI 2
    http://localhost:8080

    URIs de redireccionamiento autorizados

    URI 1
    http://localhost:5173/auth/login/google
   ```

4. Configuracion de pagos mediante stripe

   ```sh
   URL del punto de conexión
   http://localhost:8080/api/stripe/webhook
   ```

5. Reinstalar las dependencias

   ```sh
   mvn clean install
   ```

6. Ejecutar ngrok para liberar el puerto del back y sustituir en todos sitios http://localhost:8080 por la url generada

   ```sh
   ngrok http 8080
   ```

7. Ejecuta el proyecto

   ```sh
   mvn clean package
   java -jar target/nombre-del-archivo.jar
   ```
