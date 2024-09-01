package com.example.gremlinsv1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GremlinsApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GremlinsApp() {
    var selectedTab by remember { mutableStateOf(0) } // 0: Productos, 1: Envíos, 2: Clientes, 3: Proveedores

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Gremlins App", color = Color.White) },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = Color(0xFF00796B)
                ),
                actions = {
                    Switch(
                        checked = false,
                        onCheckedChange = {},
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            )
        },
        bottomBar = {
            BottomNavigation(selectedTab) { selectedTab = it }
        }
    ) { innerPadding ->
        when (selectedTab) {
            0 -> ProductosView(innerPadding)
            1 -> EnviosView(innerPadding)
            2 -> ClientesView(innerPadding)
            3 -> ProveedoresView(innerPadding)
        }
    }
}

@Composable
fun BottomNavigation(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    NavigationBar(containerColor = Color.White) {
        val items = listOf(
            Triple("Productos", Icons.Default.Inventory, 0),
            Triple("Envíos", Icons.Default.LocalShipping, 1),
            Triple("Clientes", Icons.Default.People, 2),
            Triple("Proveedores", Icons.Default.Business, 3)
        )
        items.forEach { (label, icon, index) ->
            NavigationBarItem(
                icon = {
                    Box(
                        modifier = Modifier
                            .background(
                                if (selectedTab == index) Color(0xFFFFA500) else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(4.dp)
                    ) {
                        Icon(
                            icon,
                            contentDescription = label,
                            tint = if (selectedTab == index) Color(0xFFFFA500) else Color.Gray
                        )
                    }
                },
                label = {
                    Text(
                        label,
                        color = if (selectedTab == index) Color(0xFFFFA500) else Color.Gray
                    )
                },
                selected = selectedTab == index,
                onClick = { onTabSelected(index) }
            )
        }
    }
}

@Composable
fun GenericView(
    innerPadding: PaddingValues,
    title: String,
    items: List<String>,
    icon: ImageVector,
    searchPlaceholder: String,
    showCreateButton: Boolean = false,
    createButtonText: String = ""
) {
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .padding(innerPadding)
            .padding(16.dp)
    ) {
        SearchBar(searchQuery, onSearchQueryChange = { searchQuery = it }, searchPlaceholder)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                title,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF00796B)
            )
            if (showCreateButton) {
                Button(
                    onClick = { /* TODO: Implement creation functionality */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722))
                ) {
                    Text(createButtonText)
                }
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(items) { item ->
                ListItem(item, icon)
            }
        }
    }
}

@Composable
fun ProductosView(innerPadding: PaddingValues) {
    val productos = List(9) { "Producto #${it + 1}" }
    GenericView(
        innerPadding = innerPadding,
        title = "Productos",
        items = productos,
        icon = Icons.Default.Inventory,
        searchPlaceholder = "Buscar productos",
        showCreateButton = true,
        createButtonText = "Crear nuevo producto"
    )
}

@Composable
fun EnviosView(innerPadding: PaddingValues) {
    val envios = List(9) { "Envío #${it + 1}" }
    GenericView(
        innerPadding = innerPadding,
        title = "Envíos",
        items = envios,
        icon = Icons.Default.LocalShipping,
        searchPlaceholder = "Buscar envíos",
        showCreateButton = true,
        createButtonText = "Crear nuevo envío"
    )
}

@Composable
fun ClientesView(innerPadding: PaddingValues) {
    val clientes = List(9) { "Cliente #${it + 1}" }
    GenericView(
        innerPadding = innerPadding,
        title = "Clientes",
        items = clientes,
        icon = Icons.Default.Person,
        searchPlaceholder = "Buscar clientes",
        showCreateButton = true,
        createButtonText = "Crear nuevo cliente"
    )
}

@Composable
fun ProveedoresView(innerPadding: PaddingValues) {
    val proveedores = List(9) { "Proveedor #${it + 1}" }
    GenericView(
        innerPadding = innerPadding,
        title = "Proveedores",
        items = proveedores,
        icon = Icons.Default.Business,
        searchPlaceholder = "Buscar proveedores",
        showCreateButton = true,
        createButtonText = "Crear nuevo proveedor"
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(value: String, onSearchQueryChange: (String) -> Unit, placeholder: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onSearchQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        shape = MaterialTheme.shapes.medium,
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = Color(0xFF00796B),
            unfocusedBorderColor = Color.Gray
        )
    )
}

@Composable
fun ListItem(text: String, icon: ImageVector) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = Color(0xFF00796B)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(text, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Ver detalles",
                modifier = Modifier.size(24.dp),
                tint = Color.Gray
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MaterialTheme {
        GremlinsApp()
    }
}