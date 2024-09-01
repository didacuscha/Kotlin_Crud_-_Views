package com.example.gremlinsv1

import android.annotation.SuppressLint
import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

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

//Overrode GremlinsApp()?
class GremlinsApplication : Application() {
    lateinit var databaseHelper: DatabaseHelper

    override fun onCreate() {
        super.onCreate()
        databaseHelper = DatabaseHelper(this)
    }
}

// Navigation Setup

@Composable
fun GremlinsApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "main") {
        composable("main") { MainScreen(navController) }
        composable("create_producto") { CreateProductoView { navController.popBackStack() } }
        composable("create_envio") { CreateEnvioView { navController.popBackStack() } }
        composable("create_cliente") { CreateClienteView { navController.popBackStack() } }
        composable("create_proveedor") { CreateProveedorView { navController.popBackStack() } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController) {
    var selectedTab by remember { mutableStateOf(0) }

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
            0 -> ProductosView(innerPadding, onCreateNew = { navController.navigate("create_producto") })
            1 -> EnviosView(innerPadding, onCreateNew = { navController.navigate("create_envio") })
            2 -> ClientesView(innerPadding, onCreateNew = { navController.navigate("create_cliente") })
            3 -> ProveedoresView(innerPadding, onCreateNew = { navController.navigate("create_proveedor") })
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
fun ProductosView(innerPadding: PaddingValues, onCreateNew: () -> Unit) {
    val context = LocalContext.current
    val databaseHelper = (context.applicationContext as GremlinsApplication).databaseHelper
    var productos by remember { mutableStateOf(listOf<Producto>()) }

    LaunchedEffect(key1 = true) {
        productos = databaseHelper.getAllProductos()
    }

    Column(modifier = Modifier.padding(innerPadding)) {
        Button(onClick = onCreateNew, modifier = Modifier.align(Alignment.End).padding(16.dp)) {
            Text("Crear nuevo producto")
        }
        LazyColumn {
            items(productos) { producto ->
                ProductoItem(producto)
            }
        }
    }
}

@Composable
fun EnviosView(innerPadding: PaddingValues, onCreateNew: () -> Unit) {
    val context = LocalContext.current
    val databaseHelper = (context.applicationContext as GremlinsApplication).databaseHelper
    var envios by remember { mutableStateOf(listOf<Envio>()) }

    LaunchedEffect(key1 = true) {
        envios = databaseHelper.getAllEnvios()
    }

    Column(modifier = Modifier.padding(innerPadding)) {
        Button(onClick = onCreateNew, modifier = Modifier.align(Alignment.End).padding(16.dp)) {
            Text("Crear nuevo envío")
        }
        LazyColumn {
            items(envios) { envio ->
                EnvioItem(envio)
            }
        }
    }
}

@Composable
fun ClientesView(innerPadding: PaddingValues, onCreateNew: () -> Unit) {
    val context = LocalContext.current
    val databaseHelper = (context.applicationContext as GremlinsApplication).databaseHelper
    var clientes by remember { mutableStateOf(listOf<Cliente>()) }

    LaunchedEffect(key1 = true) {
        clientes = databaseHelper.getAllClientes()
    }

    Column(modifier = Modifier.padding(innerPadding)) {
        Button(onClick = onCreateNew, modifier = Modifier.align(Alignment.End).padding(16.dp)) {
            Text("Crear nuevo cliente")
        }
        LazyColumn {
            items(clientes) { cliente ->
                ClienteItem(cliente)
            }
        }
    }
}

@Composable
fun ProveedoresView(innerPadding: PaddingValues, onCreateNew: () -> Unit) {
    val context = LocalContext.current
    val databaseHelper = (context.applicationContext as GremlinsApplication).databaseHelper
    var proveedores by remember { mutableStateOf(listOf<Proveedor>()) }

    LaunchedEffect(key1 = true) {
        proveedores = databaseHelper.getAllProveedores()
    }

    Column(modifier = Modifier.padding(innerPadding)) {
        Button(onClick = onCreateNew, modifier = Modifier.align(Alignment.End).padding(16.dp)) {
            Text("Crear nuevo proveedor")
        }
        LazyColumn {
            items(proveedores) { proveedor ->
                ProveedorItem(proveedor)
            }
        }
    }
}

@Composable
fun ProductoItem(producto: Producto) {
    ListItem(
        text = producto.nombre,
        icon = Icons.Default.Inventory,
        secondaryText = "Precio: ${producto.precio}, Proveedor: ${producto.proveedor}"
    )
}

@Composable
fun EnvioItem(envio: Envio) {
    ListItem(
        text = "Envío #${envio.id}",
        icon = Icons.Default.LocalShipping,
        secondaryText = "Fecha: ${envio.fechaEnvio}, Producto: ${envio.producto}, Cliente: ${envio.cliente}, Estado: ${envio.estado}"
    )
}

@Composable
fun ClienteItem(cliente: Cliente) {
    ListItem(
        text = cliente.nombre,
        icon = Icons.Default.Person,
        secondaryText = "Teléfono: ${cliente.telefono}, Email: ${cliente.email}"
    )
}

@Composable
fun ProveedorItem(proveedor: Proveedor) {
    ListItem(
        text = proveedor.nombre,
        icon = Icons.Default.Business,
        secondaryText = "Teléfono: ${proveedor.telefono}, Email: ${proveedor.email}"
    )
}

@Composable
fun ListItem(text: String, icon: ImageVector, secondaryText: String) {
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
            Column {
                Text(text, fontWeight = FontWeight.Medium)
                Text(secondaryText, style = MaterialTheme.typography.bodySmall)
            }
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

// Database helper

data class Producto(val id: Int, val nombre: String, val precio: Double, val proveedor: String)
data class Envio(val id: Int, val fechaEnvio: String, val producto: String, val cliente: String, val estado: String)
data class Cliente(val id: Int, val nombre: String, val numero: String, val telefono: String, val direccion: String, val email: String)
data class Proveedor(val id: Int, val nombre: String, val numero: String, val telefono: String, val direccion: String, val email: String)

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "GremlinsApp.db"

        // Table names
        private const val TABLE_PRODUCTOS = "productos"
        private const val TABLE_ENVIOS = "envios"
        private const val TABLE_CLIENTES = "clientes"
        private const val TABLE_PROVEEDORES = "proveedores"

        // Common column names
        private const val KEY_ID = "id"
        private const val KEY_NOMBRE = "nombre"

        // Productos table columns
        private const val KEY_PRECIO = "precio"
        private const val KEY_PROVEEDOR = "proveedor"

        // Envios table columns
        private const val KEY_FECHA_ENVIO = "fecha_envio"
        private const val KEY_PRODUCTO = "producto"
        private const val KEY_CLIENTE = "cliente"
        private const val KEY_ESTADO = "estado"

        // Clientes and Proveedores table columns
        private const val KEY_NUMERO = "numero"
        private const val KEY_TELEFONO = "telefono"
        private const val KEY_DIRECCION = "direccion"
        private const val KEY_EMAIL = "email"
    }


    override fun onCreate(db: SQLiteDatabase) {
        // Create Productos table
        val CREATE_PRODUCTOS_TABLE = ("CREATE TABLE " + TABLE_PRODUCTOS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NOMBRE + " TEXT,"
                + KEY_PRECIO + " REAL," + KEY_PROVEEDOR + " TEXT" + ")")
        db.execSQL(CREATE_PRODUCTOS_TABLE)

        // Create Envios table
        val CREATE_ENVIOS_TABLE = ("CREATE TABLE " + TABLE_ENVIOS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_FECHA_ENVIO + " TEXT,"
                + KEY_PRODUCTO + " TEXT," + KEY_CLIENTE + " TEXT,"
                + KEY_ESTADO + " TEXT" + ")")
        db.execSQL(CREATE_ENVIOS_TABLE)

        // Create Clientes table
        val CREATE_CLIENTES_TABLE = ("CREATE TABLE " + TABLE_CLIENTES + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NOMBRE + " TEXT,"
                + KEY_NUMERO + " TEXT," + KEY_TELEFONO + " TEXT,"
                + KEY_DIRECCION + " TEXT," + KEY_EMAIL + " TEXT" + ")")
        db.execSQL(CREATE_CLIENTES_TABLE)

        // Create Proveedores table
        val CREATE_PROVEEDORES_TABLE = ("CREATE TABLE " + TABLE_PROVEEDORES + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NOMBRE + " TEXT,"
                + KEY_NUMERO + " TEXT," + KEY_TELEFONO + " TEXT,"
                + KEY_DIRECCION + " TEXT," + KEY_EMAIL + " TEXT" + ")")
        db.execSQL(CREATE_PROVEEDORES_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Drop older tables if existed
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PRODUCTOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ENVIOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CLIENTES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PROVEEDORES")

        // Create tables again
        onCreate(db)
    }

    // CRUD Operations

    // Add new producto
    fun addProducto(nombre: String, precio: Double, proveedor: String): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(KEY_NOMBRE, nombre)
        values.put(KEY_PRECIO, precio)
        values.put(KEY_PROVEEDOR, proveedor)
        return db.insert(TABLE_PRODUCTOS, null, values)
    }

    // Add new envio
    fun addEnvio(fechaEnvio: String, producto: String, cliente: String, estado: String): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(KEY_FECHA_ENVIO, fechaEnvio)
        values.put(KEY_PRODUCTO, producto)
        values.put(KEY_CLIENTE, cliente)
        values.put(KEY_ESTADO, estado)
        return db.insert(TABLE_ENVIOS, null, values)
    }

    // Add new cliente
    fun addCliente(nombre: String, numero: String, telefono: String, direccion: String, email: String): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(KEY_NOMBRE, nombre)
        values.put(KEY_NUMERO, numero)
        values.put(KEY_TELEFONO, telefono)
        values.put(KEY_DIRECCION, direccion)
        values.put(KEY_EMAIL, email)
        return db.insert(TABLE_CLIENTES, null, values)
    }

    // Add new proveedor
    fun addProveedor(nombre: String, numero: String, telefono: String, direccion: String, email: String): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(KEY_NOMBRE, nombre)
        values.put(KEY_NUMERO, numero)
        values.put(KEY_TELEFONO, telefono)
        values.put(KEY_DIRECCION, direccion)
        values.put(KEY_EMAIL, email)
        return db.insert(TABLE_PROVEEDORES, null, values)
    }

    @SuppressLint("Range")
    fun getAllProductos(): List<Producto> {
        val productos = mutableListOf<Producto>()
        val selectQuery = "SELECT * FROM $TABLE_PRODUCTOS"
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndex(KEY_ID))
                val nombre = cursor.getString(cursor.getColumnIndex(KEY_NOMBRE))
                val precio = cursor.getDouble(cursor.getColumnIndex(KEY_PRECIO))
                val proveedor = cursor.getString(cursor.getColumnIndex(KEY_PROVEEDOR))
                productos.add(Producto(id, nombre, precio, proveedor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return productos
    }

    @SuppressLint("Range")
    fun getAllEnvios(): List<Envio> {
        val envios = mutableListOf<Envio>()
        val selectQuery = "SELECT * FROM $TABLE_ENVIOS"
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndex(KEY_ID))
                val fechaEnvio = cursor.getString(cursor.getColumnIndex(KEY_FECHA_ENVIO))
                val producto = cursor.getString(cursor.getColumnIndex(KEY_PRODUCTO))
                val cliente = cursor.getString(cursor.getColumnIndex(KEY_CLIENTE))
                val estado = cursor.getString(cursor.getColumnIndex(KEY_ESTADO))
                envios.add(Envio(id, fechaEnvio, producto, cliente, estado))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return envios
    }

    @SuppressLint("Range")
    fun getAllClientes(): List<Cliente> {
        val clientes = mutableListOf<Cliente>()
        val selectQuery = "SELECT * FROM $TABLE_CLIENTES"
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndex(KEY_ID))
                val nombre = cursor.getString(cursor.getColumnIndex(KEY_NOMBRE))
                val numero = cursor.getString(cursor.getColumnIndex(KEY_NUMERO))
                val telefono = cursor.getString(cursor.getColumnIndex(KEY_TELEFONO))
                val direccion = cursor.getString(cursor.getColumnIndex(KEY_DIRECCION))
                val email = cursor.getString(cursor.getColumnIndex(KEY_EMAIL))
                clientes.add(Cliente(id, nombre, numero, telefono, direccion, email))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return clientes
    }

    @SuppressLint("Range")
    fun getAllProveedores(): List<Proveedor> {
        val proveedores = mutableListOf<Proveedor>()
        val selectQuery = "SELECT * FROM $TABLE_PROVEEDORES"
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndex(KEY_ID))
                val nombre = cursor.getString(cursor.getColumnIndex(KEY_NOMBRE))
                val numero = cursor.getString(cursor.getColumnIndex(KEY_NUMERO))
                val telefono = cursor.getString(cursor.getColumnIndex(KEY_TELEFONO))
                val direccion = cursor.getString(cursor.getColumnIndex(KEY_DIRECCION))
                val email = cursor.getString(cursor.getColumnIndex(KEY_EMAIL))
                proveedores.add(Proveedor(id, nombre, numero, telefono, direccion, email))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return proveedores
    }
}
    // TODO: Implement get by parameter, update, and delete operations for each entity

// Composables views of create

@Composable
fun CreateProductoView(onNavigateBack: () -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var proveedor by remember { mutableStateOf("") }
    val context = LocalContext.current
    val databaseHelper = (context.applicationContext as GremlinsApplication).databaseHelper

    CreateViewScaffold(
        title = "Crear nuevo producto",
        onNavigateBack = onNavigateBack
    ) {
        InputField(value = nombre, onValueChange = { nombre = it }, label = "Nombre del producto")
        InputField(value = precio, onValueChange = { precio = it }, label = "Precio")
        InputField(value = proveedor, onValueChange = { proveedor = it }, label = "Proveedor")
        SaveButton {
            if (nombre.isNotBlank() && precio.isNotBlank() && proveedor.isNotBlank()) {
                try {
                    val id = databaseHelper.addProducto(nombre, precio.toDouble(), proveedor)
                    if (id > 0) {
                        Toast.makeText(context, "Producto guardado con éxito", Toast.LENGTH_SHORT).show()
                        onNavigateBack()
                    } else {
                        Toast.makeText(context, "Error al guardar el producto", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
fun CreateEnvioView(onNavigateBack: () -> Unit) {
    var fechaEnvio by remember { mutableStateOf("") }
    var producto by remember { mutableStateOf("") }
    var cliente by remember { mutableStateOf("") }
    var estado by remember { mutableStateOf("") }
    val context = LocalContext.current
    val databaseHelper = (context.applicationContext as GremlinsApplication).databaseHelper

    CreateViewScaffold(
        title = "Crear nuevo envío",
        onNavigateBack = onNavigateBack
    ) {
        InputField(value = fechaEnvio, onValueChange = { fechaEnvio = it }, label = "Fecha de envío")
        InputField(value = producto, onValueChange = { producto = it }, label = "Producto")
        InputField(value = cliente, onValueChange = { cliente = it }, label = "Cliente")
        InputField(value = estado, onValueChange = { estado = it }, label = "Estado")
        SaveButton {
            if (fechaEnvio.isNotBlank() && producto.isNotBlank() && cliente.isNotBlank() && estado.isNotBlank()) {
                try {
                    val id = databaseHelper.addEnvio(fechaEnvio, producto, cliente, estado)
                    if (id > 0) {
                        Toast.makeText(context, "Envío guardado con éxito", Toast.LENGTH_SHORT).show()
                        onNavigateBack()
                    } else {
                        Toast.makeText(context, "Error al guardar el envío", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
fun CreateClienteView(onNavigateBack: () -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var numero by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    val context = LocalContext.current
    val databaseHelper = (context.applicationContext as GremlinsApplication).databaseHelper

    CreateViewScaffold(
        title = "Crear nuevo cliente",
        onNavigateBack = onNavigateBack
    ) {
        InputField(value = nombre, onValueChange = { nombre = it }, label = "Nombre y apellidos")
        InputField(value = numero, onValueChange = { numero = it }, label = "Número")
        InputField(value = telefono, onValueChange = { telefono = it }, label = "Teléfono")
        InputField(value = direccion, onValueChange = { direccion = it }, label = "Dirección")
        InputField(value = email, onValueChange = { email = it }, label = "Email")
        SaveButton {
            if (nombre.isNotBlank() && numero.isNotBlank() && telefono.isNotBlank() && direccion.isNotBlank() && email.isNotBlank()) {
                try {
                    val id = databaseHelper.addCliente(nombre, numero, telefono, direccion, email)
                    if (id > 0) {
                        Toast.makeText(context, "Cliente guardado con éxito", Toast.LENGTH_SHORT).show()
                        onNavigateBack()
                    } else {
                        Toast.makeText(context, "Error al guardar el cliente", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
fun CreateProveedorView(onNavigateBack: () -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var numero by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    val context = LocalContext.current
    val databaseHelper = (context.applicationContext as GremlinsApplication).databaseHelper

    CreateViewScaffold(
        title = "Crear nuevo proveedor",
        onNavigateBack = onNavigateBack
    ) {
        InputField(value = nombre, onValueChange = { nombre = it }, label = "Nombre y apellidos")
        InputField(value = numero, onValueChange = { numero = it }, label = "Número")
        InputField(value = telefono, onValueChange = { telefono = it }, label = "Teléfono")
        InputField(value = direccion, onValueChange = { direccion = it }, label = "Dirección")
        InputField(value = email, onValueChange = { email = it }, label = "Email")
        SaveButton {
            if (nombre.isNotBlank() && numero.isNotBlank() && telefono.isNotBlank() && direccion.isNotBlank() && email.isNotBlank()) {
                try {
                    val id = databaseHelper.addProveedor(nombre, numero, telefono, direccion, email)
                    if (id > 0) {
                        Toast.makeText(context, "Proveedor guardado con éxito", Toast.LENGTH_SHORT).show()
                        onNavigateBack()
                    } else {
                        Toast.makeText(context, "Error al guardar el proveedor", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateViewScaffold(
    title: String,
    onNavigateBack: () -> Unit,
    content: @Composable () -> Unit
) {
    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text(title, color = Color.White) },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = Color(0xFF00796B)
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            content()
        }
    }
}

@Composable
fun InputField(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    )
}

@Composable
fun SaveButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722))
    ) {
        Text("Guardar")
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MaterialTheme {
        GremlinsApp()
    }
}