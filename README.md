# 🍔 PedidosYa - API REST de Gestión de Pedidos y Delivery

API REST para un sistema integral de comercio electrónico y logística de pedidos a domicilio estilo **PedidosYa**, desarrollado con **Java 21**, **Spring Boot**, **Spring Security**, **JWT (JSON Web Tokens)**, **Spring Data JPA**, **Hibernate** y **MySQL**.

---

## 📋 Tabla de Contenidos
1. [Descripción General](#-descripción-general)
2. [Arquitectura y Tecnologías](#-arquitectura-y-tecnologías)
3. [Modelo de Datos y Entidades](#-modelo-de-datos-y-entidades)
4. [Reglas de Negocio Clave](#-reglas-de-negocio-clave)
5. [Requisitos Previos](#-requisitos-previos)
6. [Configuración de Base de Datos](#-configuración-de-base-de-datos)
7. [Paso a Paso: Cómo Ejecutar la Aplicación](#-paso-a-paso-cómo-ejecutar-la-aplicación)
8. [Usuarios y Credenciales por Defecto (DataSeeder)](#-usuarios-y-credenciales-por-defecto-dataseeder)
9. [Guía Paso a Paso: Cómo Entrar y Usar la API](#-guía-paso-a-paso-cómo-entrar-y-usar-la-api)
10. [Catálogo Completo de Endpoints](#-catálogo-completo-de-endpoints)
11. [Manejo de Errores y Excepciones](#-manejo-de-errores-y-excepciones)

---

## 📖 Descripción General

La aplicación modela el ecosistema de entrega de pedidos en línea, gestionando la interacción entre tres actores clave:
* **Clientes:** Consultan catálogos de comercios (restaurantes, supermercados, farmacias), seleccionan productos, configuran pedidos y realizan el seguimiento del estado de sus órdenes.
* **Repartidores:** Visualizan pedidos pendientes de entrega en tiempo real, toman pedidos disponibles y actualizan los estados logísticos (`EN_CAMINO`, `ENTREGADO`).
* **Administradores:** Gestionan comercios, productos, inventarios, monitorean todos los pedidos de la plataforma y consultan la base de usuarios.

---

## 🛠️ Arquitectura y Tecnologías

El proyecto sigue una arquitectura en capas desacoplada orientada a servicios:

* **Lenguaje:** Java 21 LTS
* **Framework Principal:** Spring Boot 4.x / Spring Framework 7
* **Seguridad:** Spring Security 7 con arquitectura Stateless y cifrado de contraseñas mediante **BCrypt (Factor de costo 12 - Estándar OWASP)**
* **Autenticación:** JSON Web Tokens (JJWT 0.12.6) con clave secreta protegida y cifrada mediante **BCrypt** y algoritmo de firma digital **HMAC-SHA256**
* **Persistencia:** Spring Data JPA con Hibernate ORM
* **Base de Datos:** MySQL Server 8.0+
* **Concurrencia:** Bloqueo Pesimista de Escritura (`PESSIMISTIC_WRITE`) en transacciones de stock
* **Validaciones:** Hibernate Validator / Jakarta Bean Validation (`@NotNull`, `@NotBlank`, `@DecimalMin`, etc.)
* **Documentación y Calidad:** JavaDoc exhaustivo, pruebas unitarias de criptografía y manejo global de excepciones con `@RestControllerAdvice`

---

## 🗄️ Modelo de Datos y Entidades

```
+------------------+         1:N         +------------------+
|     Comercio     |-------------------->|     Producto     |
|------------------|                     |------------------|
| id (PK)          |                     | id (PK)          |
| nombre           |                     | comercio_id (FK) |
| categoria        |                     | nombre           |
| direccion        |                     | precio           |
| abierto          |                     | stock            |
+------------------+                     | disponible       |
                                         +------------------+
                                                   | 1
                                                   |
                                                   | N
+------------------+         1:N         +------------------+
|      Pedido      |-------------------->|  DetallePedido   |
|------------------|                     |------------------|
| id (PK)          |                     | id (PK)          |
| cliente_id (FK)  |                     | pedido_id (FK)   |
| repartidor_id(FK)|                     | producto_id (FK) |
| fechaPedido      |                     | cantidad         |
| costoEnvio       |                     | precioUnitario   |
| montoTotal       |                     | subtotal         |
| estado           |                     +------------------+
+------------------+
         |
         | N:1
+------------------+
|     Usuario      |
|------------------|
| id (PK)          |
| nombre           |
| direccion        |
| telefono         |
| email (Unique)   |
| password (BCrypt)|
| rol (Enum)       |
+------------------+
```

### Enumeraciones
* **Rol:** `ADMIN`, `REPARTIDOR`, `CLIENTE`
* **CategoriaComercio:** `RESTAURANTE`, `SUPERMERCADO`, `FARMACIA`
* **EstadoPedido:** `PENDIENTE`, `EN_PREPARACION`, `EN_CAMINO`, `ENTREGADO`, `CANCELADO`

---

## ⚡ Reglas de Negocio Clave

1. **Bloqueo Pesimista Concurrente:** Al confirmar un pedido, los productos seleccionados se bloquean a nivel de base de datos (`SELECT ... FOR UPDATE`), garantizando que dos clientes concurrentes no compren más stock del disponible.
2. **Homogeneidad de Comercio:** Todos los productos dentro de un mismo pedido deben pertenecer al mismo establecimiento comercial.
3. **Disponibilidad y Apertura:** No se pueden ordenar productos de un comercio cerrado ni productos marcados como no disponibles.
4. **Máquina de Estados Estricta:**
   * `PENDIENTE` ➔ `EN_PREPARACION` o `CANCELADO`
   * `EN_PREPARACION` ➔ `EN_CAMINO` o `CANCELADO`
   * `EN_CAMINO` ➔ `ENTREGADO` o `CANCELADO`
   * Los estados `ENTREGADO` y `CANCELADO` son terminales e inmutables.
5. **Restitución Automática de Inventario:** Si un pedido pasa al estado `CANCELADO`, el sistema reintegra automáticamente las cantidades reservadas al stock de cada producto.

---

## 💻 Requisitos Previos

Asegúrate de contar con lo siguiente instalado en tu entorno:
* **Java Development Kit (JDK):** Versión 21 o superior (`java -version`).
* **MySQL Server:** Versión 8.0 o superior en ejecución en el puerto `3306`.
* **Herramienta de pruebas de API:** Postman, Insomnia, Thunder Client (extensión de VS Code) o `curl`.

---

## ⚙️ Configuración de Base de Datos

Verifica o ajusta el archivo `src/main/resources/application.properties`:

```properties
spring.application.name=pedidosya
server.port=8080

# Configuracion de Conexion MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/dbPedidosya_in5am?createDatabaseIfNotExist=true
spring.datasource.username=IN5AM
spring.datasource.password=_odmon5Am

# Hibernate y JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.open-in-view=false
spring.jpa.show-sql=true

# JSON Web Token (JWT) con Clave Secreta Cifrada mediante BCrypt (Rounds: 12)
jwt.secret=$2a$12$e8kqX9J1Z1qK8Q7b1m4o5u0WcvOba81EeXqE9EUB7bdTi8S2gzcj6
jwt.expiration=86400000
```

> **Nota:** La propiedad `createDatabaseIfNotExist=true` creará de manera automática el esquema `dbPedidosya_in5am` si no existe previamente en MySQL.

---

### 🛡️ Esquema Profesional de Seguridad y Criptografía con BCrypt

1. **Cifrado de Contraseñas de Usuarios (`BCryptPasswordEncoder`):**
   * Configurado con factor de costo `12` (4096 iteraciones de hashing criptográfico).
   * Protección contra ataques de fuerza bruta y diccionarios con sal aleatoria única por usuario.
   * Auto-encriptación garantizada en el registro (`AuthService`) y en la persistencia de usuarios (`UsuarioService`).

2. **Cifrado y Protección de la Clave Secreta JWT (`JWT Secret`):**
   * El secreto configurado en `application.properties` se almacena como un hash criptográfico generado mediante el algoritmo **BCrypt** de 60 caracteres (`$2a$12$...`).
   * `JwtService` procesa y valida la clave bajo el algoritmo BCrypt antes de generar la clave binaria para la firma digital **HMAC-SHA256**.
   * Evita la exposición de claves en texto plano y fortalece la entropía de firma de los tokens.

---

## 🚀 Paso a Paso: Cómo Ejecutar la Aplicación

### Opción 1: Desde la Terminal (Recomendado)
Abre una terminal PowerShell o CMD en la raíz del proyecto y ejecuta:

```powershell
./mvnw spring-boot:run
```

### Opción 2: Compilación y Ejecución del JAR
```powershell
./mvnw clean package -DskipTests
java -jar target/pedidosya-0.0.1-SNAPSHOT.jar
```

### Opción 3: Desde un IDE (IntelliJ IDEA / VS Code / Eclipse)
1. Abre el proyecto seleccionando la carpeta raíz.
2. Espera a que Maven sincronice las dependencias del `pom.xml`.
3. Navega a `src/main/java/com/andersonmorales/pedidosya/PedidosyaApplication.java`.
4. Haz clic derecho y selecciona **Run 'PedidosyaApplication'**.

Al arrancar, verás en consola:
```
Tomcat started on port 8080 (http) with context path '/'
Started PedidosyaApplication in X.XXX seconds
```

---

## 👥 Usuarios y Credenciales por Defecto (DataSeeder)

El componente `DataSeeder` inserta automáticamente usuarios, comercios y productos de prueba la primera vez que se inicia la aplicación:

| Rol | Correo Electrónico | Contraseña | Propósito |
| :--- | :--- | :--- | :--- |
| **ADMIN** | `admin@pedidosya.com` | `admin1234` | Administrar comercios, productos y ver todos los pedidos |
| **REPARTIDOR** | `repartidor@pedidosya.com` | `repartidor1234` | Ver pedidos disponibles, asignárselos y cambiar estados |
| **CLIENTE** | `cliente@pedidosya.com` | `cliente1234` | Consultar catálogos, crear pedidos y ver historial |

---

## 🔐 Guía Paso a Paso: Cómo Entrar y Usar la API

Sigue este flujo secuencial en Postman o Thunder Client:

### Paso 1: Autenticación (Login)
Realiza una petición `POST` para obtener tu token de acceso JWT.

* **URL:** `http://localhost:8080/api/auth/login`
* **Método:** `POST`
* **Headers:** `Content-Type: application/json`
* **Body (Raw JSON):**
```json
{
  "email": "cliente@pedidosya.com",
  "password": "cliente1234"
}
```

* **Respuesta Exitosa (HTTP 200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJyb2wiOiJDTElFTlRFIiwibm9tYnJlIjoiQW5kZXJzb24gTW9yYWxlcyIsInVzZXJJZCI6Mywic3ViIjoiY2xpZW50ZUBwZWRpZG9zeWEuY29tIiwiaWF0IjoxNzg0ODk3NjAwLCJleHAiOjE3ODQ5ODQwMDB9...",
  "tipo": "Bearer",
  "email": "cliente@pedidosya.com",
  "rol": "CLIENTE"
}
```

---

### Paso 2: Configurar el Token en tus Peticiones
Copia el valor del campo `"token"` de la respuesta.
En cada petición protegida posterior, añade la cabecera HTTP:

```http
Authorization: Bearer <TU_TOKEN_JWT_AQUI>
```

*(En Postman: pestaña **Authorization** ➔ Type: **Bearer Token** ➔ Pega el token).*

---

### Paso 3: Explorar Comercios y Catálogos (Público)
* **Ver Comercios Abiertos:**
  * `GET http://localhost:8080/api/comercios`
  * O filtrar por categoría: `GET http://localhost:8080/api/comercios?categoria=RESTAURANTE`

* **Ver Productos de un Comercio (ej. Comercio ID 1):**
  * `GET http://localhost:8080/api/comercios/1/productos`

---

### Paso 4: Crear un Pedido (Como CLIENTE)
Con el token del **CLIENTE** en la cabecera `Authorization`:

* **URL:** `http://localhost:8080/api/pedidos`
* **Método:** `POST`
* **Headers:**
  * `Authorization: Bearer <TOKEN_CLIENTE>`
  * `Content-Type: application/json`
* **Body:**
```json
{
  "items": [
    {
      "productoId": 1,
      "cantidad": 2
    },
    {
      "productoId": 2,
      "cantidad": 1
    }
  ]
}
```
* **Respuesta (HTTP 201 Created):**
```json
{
  "id": 1,
  "cliente": {
    "id": 3,
    "nombre": "Anderson Morales",
    "direccion": "Colonia Las Flores, Lote 12",
    "telefono": "55443322",
    "email": "cliente@pedidosya.com",
    "rol": "CLIENTE"
  },
  "repartidor": null,
  "fechaPedido": "2026-10-06T11:30:00",
  "costoEnvio": 15.00,
  "montoTotal": 143.00,
  "estado": "PENDIENTE",
  "detalles": [
    {
      "id": 1,
      "productoId": 1,
      "producto": "Hamburguesa Doble Carne con Queso",
      "cantidad": 2,
      "precioUnitario": 55.00,
      "subtotal": 110.00
    },
    {
      "id": 2,
      "productoId": 2,
      "producto": "Papas Fritas Medianas",
      "cantidad": 1,
      "precioUnitario": 18.00,
      "subtotal": 18.00
    }
  ]
}
```

---

### Paso 5: El Repartidor Toma el Pedido
1. Haz login con el repartidor (`repartidor@pedidosya.com` / `repartidor1234`) y copia su token.
2. Consulta los pedidos disponibles:
   * `GET http://localhost:8080/api/pedidos/disponibles`
   * `Authorization: Bearer <TOKEN_REPARTIDOR>`
3. Asignarse el pedido #1:
   * `POST http://localhost:8080/api/pedidos/1/tomar`
   * `Authorization: Bearer <TOKEN_REPARTIDOR>`
   * *El pedido se asigna y cambia automáticamente a estado `EN_PREPARACION`.*

---

### Paso 6: Actualizar Estado del Pedido
Con el token del repartidor:
* **URL:** `http://localhost:8080/api/pedidos/1/estado`
* **Método:** `PATCH`
* **Headers:** `Authorization: Bearer <TOKEN_REPARTIDOR>`
* **Body:**
```json
{
  "estado": "EN_CAMINO"
}
```
Posteriormente, al entregarlo:
```json
{
  "estado": "ENTREGADO"
}
```

---

## 📡 Catálogo Completo de Endpoints

### 1. Autenticación (`/api/auth`)
| Método | Endpoint | Rol Requerido | Descripción |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/register` | Público | Registra una nueva cuenta de cliente y devuelve su JWT |
| `POST` | `/api/auth/login` | Público | Inicia sesión con correo y contraseña |

### 2. Comercios (`/api/comercios`)
| Método | Endpoint | Rol Requerido | Descripción |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/comercios` | Público | Lista comercios abiertos (filtro opcional `?categoria=`) |
| `GET` | `/api/comercios/todos` | `ADMIN` | Lista todos los comercios (abiertos y cerrados) |
| `GET` | `/api/comercios/{id}` | Público | Consulta detalle de un comercio por ID |
| `POST` | `/api/comercios` | `ADMIN` | Crea un nuevo establecimiento comercial |
| `PUT` | `/api/comercios/{id}` | `ADMIN` | Modifica datos de un comercio |
| `PATCH` | `/api/comercios/{id}/abierto?abierto=false` | `ADMIN` | Abre o cierra un comercio |
| `DELETE` | `/api/comercios/{id}` | `ADMIN` | Elimina un comercio |

### 3. Productos (`/api`)
| Método | Endpoint | Rol Requerido | Descripción |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/comercios/{comercioId}/productos` | Público | Catálogo de productos de un comercio |
| `POST` | `/api/comercios/{comercioId}/productos` | `ADMIN` | Registra un producto para un comercio |
| `GET` | `/api/productos/{id}` | Público | Consulta detalle de un producto por ID |
| `PUT` | `/api/productos/{id}` | `ADMIN` | Actualiza precio, stock o nombre de producto |
| `PATCH` | `/api/productos/{id}/disponibilidad?disponible=true` | `ADMIN` | Habilita o pausa la venta de un producto |
| `DELETE` | `/api/productos/{id}` | `ADMIN` | Elimina un producto del catálogo |

### 4. Pedidos (`/api/pedidos`)
| Método | Endpoint | Rol Requerido | Descripción |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/pedidos` | `CLIENTE`, `ADMIN` | Crea un nuevo pedido con validación de stock y envío |
| `GET` | `/api/pedidos/{id}` | Autenticado | Consulta detalle completo del pedido |
| `GET` | `/api/pedidos/mis-pedidos` | `CLIENTE`, `ADMIN` | Historial de pedidos del cliente autenticado |
| `GET` | `/api/pedidos/disponibles` | `REPARTIDOR`, `ADMIN` | Cola de pedidos listos para tomar por repartidores |
| `POST` | `/api/pedidos/{id}/tomar` | `REPARTIDOR`, `ADMIN` | Repartidor se asigna un pedido disponible |
| `PATCH` | `/api/pedidos/{id}/estado` | `REPARTIDOR`, `ADMIN` | Actualiza estado (`EN_CAMINO`, `ENTREGADO`, `CANCELADO`) |
| `GET` | `/api/pedidos` | `ADMIN` | Lista global de todos los pedidos del sistema |
| `GET` | `/api/pedidos/mis-entregas` | `REPARTIDOR`, `ADMIN` | Lista entregas asignadas al repartidor |

### 5. Usuarios (`/api/usuarios`)
| Método | Endpoint | Rol Requerido | Descripción |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/usuarios/perfil` | Autenticado | Retorna datos del usuario en sesión |
| `GET` | `/api/usuarios` | `ADMIN` | Directorio de todos los usuarios registrados |
| `GET` | `/api/usuarios/{id}` | `ADMIN` | Consulta información de un usuario específico |

---

## ⚠️ Manejo de Errores y Excepciones

Las respuestas ante errores son interceptadas por `GlobalExceptionHandler` y devuelven una estructura consistente:

```json
{
  "timestamp": "2026-10-06T11:45:12.345",
  "status": 409,
  "error": "Conflict",
  "message": "Stock insuficiente para el producto: Hamburguesa Doble Carne con Queso. Disponible: 2, Solicitado: 5",
  "path": "/api/pedidos"
}
```

En caso de errores de validación de campos (`400 Bad Request`), incluye el desglose:
```json
{
  "timestamp": "2026-10-06T11:45:30.120",
  "status": 400,
  "error": "Bad Request",
  "message": "Error en la validación de los campos enviados",
  "path": "/api/auth/register",
  "validationErrors": {
    "email": "debe ser una dirección de correo electrónico con formato correcto",
    "password": "el tamaño debe estar entre 8 y 72"
  }
}
```

---

## 👨‍💻 Autor
* **Anderson Morales** - Proyecto Académico de API REST de Pedidos y Delivery.

