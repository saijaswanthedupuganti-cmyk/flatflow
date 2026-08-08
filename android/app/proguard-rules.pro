# Keep source file / line numbers so Play Console can deobfuscate crash stack traces
# (the mapping file is uploaded with the bundle), while still renaming the source file.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Credential Manager loads its Play Services provider reflectively at runtime, so R8
# cannot see the reference and would strip it. Rule as published in the official
# Credential Manager integration guide.
-if class androidx.credentials.CredentialManager
-keep class androidx.credentials.playservices.** {
  *;
}

# GoogleIdTokenCredential.createFrom() reconstructs the credential from the raw Bundle
# returned by the provider, matched against this library's own type constants. Keeping
# the classes and their members avoids a stripped/renamed constant breaking the parse.
-keep class com.google.android.libraries.identity.googleid.** { *; }

# Firebase Auth and Firestore ship consumer ProGuard rules inside their AARs, which R8
# applies automatically. This app never calls DocumentSnapshot.toObject()/toObjects()
# and declares no @Keep / @PropertyName / @IgnoreExtraProperties model classes - all
# document reads go through snapshot.data and typed getters (getString, etc.), and all
# writes pass explicit Map<String, Any?>. So there is no reflected POJO surface here and
# no additional Firebase keep rules are required.
