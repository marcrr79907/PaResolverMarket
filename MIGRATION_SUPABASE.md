# Guía de Migración a Supabase

## ✅ Cambios realizados

### 1. Repositorios unificados en `commonMain`

Se han creado implementaciones únicas que funcionan en **Android e iOS**:

- ✅ `AuthRepositoryImpl.kt` - Autenticación (email/password y Google OAuth)
- ✅ `ProductRepositoryImpl.kt` - Ya existía, usa Supabase Postgrest
- ✅ `StorageRepositoryImpl.kt` - Subida de imágenes a Supabase Storage
- ✅ `CartRepositoryImpl.kt` - Carrito con Realtime (opcional)
- ✅ `DataModule.kt` - Módulo DI que registra todos los repositories

### 2. Archivos a ELIMINAR

Puedes eliminar todas las implementaciones específicas por plataforma:

**Android:**
```
composeApp/src/androidMain/kotlin/com/market/paresolvershop/data/
├── AuthRepositoryAndroid.kt
├── ProductRepositoryAndroid.kt
├── CartRepositoryAndroid.kt
└── StorageRepositoryAndroid.kt
```

**iOS:**
```
composeApp/src/iosMain/kotlin/com/market/paresolvershop/data/
├── AuthRepositoryIos.kt
├── ProductRepositoryIos.kt
└── StorageRepositoryIos.kt
```

**Comando para eliminar:**
```bash
rm -rf composeApp/src/androidMain/kotlin/com/market/paresolvershop/data/*Repository*.kt
rm -rf composeApp/src/iosMain/kotlin/com/market/paresolvershop/data/*Repository*.kt
```

### 3. Configurar Koin

Asegúrate de incluir `dataModule` en la inicialización de Koin:

```kotlin
// En tu App.kt o MainActivity/AppDelegate
fun initKoin() {
    startKoin {
        modules(
            supabaseModule,    // SupabaseClient
            dataModule,        // 🆕 NUEVO: Repositories
            productModule,
            authModule,
            cartModule,
            checkoutModule,
            platformModule
        )
    }
}
```

### 4. Estructura de base de datos en Supabase

Necesitas crear estas tablas en Supabase:

#### Tabla `users`
```sql
CREATE TABLE users (
  id UUID PRIMARY KEY REFERENCES auth.users(id),
  email TEXT NOT NULL,
  name TEXT NOT NULL,
  role TEXT DEFAULT 'client',
  created_at TIMESTAMP DEFAULT NOW()
);

-- RLS (Row Level Security)
ALTER TABLE users ENABLE ROW LEVEL SECURITY;

-- Policy: Los usuarios solo pueden ver/editar su propio perfil
CREATE POLICY "Users can view own profile" ON users
  FOR SELECT USING (auth.uid() = id);

CREATE POLICY "Users can update own profile" ON users
  FOR UPDATE USING (auth.uid() = id);
```

#### Tabla `products`
```sql
CREATE TABLE products (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  name TEXT NOT NULL,
  description TEXT,
  price NUMERIC NOT NULL,
  stock INTEGER NOT NULL DEFAULT 0,
  image_url TEXT,
  category TEXT,
  created_at TIMESTAMP DEFAULT NOW()
);

-- RLS
ALTER TABLE products ENABLE ROW LEVEL SECURITY;

-- Policy: Todos pueden leer productos
CREATE POLICY "Anyone can view products" ON products
  FOR SELECT USING (true);

-- Policy: Solo admins pueden insertar/actualizar/eliminar
CREATE POLICY "Admins can manage products" ON products
  FOR ALL USING (
    EXISTS (
      SELECT 1 FROM users 
      WHERE users.id = auth.uid() 
      AND users.role = 'admin'
    )
  );
```

#### Tabla `cart_items`
```sql
CREATE TABLE cart_items (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
  product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
  quantity INTEGER NOT NULL DEFAULT 1,
  created_at TIMESTAMP DEFAULT NOW(),
  UNIQUE(user_id, product_id)
);

-- RLS
ALTER TABLE cart_items ENABLE ROW LEVEL SECURITY;

-- Policy: Los usuarios solo pueden ver/editar sus propios items
CREATE POLICY "Users can view own cart" ON cart_items
  FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "Users can insert own cart" ON cart_items
  FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update own cart" ON cart_items
  FOR UPDATE USING (auth.uid() = user_id);

CREATE POLICY "Users can delete own cart" ON cart_items
  FOR DELETE USING (auth.uid() = user_id);
```

#### Storage Bucket `product_images`

En Supabase Dashboard > Storage:
1. Crear bucket llamado `product_images`
2. Hacer público (o configurar políticas personalizadas)
3. Permitir subida de imágenes (JPG, PNG, WEBP)

**Política de Storage:**
```sql
-- Policy: Los admins pueden subir imágenes
CREATE POLICY "Admins can upload images" ON storage.objects
  FOR INSERT WITH CHECK (
    bucket_id = 'product_images' AND
    EXISTS (
      SELECT 1 FROM users 
      WHERE users.id = auth.uid() 
      AND users.role = 'admin'
    )
  );

-- Policy: Todos pueden ver imágenes
CREATE POLICY "Anyone can view images" ON storage.objects
  FOR SELECT USING (bucket_id = 'product_images');
```

### 5. Configurar credenciales

Edita `composeApp/src/commonMain/kotlin/com/market/paresolvershop/di/SupabaseModule.kt`:

```kotlin
single<SupabaseClient> {
    createSupabaseClient(
        supabaseUrl = "https://tu-proyecto.supabase.co",
        supabaseKey = "tu-anon-key"
    ) {
        install(Auth)
        install(Postgrest)
        install(Storage)
        install(Realtime)
        
        defaultSerializer = KotlinXSerializer(Json {
            ignoreUnknownKeys = true
            explicitNulls = false
            encodeDefaults = true
            isLenient = true
        })
    }
}
```

O mejor aún, usa `local.properties`:
```properties
supabase.url=https://tu-proyecto.supabase.co
supabase.anon.key=tu-anon-key
```

### 6. Configurar Google OAuth (opcional)

Si usas Google Sign-In:

1. En Supabase Dashboard > Authentication > Providers
2. Habilitar Google provider
3. Configurar Client ID y Secret de Google Cloud Console
4. Agregar redirect URL: `https://tu-proyecto.supabase.co/auth/v1/callback`

En tu `local.properties`:
```properties
web_client_id=tu-google-client-id.apps.googleusercontent.com
```

### 7. Ventajas de esta arquitectura

✅ **Código unificado**: Una sola implementación para Android e iOS
✅ **Sin Firebase**: Todo migrado a Supabase
✅ **Realtime opcional**: Puedes activar/desactivar según necesites
✅ **Type-safe**: Usa Kotlin Serialization
✅ **Clean Architecture**: Domain ← Data ← Supabase
✅ **Multiplataforma nativo**: No necesitas expect/actual

### 8. Próximos pasos

1. ✅ Crear tablas en Supabase (ver sección 4)
2. ✅ Configurar RLS y políticas de seguridad
3. ✅ Configurar credenciales en `SupabaseModule.kt`
4. ✅ Registrar `dataModule` en Koin
5. ✅ Eliminar implementaciones antiguas (Android/iOS)
6. ✅ Probar autenticación, productos y carrito
7. ✅ Opcional: Configurar Realtime para el carrito

### 9. Diferencias clave con Firebase

| Firebase | Supabase |
|----------|----------|
| `FirebaseAuth.currentUser` | `supabase.auth.currentUserOrNull()` |
| `signInWithEmailAndPassword()` | `auth.signInWith(Email)` |
| `firestore.collection("users")` | `from("users").select()` |
| `storage.reference.putBytes()` | `storage.from().upload()` |
| `addSnapshotListener` | `postgresChangeFlow` (Realtime) |

### 10. Troubleshooting

**Error: "Row Level Security policy violation"**
- Asegúrate de haber configurado las políticas RLS correctamente

**Error: "JWT expired"**
- La sesión ha expirado, llama a `auth.refreshCurrentSession()`

**Error: "Bucket not found"**
- Crea el bucket `product_images` en Supabase Storage

**Realtime no funciona**
- Verifica que Realtime esté habilitado en tu tabla
- Asegúrate de tener `install(Realtime)` en SupabaseModule
