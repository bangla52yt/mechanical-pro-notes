# Room SQLite & Kotlin Release Minification Rules
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod,RuntimeVisibleAnnotations
-keep class androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Keep Domain, Enum, Backup & Entity Models
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keep class com.researchbangla.mechanicalpronotes.domain.model.** { *; }
-keep class com.researchbangla.mechanicalpronotes.data.local.database.entity.** { *; }
-keep class com.researchbangla.mechanicalpronotes.data.repository.ValidatedBackupPayload { *; }
-keep class * extends androidx.room.RoomDatabase

# Keep kotlinx.serialization & DataStore Preferences runtime hooks
-keepattributes *Annotation*,InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}
-keep class androidx.datastore.preferences.core.Preferences$Key { *; }
-keep class androidx.datastore.preferences.core.MutablePreferences { *; }

# Keep helpful crash stacktrace metadata without retaining raw local variables
-keepattributes SourceFile,LineNumberTable
