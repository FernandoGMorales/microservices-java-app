# Aplicación Java de Microservicios: API de Procesamiento de Pedidos de Alta Concurrencia

Este proyecto implementa una API REST que simula un sistema de procesamiento de pedidos de alta concurrencia, según los requisitos detallados en el Desafío de Código Java. Está diseñado para manejar de manera eficiente múltiples solicitudes simultáneas, aplicando descuentos y procesando pedidos de forma asíncrona.

## Tabla de Contenidos

1.  [Tecnologías Utilizadas](#1-tecnologías-utilizadas)
2.  [Estructura del Proyecto](#2-estructura-del-proyecto)
3.  [Instrucciones de Configuración](#3-instrucciones-de-configuración)
4.  [Ejecutando la Aplicación](#4-ejecutando-la-aplicación)
5.  [Endpoints de la API](#5-endpoints-de-la-api)
6.  [Autenticación](#6-autenticación)
7.  [Base de Datos (Consola H2)](#7-base-de-datos-consola-h2)
8.  [Métricas (Spring Boot Actuator)](#8-métricas-spring-boot-actuator)
9.  [Pruebas de Estrés con JMeter](#9-pruebas-de-estrés-con-jmeter)
10. [Concurrencia y Procesamiento Asíncrono](#10-concurrencia-y-procesamiento-asíncrono)
11. [Pruebas](#11-pruebas)
12. [Posibles Mejoras](#12-posibles-mejoras)

## 1. Tecnologías Utilizadas

* **Java 17**: Lenguaje de Programación
* **Spring Boot 3.2.x**: Framework para construir la API REST
* **Spring Data JPA**: Para interacción con la base de datos y ORM
* **H2 Database**: Base de datos en memoria para persistencia
* **Spring Security**: Para Autenticación Básica
* **Lombok**: Para reducir el código repetitivo (getters, setters, constructores)
* **Maven**: Herramienta de automatización de construcción
* **JUnit 5 & Mockito**: Para pruebas unitarias y de integración
* **Spring Boot Actuator**: Para monitoreo y métricas
* **JMeter**: Para pruebas de estrés

## 2. Estructura del Proyecto

```
microservices-java-app/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── example/
│   │   │           └── microservicesjavaapp/
│   │   │               ├── MicroservicesJavaAppApplication.java
│   │   │               ├── config/
│   │   │               │   └── SecurityConfig.java
│   │   │               ├── controller/
│   │   │               │   ├── CartController.java
│   │   │               │   └── GlobalExceptionHandler.java
│   │   │               ├── model/
│   │   │               │   ├── User.java
│   │   │               │   ├── Product.java
│   │   │               │   ├── Discount.java
│   │   │               │   ├── Cart.java
│   │   │               │   └── CartItem.java
│   │   │               ├── repository/
│   │   │               │   ├── UserRepository.java
│   │   │               │   ├── ProductRepository.java
│   │   │               │   ├── DiscountRepository.java
│   │   │               │   ├── CartRepository.java
│   │   │               │   └── CartItemRepository.java
│   │   │               └── service/
│   │   │                   └── CartService.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── data.sql
│   │       └── schema.sql
│   └── test/
│       └── java/
│           └── com/
│               └── example/
│                   └── microservicesjavaapp/
│                       ├── MicroservicesJavaAppApplicationTests.java
│                       ├── controller/
│                       │   └── CartControllerIntegrationTest.java
│                       └── service/
│                           └── CartServiceTest.java
├── pom.xml
├── app-jmeter-test.jmx  <-- Archivo del plan de pruebas de JMeter
└── README.md
```

## 3. Instrucciones de Configuración

1.  **Clonar el Repositorio (si aplica):**
    ```bash
    git clone <url-del-repositorio>
    cd microservices-java-app
    ```

2.  **Prerrequisitos:**
    * **Java 17 (JDK):** Asegúrate de tener instalado el Kit de Desarrollo de Java versión 17 o superior. Puedes verificarlo con `java -version`.
    * **Maven:** Asegúrate de que Maven esté instalado. Puedes verificarlo con `mvn -v`.

3.  **Construir el Proyecto:**
    Navega al directorio raíz del proyecto (`microservices-java-app`) y ejecuta:
    ```bash
    mvn clean install
    ```
    Este comando compila el código, ejecuta las pruebas y empaqueta la aplicación en un archivo JAR ejecutable.

## 4. Ejecutando la Aplicación

Después de construir el proyecto, puedes ejecutar la aplicación Spring Boot usando Maven:

```bash
mvn spring-boot:run
```

La aplicación se iniciará en `http://localhost:8080`.

## 5. Endpoints de la API

Todos los endpoints de la API requieren Autenticación Básica. Usa `username: user` y `password: password`.

| Método HTTP | Endpoint | Descripción | Cuerpo de la Solicitud (JSON) | Cuerpo de la Respuesta (JSON) / Estado |
| :---------- | :------- | :---------- | :-------------------------------------------------------- | :---------------------------------- |
| `POST`      | `/api/carts` | Crea un nuevo carrito de compras para un usuario. | `{ "userId": <id_usuario> }` | `201 Created` / Objeto `Cart` |
| `POST`      | `/api/carts/{cartId}/items` | Agrega un producto a un carrito existente (actualiza la cantidad si ya existe). | `{ "productCode": "<código_producto>", "quantity": <cantidad> }` | `200 OK` / Objeto `CartItem` |
| `DELETE`    | `/api/carts/{cartId}/items/{productId}` | Elimina un producto de un carrito. | *(Ninguno)* | `204 No Content` |
| `GET`       | `/api/carts/{cartId}/items` | Lista todos los productos (ítems) en un carrito específico. | *(Ninguno)* | `200 OK` / Lista de `CartItem`s |
| `POST`      | `/api/carts/{cartId}/process` | Procesa un pedido de carrito asíncronamente. | *(Ninguno)* | `202 Accepted` / "Estamos procesando su orden" |
| `GET`       | `/api/carts/user/{userId}` | Lista todos los carritos asociados a un usuario específico. | *(Ninguno)* | `200 OK` / Lista de objetos `Cart` |

**Ejemplos de comandos `curl` (usando autenticación básica `user:password`):**

**Crear un Carrito para el Usuario 1:**
```bash
curl -X POST -u user:password -H "Content-Type: application/json" -d '{"userId": 1}' http://localhost:8080/api/carts
```

**Agregar Producto PROD001 (Laptop) al Carrito 1 (asumiendo que el ID del carrito es 1):**
```bash
curl -X POST -u user:password -H "Content-Type: application/json" -d '{"productCode": "PROD001", "quantity": 2}' http://localhost:8080/api/carts/1/items
```

**Listar Productos en el Carrito 1:**
```bash
curl -X GET -u user:password http://localhost:8080/api/carts/1/items
```

**Procesar Carrito 1 (asíncrono):**
```bash
curl -X POST -u user:password http://localhost:8080/api/carts/1/process
```

**Listar Carritos del Usuario 1:**
```bash
curl -X GET -u user:password http://localhost:8080/api/carts/user/1
```

## 6. Autenticación

La API implementa **Autenticación Básica** en todos los endpoints usando Spring Security.
* **Nombre de usuario**: `user`
* **Contraseña**: `password`

## 7. Base de Datos (Consola H2)

La aplicación utiliza una base de datos en memoria H2. Puedes acceder a la consola H2 para ver las tablas y los datos de la base de datos:

* **URL:** `http://localhost:8080/h2-console`
* **URL JDBC:** `jdbc:h2:mem:shoppingcart_db`
* **Nombre de usuario:** `sa`
* **Contraseña:** *(dejar en blanco)*

El archivo `data.sql` (`src/main/resources/data.sql`) precarga las tablas `users`, `products` y `discounts` con datos iniciales.

## 8. Métricas (Spring Boot Actuator)

Spring Boot Actuator está integrado para proporcionar monitoreo e información operativa. Los siguientes endpoints están expuestos:

* **Salud:** `http://localhost:8080/actuator/health`
* **Métricas:** `http://localhost:8080/actuator/metrics` (lista las métricas disponibles)
* **Métricas de Solicitudes HTTP del Servidor:** `http://localhost:8080/actuator/metrics/http.server.requests` (proporciona métricas detalladas sobre el rendimiento de las solicitudes HTTP, incluyendo llamadas exitosas/fallidas y tiempos de respuesta para cada endpoint).

Estas métricas se pueden utilizar para rastrear el uso de la API, los tiempos de respuesta e identificar posibles cuellos de botella en el rendimiento.

## 9. Pruebas de Estrés con JMeter

Se proporciona un plan de pruebas con JMeter para simular solicitudes de alta concurrencia. El archivo `app-jmeter-test.jmx` se encuentra en el directorio raíz de este proyecto.

### Ejecutando el Plan de Pruebas de JMeter:

1.  **Descargar JMeter:** Si aún no lo tienes, descarga Apache JMeter de su sitio web oficial.
2.  **Iniciar JMeter:** Ejecuta `jmeter.bat` (en Windows) o `jmeter.sh` (en Linux/macOS) desde el directorio `bin` de tu instalación de JMeter.
3.  **Abrir el Plan de Pruebas:** En JMeter, ve a "File" -> "Open" y selecciona el archivo `app-jmeter-test.jmx` ubicado en el directorio raíz de este proyecto.
4.  **Limpiar Resultados (Opcional pero Recomendado):** Antes de ejecutar una nueva prueba, es una buena práctica limpiar los resultados anteriores. En JMeter, ve a "Run" -> "Clear All" o haz clic en el icono de la escoba en la barra de herramientas.
5.  **Iniciar la Prueba:** Haz clic en el icono de la flecha verde "Start" en la barra de herramientas de JMeter, o ve a "Run" -> "Start".

### Interpretación de los Resultados de las Pruebas de Estrés:

Concéntrate en las siguientes métricas de los listeners de JMeter (`Summary Report`, `Aggregate Report`, `View Results Tree`):

* **Tiempo de Respuesta Promedio:** Indica el tiempo típico que tarda tu API en responder bajo carga.
* **Throughput (Solicitudes/seg):** El número de solicitudes que tu API puede manejar por segundo. Un mayor throughput es mejor.
* **Error %:** El porcentaje de solicitudes fallidas. Este debe ser lo más cercano posible al 0%.
* **Percentiles (ej., 90º, 95º, 99º):** Muestran los tiempos de respuesta por debajo de los cuales cae un cierto porcentaje de solicitudes, brindando información sobre el rendimiento en el peor de los casos.

**Monitorear los Recursos del Servidor:** Mientras ejecutas las pruebas de JMeter, utiliza herramientas como `jvisualvm`, `jstat` o herramientas a nivel del sistema operativo (`top`, `htop`, Administrador de Tareas) para monitorear el uso de CPU, el consumo de memoria y la E/S de red de tu aplicación Java. Esto ayuda a identificar cuellos de botella de recursos.

## 10. Concurrencia y Procesamiento Asíncrono

* **Control de Concurrencia:** El `CartService` utiliza `java.util.concurrent.locks.ReentrantLock` junto con un `ConcurrentHashMap` para gestionar bloqueos granulares por `Cart`. Esto previene condiciones de carrera cuando múltiples hilos intentan modificar el mismo carrito de compras simultáneamente (por ejemplo, agregando/eliminando productos).
* **Procesamiento de Pedidos Asíncrono:** El método `processOrder` en `CartService` está anotado con `@Async`. Esto permite que el endpoint `POST /api/carts/{cartId}/process` devuelva inmediatamente un mensaje "Estamos procesando su orden" (`202 Accepted`) mientras el trabajo pesado real de validación de pedidos, aplicación de descuentos y cálculo ocurre en un pool de hilos separado en segundo plano. Este diseño mejora la capacidad de respuesta de la API bajo alta carga.

## 11. Pruebas

El proyecto incluye pruebas unitarias y de integración para garantizar la calidad del código y la funcionalidad.

* **Pruebas Unitarias (`CartServiceTest.java`):**
    * Utiliza JUnit 5 y Mockito.
    * Prueba la lógica de negocio del `CartService` de forma aislada, mockeando las dependencias del repositorio.
    * Cubre escenarios de creación, adición/eliminación de productos, listado y procesamiento asíncrono.
* **Pruebas de Integración (`CartControllerIntegrationTest.java`):**
    * Utiliza `@SpringBootTest` de Spring Boot y `MockMvc`.
    * Simula solicitudes HTTP reales a los endpoints del controlador, probando la pila completa (controlador -> servicio -> repositorio -> DB H2).
    * Incluye pruebas para autenticación básica, errores de validación y comportamiento asíncrono.

**Para ejecutar todas las pruebas:**

```bash
mvn test
```

## 12. Posibles Mejoras

* **Manejo Robusto de Errores para Operaciones Asíncronas:** Implementar un mecanismo para notificar a los clientes sobre el estado final de un pedido procesado asíncronamente (por ejemplo, WebSockets, callbacks, un endpoint de verificación de estado separado).
* **Migraciones de Bases de Datos:** Utilizar Flyway o Liquibase para migraciones de esquemas de bases de datos con control de versiones en lugar de `ddl-auto=update`.
* **Autenticación Más Sofisticada:** Implementar JWT u OAuth2 para un mecanismo de autenticación más seguro y escalable.
* **Externalización de Configuración:** Externalizar configuraciones sensibles (por ejemplo, credenciales de la base de datos) utilizando Spring Cloud Config o variables de entorno.
* **Observabilidad:** Integrar con una solución de monitoreo más completa (por ejemplo, Prometheus/Grafana) y rastreo distribuido (por ejemplo, Zipkin/Jaeger) para microservicios.
* **Dockerización:** Contenerizar la aplicación utilizando Docker para facilitar la implementación y escalado.
* **Concurrencia Más Avanzada:** Explorar el uso de `CompletableFuture` para flujos de trabajo asíncronos más complejos o `RxJava` / Reactor para paradigmas de programación reactiva si el rendimiento extremadamente alto y la E/S no bloqueante se vuelven críticos.
* **Patrones de Circuit Breaker/Resiliencia:** Implementar patrones como Circuit Breaker (por ejemplo, con Resilience4j) para prevenir fallas en cascada en un entorno de microservicios.
