# Room SQLite & Kotlin Release Minification Rules
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keep class androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Keep Domain, Enum, Backup & Entity Models
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keep class com.researchbangla.mechanicalpronotes.domain.model.** { *; }
-keep class com.researchbangla.mechanicalpronotes.data.local.database.entity.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keepattributes SourceFile,LineNumberTable
