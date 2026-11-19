package org.example.tiendaapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.tiendaapp.model.Producto
import org.example.tiendaapp.network.ApiClient

data class ProductoListState(
    val productos: List<Producto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val actionMessage: String? = null
)

data class FormState(
    val id: Int? = null,
    val nombre: String = "",
    val descripcion: String = "",
    val precio: String = "",
    val stock: String = "",
    val activo: Boolean = true,
    val isSubmitting: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val nombreError: String? = null,
    val descripcionError: String? = null,
    val precioError: String? = null,
    val stockError: String? = null
) {
    val isEditing: Boolean get() = id != null
}

class ProductoViewModel : ViewModel() {

    private val _listState = MutableStateFlow(ProductoListState())
    val listState: StateFlow<ProductoListState> = _listState.asStateFlow()

    private val _formState = MutableStateFlow(FormState())
    val formState: StateFlow<FormState> = _formState.asStateFlow()

    init {
        cargarProductos()
    }

    fun cargarProductos() {
        viewModelScope.launch {
            _listState.value = _listState.value.copy(isLoading = true, error = null)
            try {
                val productos = ApiClient.getProductos()
                _listState.value = _listState.value.copy(
                    productos = productos,
                    isLoading = false
                )
            } catch (e: Exception) {
                _listState.value = _listState.value.copy(
                    isLoading = false,
                    error = "Error al cargar productos: ${e.message}"
                )
            }
        }
    }

    fun iniciarCreacion() {
        _formState.value = FormState()
    }

    fun iniciarEdicion(producto: Producto) {
        val precioStr = if (producto.precio % 1.0 == 0.0) {
            producto.precio.toLong().toString()
        } else {
            producto.precio.toString()
        }
        _formState.value = FormState(
            id = producto.id,
            nombre = producto.nombre,
            descripcion = producto.descripcion,
            precio = precioStr,
            stock = producto.stock.toString(),
            activo = producto.activo
        )
    }

    fun updateNombre(value: String) {
        _formState.value = _formState.value.copy(nombre = value, nombreError = null)
    }

    fun updateDescripcion(value: String) {
        _formState.value = _formState.value.copy(descripcion = value, descripcionError = null)
    }

    fun updatePrecio(value: String) {
        _formState.value = _formState.value.copy(precio = value, precioError = null)
    }

    fun updateStock(value: String) {
        _formState.value = _formState.value.copy(stock = value, stockError = null)
    }

    fun updateActivo(value: Boolean) {
        _formState.value = _formState.value.copy(activo = value)
    }

    private fun validarFormulario(): Boolean {
        val state = _formState.value
        var isValid = true

        if (state.nombre.isBlank()) {
            _formState.value = _formState.value.copy(nombreError = "El nombre es obligatorio")
            isValid = false
        }

        if (state.descripcion.isBlank()) {
            _formState.value = _formState.value.copy(descripcionError = "La descripción es obligatoria")
            isValid = false
        }

        val precio = state.precio.toDoubleOrNull()
        if (precio == null || precio <= 0) {
            _formState.value = _formState.value.copy(precioError = "Ingrese un precio válido mayor a 0")
            isValid = false
        }

        val stock = state.stock.toIntOrNull()
        if (stock == null || stock < 0) {
            _formState.value = _formState.value.copy(stockError = "Ingrese un stock válido (0 o más)")
            isValid = false
        }

        return isValid
    }

    fun enviarProducto(onSuccess: () -> Unit) {
        if (!validarFormulario()) return

        val state = _formState.value
        val producto = Producto(
            id = state.id ?: 0,
            nombre = state.nombre.trim(),
            descripcion = state.descripcion.trim(),
            precio = state.precio.toDouble(),
            stock = state.stock.toInt(),
            activo = state.activo
        )

        viewModelScope.launch {
            _formState.value = _formState.value.copy(
                isSubmitting = true,
                successMessage = null,
                errorMessage = null
            )
            val result = if (state.isEditing) {
                ApiClient.actualizarProducto(state.id!!, producto)
            } else {
                ApiClient.crearProducto(producto)
            }

            result.onSuccess {
                val mensaje = if (state.isEditing) {
                    "¡Producto '${it.nombre}' actualizado exitosamente!"
                } else {
                    "¡Producto '${it.nombre}' creado exitosamente!"
                }
                _formState.value = _formState.value.copy(
                    isSubmitting = false,
                    successMessage = mensaje
                )
                // Recargar listado en segundo plano
                cargarProductos()
                // Breve pausa para mostrar la respuesta visual de éxito
                delay(1200)
                resetForm()
                onSuccess()
            }.onFailure { e ->
                _formState.value = _formState.value.copy(
                    isSubmitting = false,
                    errorMessage = e.message ?: "Error al procesar la solicitud"
                )
            }
        }
    }

    fun eliminarProducto(id: Int) {
        viewModelScope.launch {
            _listState.value = _listState.value.copy(isLoading = true, actionMessage = null)
            val result = ApiClient.eliminarProducto(id)
            result.onSuccess {
                _listState.value = _listState.value.copy(
                    actionMessage = "Producto eliminado correctamente"
                )
                cargarProductos()
            }.onFailure { e ->
                _listState.value = _listState.value.copy(
                    isLoading = false,
                    error = "Error al eliminar producto: ${e.message}"
                )
            }
        }
    }

    fun clearActionMessage() {
        _listState.value = _listState.value.copy(actionMessage = null)
    }

    fun resetForm() {
        _formState.value = FormState()
    }
}
