# Etapa de construcción: Usa una imagen de Maven con JDK 17 para construir el proyecto
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace/app

# Copia el pom.xml y los archivos wrapper de Maven para descargar dependencias
COPY pom.xml .
COPY .mvn/ .mvn/
COPY mvnw .
COPY src/ src/

# Ejecuta el comando de Maven para construir el proyecto y generar el JAR
# -DskipTests omite la ejecución de pruebas unitarias para acelerar la construcción
RUN ./mvnw package -DskipTests

# Etapa de ejecución: Usa una imagen ligera de OpenJDK 17 para ejecutar la aplicación
FROM openjdk:17-jdk-slim
WORKDIR /app

# Copia el JAR construido desde la etapa 'build' al directorio de trabajo de la imagen final
# Asegúrate de que el patrón coincida con el nombre de tu JAR generado en la carpeta target
COPY --from=build /workspace/app/target/project-management-*.jar app.jar

# Expone el puerto en el que corre tu aplicación Spring Boot (usualmente 8080)
EXPOSE 8080

# Argumento para opciones de Java, puedes pasarlas al construir la imagen o como variables de entorno en Render
ARG JAVA_OPTS
ENV JAVA_OPTS=$JAVA_OPTS

# Comando para ejecutar la aplicación cuando el contenedor inicie
# La opción -Djava.security.egd=file:/dev/./urandom es una buena práctica para Spring Boot para evitar problemas de entropía
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom"]
# Si JAVA_OPTS no está vacío, se añadirán esas opciones. Si está vacío, no se añadirán.
# Para asegurar que JAVA_OPTS se use si está definido:
# ENTRYPOINT exec java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app/app.jar
# O, de forma más explícita y manejando el caso de JAVA_OPTS vacío:
CMD ["sh", "-c", "exec java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app/app.jar"]
