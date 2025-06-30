# BNPL (Buy Now Pay Later) - Sistema de Crédito

Este proyecto simula los eventos principales de un sistema de creación y gestión de crédito para un negocio de tipo "Compre Ahora, Pague Después". La aplicación consiste en una API REST desarrollada con Java y Spring Boot.

---

## Tecnologías Utilizadas

- **Lenguaje:** Java 17
- **Framework:** Spring Boot 3.x
- **Acceso a Datos:** Spring Data JPA / Hibernate
- **Base de Datos:** PostgreSQL (orquestada con Docker)
- **Seguridad:** Spring Security (con autenticación HTTP Basic)
- **Gestión de Dependencias:** Maven
- **Pruebas:** JUnit 5, Mockito
- **Containerización:** Docker & Docker Compose

---

## Funcionalidades Implementadas

La API expone dos endpoints principales:

### 1. Registro de Cliente
- **Endpoint:** `POST /api/v1/clients`
- **Descripción:** Registra un nuevo cliente en el sistema. La línea de crédito se asigna automáticamente basándose en la edad del cliente.
- **Reglas de Negocio:**
    - **18 a 25 años:** $3,000
    - **26 a 30 años:** $5,000
    - **31 a 65 años:** $8,000
    - Clientes fuera de este rango de edad no son aceptados.

### 2. Registro de Compra
- **Endpoint:** `POST /api/v1/purchases`
- **Descripción:** Registra una compra para un cliente existente, validando contra su línea de crédito disponible.
- **Reglas de Negocio:**
    - La compra es rechazada si el monto excede el crédito disponible.
    - Se asigna un esquema de pago y una tasa de interés según las siguientes reglas (en orden de prioridad):
        1.  **Scheme 1 (13% interés):** Si el primer nombre del cliente empieza con 'C', 'L', o 'H'.
        2.  **Scheme 2 (16% interés):** Si el ID del cliente es mayor a 25.
        3.  **Scheme 2 (16% interés):** Por defecto, si ninguna de las reglas anteriores aplica.
    - Todas las compras se dividen en 5 pagos quincenales.

---

## Cómo Ejecutar el Proyecto

Para levantar el entorno completo (aplicación + base de datos), solo necesitas tener **Docker** y **Docker Compose** instalados.

1.  **Clonar el Repositorio:**
    ```bash
    git clone [https://github.com/MauMorris/bnpl-technical-test.git](https://github.com/MauMorris/bnpl-technical-test.git)
    cd bnpl-technical-test
    ```

2.  **Levantar los Contenedores:**
    Desde la raíz del proyecto, ejecuta el siguiente comando. La primera vez puede tardar unos minutos mientras se descargan y construyen las imágenes.
    ```bash
    docker-compose up --build
    ```
    La aplicación estará disponible en `http://localhost:8080`.

3.  **Probar la API:**
    Puedes usar cualquier cliente de API como Postman.
    - **Autenticación:** La API está protegida con HTTP Basic Auth. Usa las siguientes credenciales:
        - **Usuario:** `testuser`
        - **Contraseña:** `testpass`
    - **Ejemplo de Petición (Crear Cliente):**
        - `POST http://localhost:8080/api/v1/clients`
        - **Body (JSON):**
          ```json
          {
              "name": "Ana García",
              "birthDate": "2000-05-15"
          }
          ```

---

## Pruebas

El proyecto incluye una suite de pruebas unitarias para la lógica de negocio. Para ejecutarlas, puedes usar el wrapper de Maven incluido:

```bash
./mvnw test