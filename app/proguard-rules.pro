# ===========================================================================
# Reglas de Ofuscación y Optimización R8 / ProGuard
# ===========================================================================

# Optimización agresiva y modificación de accesos para mayor ratio de ofuscación
-allowaccessmodification
-repackageclasses ''
-overloadaggressively

# Preservar atributos necesarios para Kotlin Coroutines, Compose y Room
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Preservar información de línea para stacktraces ofuscados
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Componentes Android del AndroidManifest
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends androidx.core.content.FileProvider

# ViewModels: mantener constructores para instanciación por ViewModelProvider
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# Room Database y Entidades
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keepclassmembers class * {
    @androidx.room.PrimaryKey *;
    @androidx.room.ColumnInfo *;
    @androidx.room.Embedded *;
    @androidx.room.Relation *;
}
-dontwarn androidx.room.paging.**

# Supresión de advertencias en librerías comunes
-dontwarn kotlinx.coroutines.**
-dontwarn coil.**
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-dontwarn com.google.firebase.**

