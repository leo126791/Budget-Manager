# ProGuard & R8 Optimization Rules for Budget Manager

# Keep Room Entities, Models & DAOs
-keep class com.example.debit.data.** { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public <init>();
}

# Keep Compose Runtime
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}

# Keep Kotlin Metadata
-keepclassmembers class * {
    @kotlin.jvm.JvmField *;
}
