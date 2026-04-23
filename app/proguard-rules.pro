# Add project specific ProGuard rules here.
# Hilt
-keepattributes *Annotation*
-keep class dagger.hilt.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao interface *
