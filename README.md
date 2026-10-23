# Taller Backend 01 — Tu primera API REST

API REST con **Spring Boot 3.5.7**, **Java 21** y **Maven**. Datos almacenados en memoria (se pierden al reiniciar). Proyecto independiente del frontend.

## Requisitos
- Java JDK 21
- Internet en el primer arranque para descargar Maven y dependencias

## Ejecutar en Windows

```powershell
.\mvnw.cmd spring-boot:run
```

## Ejecutar en Linux/macOS

```bash
chmod +x mvnw
./mvnw spring-boot:run
```

## Pruebas

```powershell
.\mvnw.cmd clean test
```

En Linux/macOS: `./mvnw clean test`. Incluye siete pruebas con MockMvc, que cubren los cinco casos obligatorios.

## Endpoints

| Método | Ruta | Éxito | Error |
| --- | --- | --- | --- |
| GET | `/api/productos` | 200 + lista | — |
| GET | `/api/productos?categoria=libros` | 200 + lista filtrada | — |
| GET | `/api/productos/{id}` | 200 + producto | 404 |
| POST | `/api/productos` | 201 + Location | 400 |
| PUT | `/api/productos/{id}` | 200 + producto | 400 / 404 |
| DELETE | `/api/productos/{id}` | 204 sin cuerpo | 404 |
| GET | `/api/productos/estadisticas` | 200 + estadísticas | — |

## Ejemplos de uso

Listar productos:

```bash
curl -i http://localhost:8080/api/productos
```

Crear producto (Windows PowerShell):

```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/productos -ContentType 'application/json' -Body '{"titulo":"Libro nuevo","precio":25000,"categoria":"libros","existencias":4}'
```

La API asigna el ID automáticamente. `precio` usa `BigDecimal`. Se rechazan precios menores o iguales a cero, títulos o categorías vacíos y existencias negativas.

## Arquitectura

- `modelo`: registros `Producto` y `Estadisticas`.
- `repositorio`: `ConcurrentHashMap` y `AtomicLong`, con ocho productos iniciales de tres categorías.
- `servicio`: reglas de negocio, validación y estadísticas.
- `controlador`: endpoints HTTP y códigos de respuesta.

La inyección de dependencias es por constructor y campos `final`. La propiedad `catalogo.iva` está definida en `application.properties` y se lee con `@Value("${catalogo.iva:0.19}")` para incluirla en el resumen estadístico.

## Notas

- Puerto: `8080`.
- No se usa base de datos en este taller.
- El proyecto debe subirse a un **repositorio nuevo** de GitHub, no al repositorio del frontend.
- Los archivos `.mvn/`, `mvnw` y `mvnw.cmd` deben incluirse en GitHub.
