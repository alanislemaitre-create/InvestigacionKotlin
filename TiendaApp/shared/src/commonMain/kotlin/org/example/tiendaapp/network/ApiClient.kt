package org.example.tiendaapp.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.example.tiendaapp.model.Producto

object ApiClient {
    private const val BASE_URL = "http://127.0.0.1:8000"

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    suspend fun getProductos(): List<Producto> {
        val response: HttpResponse = client.get("$BASE_URL/api/productos/")
        return response.body()
    }

    suspend fun crearProducto(producto: Producto): Result<Producto> {
        return try {
            val response: HttpResponse = client.post("$BASE_URL/api/productos/") {
                contentType(ContentType.Application.Json)
                setBody(producto)
            }
            if (response.status == HttpStatusCode.Created) {
                Result.success(response.body())
            } else {
                val errorBody = response.bodyAsText()
                Result.failure(Exception("Error ${response.status.value}: $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun actualizarProducto(id: Int, producto: Producto): Result<Producto> {
        return try {
            val response: HttpResponse = client.put("$BASE_URL/api/productos/$id/") {
                contentType(ContentType.Application.Json)
                setBody(producto)
            }
            if (response.status == HttpStatusCode.OK) {
                Result.success(response.body())
            } else {
                val errorBody = response.bodyAsText()
                Result.failure(Exception("Error ${response.status.value}: $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun eliminarProducto(id: Int): Result<Unit> {
        return try {
            val response: HttpResponse = client.delete("$BASE_URL/api/productos/$id/")
            if (response.status == HttpStatusCode.NoContent || response.status == HttpStatusCode.OK) {
                Result.success(Unit)
            } else {
                val errorBody = response.bodyAsText()
                Result.failure(Exception("Error ${response.status.value}: $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
