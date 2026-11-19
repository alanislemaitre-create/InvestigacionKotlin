package org.example.tiendaapp

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import org.example.tiendaapp.ui.FormularioScreen
import org.example.tiendaapp.ui.ListadoScreen
import org.example.tiendaapp.viewmodel.ProductoViewModel

enum class Screen {
    LISTADO,
    FORMULARIO
}

@Composable
fun App() {
    MaterialTheme {
        val viewModel: ProductoViewModel = viewModel { ProductoViewModel() }
        var currentScreen by remember { mutableStateOf(Screen.LISTADO) }

        when (currentScreen) {
            Screen.LISTADO -> {
                ListadoScreen(
                    viewModel = viewModel,
                    onNavigateToCreate = {
                        viewModel.iniciarCreacion()
                        currentScreen = Screen.FORMULARIO
                    },
                    onNavigateToEdit = { producto ->
                        viewModel.iniciarEdicion(producto)
                        currentScreen = Screen.FORMULARIO
                    }
                )
            }
            Screen.FORMULARIO -> {
                FormularioScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        viewModel.resetForm()
                        currentScreen = Screen.LISTADO
                        viewModel.cargarProductos()
                    }
                )
            }
        }
    }
}
