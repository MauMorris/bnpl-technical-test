# --- Etapa 1: Compilación (Builder) ---
# Usamos una imagen de Maven que ya tiene el JDK 17 para compilar nuestro proyecto.
FROM maven:3.9.6-eclipse-temurin-17 AS builder

# Establecemos el directorio de trabajo dentro del contenedor.
WORKDIR /app

# Copiamos primero el pom.xml para aprovechar el cache de Docker.
# Si las dependencias no cambian, Docker no las volverá a descargar.
COPY pom.xml .
RUN mvn dependency:go-offline

# Copiamos el resto del código fuente de nuestra aplicación.
COPY src ./src

# Compilamos la aplicación y la empaquetamos en un .jar, saltando las pruebas por ahora.
RUN mvn package -DskipTests

# --- Etapa 2: Ejecución (Runner) ---
# Usamos una imagen mucho más ligera que solo tiene el entorno de ejecución de Java 17.
FROM eclipse-temurin:17-jre-jammy

# Establecemos el directorio de trabajo.
WORKDIR /app

# Copiamos SOLAMENTE el .jar compilado de la etapa anterior (builder).
# Fíjate en la ruta, corresponde a la estructura de un proyecto Maven.
COPY --from=builder /app/target/*.jar app.jar

# Exponemos el puerto 8080, que es donde correrá nuestra aplicación Spring Boot.
EXPOSE 8080

# El comando que se ejecutará cuando el contenedor inicie.
ENTRYPOINT ["java", "-jar", "app.jar"]