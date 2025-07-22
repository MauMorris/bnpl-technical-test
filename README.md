# BNPL (Buy Now Pay Later) - Sistema de Crédito

Este proyecto simula los eventos principales de un sistema de creación y gestión de crédito para un negocio de tipo "Compre Ahora, Pague Después". La aplicación consiste en una API REST desarrollada con Java y Spring Boot.

---

## Tecnologías Utilizadas

- **Lenguaje:** Java 17
- **Framework:** Spring Boot 3.x
- **Acceso a Datos:** Spring Data JPA / Hibernate
- **Base de Datos:** PostgreSQL (orquestada con Docker y Testcontainers para pruebas)
- **Seguridad:** Spring Security (con autenticación JWT Bearer Token)
- **Gestión de Dependencias:** Maven
- **Pruebas:** JUnit 5, Mockito
- **Containerización:** Docker & Docker Compose
- **Pipeline CI/CD:** Con Github Actions que se dispare con cada `push` a cualquier rama.
---

## Funcionalidades Implementadas

La API expone dos endpoints principales:

### 1. Registro de Cliente (Customer)
- **Endpoint:** `POST /v1/customers`
- **Descripción:** Registra un nuevo cliente en el sistema. La línea de crédito se asigna automáticamente basándose en la edad del cliente.
- **Reglas de Negocio:**
    - **18 a 25 años:** $3,000 MXN
    - **26 a 30 años:** $5,000 MXN
    - **31 a 65 años:** $8,000 MXN
    - Clientes fuera de este rango de edad no son aceptados.

### 2. Registro de Préstamo (Loan)
- **Endpoint:** `POST /v1/loans`
- **Descripción:** Registra un loan para un cliente existente, validando contra su línea de crédito disponible.
- **Reglas de Negocio:**
    - El préstamo (Loan) es rechazado si el monto excede el crédito disponible.
    - Se asigna un esquema de pago y una tasa de interés según las siguientes reglas (en orden de prioridad):
        1.  **Scheme 1 (13% interés):** Si el primer nombre del cliente empieza con 'C', 'L', o 'H'.
        2.  **Scheme 2 (16% interés):** Por defecto, si la regla anterior no aplica.
    - Todos los préstamos se dividen en 5 pagos quincenales.

---

## Cómo Ejecutar el Proyecto

Para levantar el entorno completo (aplicación + base de datos), solo necesitas tener **Docker** y **Docker Compose** instalados.

1.  **Clonar el Repositorio:**
    ```bash
    git clone https://github.com/MauMorris/bnpl-technical-test.git
    cd bnpl-technical-test
    ```

2.  **Levantar los Contenedores:**
    Desde la raíz del proyecto, ejecuta el siguiente comando. La primera vez puede tardar unos minutos mientras se descargan y construyen las imágenes.
    ```bash
    docker-compose up --build
    ```
    La aplicación estará disponible en `http://localhost:8080`.

3.  **Probar la API:**
    Puedes usar cualquier cliente de API como Postman. La API está protegida con JWT.
    - **Paso 1: Obtener un Token de Autenticación**
        - `POST http://localhost:8080/v1/auth/login`
        - **Body (JSON):**
          ```json
          {
            "username": "testuser",
            "password": "testpass"
          }
          ```
        - La respuesta te dará un token. Cópialo.
    - **Ejemplo de Petición (Crear Cliente):**
        - `POST http://localhost:8080/v1/customers`
        - **Body (JSON):**
          ```json
          {
            "firstName": "Juan",
            "lastName": "Perez",
            "secondLastName": "Garcia",
            "dateOfBirth": "1990-01-01"
            }
          ```
        - **Headers:** Añade un header `Authorization` con el valor `Bearer <TU_TOKEN_COPIADO_AQUI>`.
---

## Pruebas

El proyecto incluye una suite de pruebas unitarias para la lógica de negocio. Para ejecutarlas, puedes usar el wrapper de Maven incluido:

```bash
./mvnw test

---

## Pipelines

CI/CD (Integración y Despliegue Continuo):
Se tiene un pipeline en **GitHub Actions** que se dispare con cada `push` a cualquier rama.
    * El pipeline ejecuta los siguientes pasos:
        1. **Checkout repository:** Clona el código del repositorio.
        2. **Set up JDK 17:** Configura el entorno de Java 17.
        3. **Build and test with Maven:** Compila el código y ejecuta la suite completa de pruebas.
        4. **Upload JaCoCo coverage report:** Guarda el reporte de cobertura de pruebas como un artefacto.
        5. **Extract Docker metadata:** Genera las etiquetas y metadatos para la imagen Docker (solo en la rama `main`).
        6. **Log in to GitHub Container Registry:** Inicia sesión en el registro de contenedores (solo en la rama `main`).
        7. **Build and push Docker image:** Construye la imagen Docker y la publica en el registro (solo en la rama `main`).

---

## Futuras Mejoras y Siguientes Pasos

Aunque Se proponen los siguientes pasos para llevarlo a un siguiente nivel de madurez:

* **Documentación de API con OpenAPI (Swagger):**
    * Integrar la dependencia `springdoc-openapi` para generar automáticamente una especificación OpenAPI 3.
    * Esto proporcionaría una UI de Swagger (`/swagger-ui.html`) para que los consumidores de la API puedan explorar y probar los endpoints de forma interactiva.

* **Gestión de Secretos:**
    * Externalizar los datos sensibles (como las credenciales de la base de datos y de la API) del archivo `application.properties`.
    * Integrar la aplicación con **Azure Key Vault** para gestionar estos secretos de forma segura, en lugar de tenerlos en el código fuente o en variables de entorno simples.

* **Monitorización y Métricas:**
    * Habilitar los endpoints de **Spring Boot Actuator** para exponer métricas de salud y rendimiento de la aplicación.
    * Configurar un stack de monitorización con **Prometheus** para recolectar las métricas y **Grafana** para visualizarlas en dashboards, permitiendo una observabilidad completa del estado de la aplicación en producción.

* **Subir a producción utilizando infraestructura Cloud:**
    * Se sugiere subir la aplicación contenerizada a un servicio de Azure
    * Utilizar una BD de Servicio de la nube ya que la imagen de la BD de Docker se recomienda utilizar solamente para fase de pruebas
    * Generar 2 grupos de recursos, 1 para QA y 1 para Prod
    * Utilizar Terraform para mantener una correcta gestión de las infraestructuras de Azure

* **Microservicios:**
    * Si se generan más servicios se sugiere utilizar una arquitectura de microservicios
    * Utilizar Kubernetes para gestionarlos
    * Generar una estrategia de BD en caso de que se genere una BD por servicio (Recomendado)
    * Modificar Deploy del pipeline: Desplegar la nueva versión de la imagen en **Azure Kubernetes Service (AKS)**, actualizando los pods de forma controlada (ej. Rolling Update).