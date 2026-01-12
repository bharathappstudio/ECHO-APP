############################
# Google Sign-In
############################
-keep class com.google.android.gms.auth.api.signin.** { *; }
-keep class com.google.android.gms.common.api.** { *; }
-keep class com.google.android.gms.common.internal.** { *; }

############################
# Firebase Auth
############################
-keep class com.google.firebase.auth.** { *; }
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.tasks.** { *; }

############################
# Kotlin Coroutines
############################
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

############################
# Reflection / Metadata
############################
-keepattributes Signature
-keepattributes *Annotation*

############################
# Prevent noisy warnings (SAFE)
############################
-dontwarn com.google.android.gms.**
-dontwarn com.google.firebase.**
