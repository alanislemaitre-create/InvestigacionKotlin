package org.example.tiendaapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.example.tiendaapp.viewmodel.ProductoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioScreen(
    viewModel: ProductoViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.formState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(if (state.isEditing) "Editar Producto" else "Registrar Producto") 
                },
                navigationIcon = {
                    TextButton(onClick = {
                        viewModel.resetForm()
                        onNavigateBack()
                    }) {
                        Text("← Volver", color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Mensaje de éxito
            state.successMessage?.let { msg ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "✓ $msg",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Mensaje de error de la API
            state.errorMessage?.let { msg ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "✗ $msg",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Campo 1: Nombre
            OutlinedTextField(
                value = state.nombre,
                onValueChange = { viewModel.updateNombre(it) },
                label = { Text("Nombre del producto *") },
                isError = state.nombreError != null,
                supportingText = state.nombreError?.let { { Text(it) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isSubmitting
            )

            // Campo 2: Descripción
            OutlinedTextField(
                value = state.descripcion,
                onValueChange = { viewModel.updateDescripcion(it) },
                label = { Text("Descripción *") },
                isError = state.descripcionError != null,
                supportingText = state.descripcionError?.let { { Text(it) } },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isSubmitting
            )

            // Campo 3: Precio
            OutlinedTextField(
                value = state.precio,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                        viewModel.updatePrecio(newValue)
                    }
                },
                label = { Text("Precio (mayor a 0) *") },
                isError = state.precioError != null,
                supportingText = state.precioError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isSubmitting
            )

            // Campo 4: Stock
            OutlinedTextField(
                value = state.stock,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() || newValue.matches(Regex("^\\d+$"))) {
                        viewModel.updateStock(newValue)
                    }
                },
                label = { Text("Stock (0 o más) *") },
                isError = state.stockError != null,
                supportingText = state.stockError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isSubmitting
            )

            // Campo 5: Switch Activo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Producto activo",
                    style = MaterialTheme.typography.bodyLarge
                )
                Switch(
                    checked = state.activo,
                    onCheckedChange = { viewModel.updateActivo(it) },
                    enabled = !state.isSubmitting
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Botón Guardar / Actualizar
            Button(
                onClick = { viewModel.enviarProducto(onSuccess = onNavigateBack) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !state.isSubmitting,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(if (state.isEditing) "Actualizando..." else "Guardando...")
                } else {
                    Text(
                        if (state.isEditing) "Actualizar Producto" else "Guardar Producto", 
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}
