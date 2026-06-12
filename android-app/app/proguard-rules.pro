# OkHttp + Okio
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Modèles de données (sérialisation JSON manuelle via JSONObject)
-keep class com.ostirotix.app.data.model.** { *; }

# Conserver les annotations (nécessaire pour certaines librairies)
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes Signature

# Compose : géré automatiquement par AGP 8.x / R8
# Kotlin Coroutines
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**
