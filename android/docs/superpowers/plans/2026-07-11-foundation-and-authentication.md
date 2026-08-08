# Foundation & Authentication Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Stand up the native Android project from scratch and get real Firebase Authentication working end to end, producing an APK Sai can install on his phone and use to sign in with real accounts (Google or email/password) against the production `habitiq-by-jaswanth` Firebase project.

**Architecture:** Kotlin + Jetpack Compose, MVVM. A `AuthRepository` wraps Firebase Auth and is the only thing ViewModels talk to. No custom backend server — the app talks directly to Firebase, same as the web app. This plan stops at "signed in, session persists, user doc exists in Firestore" — it does not yet build flats, tasks, or any other feature.

**Tech Stack:** Kotlin 2.2.10, Jetpack Compose, AGP 8.13.0, Gradle 9.4.1 (cached wrapper already present at `C:\Users\user\.gradle\wrapper\dists\gradle-9.4.1-bin`), Firebase BOM 34.15.0, Firebase Auth, Cloud Firestore, Credential Manager (`androidx.credentials`) for Google Sign-In, Jetpack Navigation Compose.

## Global Constraints

- **applicationId / namespace: `habitiq.app`** — exact match required. This is the package name already registered against a Firebase Android app in production (`habitiq-by-jaswanth`, App ID `1:62233483871:android:bc1a447f1c6809f96899ea`). Any other value will make `google-services.json` fail to match ("No matching client found for package name").
- **Firebase project: production `habitiq-by-jaswanth` only.** Never point this app at `garbage-f79f7` or any other project.
- **No offline mode.** Every screen that depends on network state must have a working "no connection" case — this plan's screens (Login/Signup/Home) must handle "network unavailable" without crashing or hanging silently.
- **No secrets in the APK.** `google-services.json` (client config) is safe to include. A service-account/Admin SDK JSON key is never placed in this project, ever — if one shows up, stop and ask before proceeding.
- **JAVA_HOME** for all Gradle commands in this plan must point at Android Studio's bundled JBR (matches the working setup already validated on this machine for the old scaffold), not a generic system JDK, or Kotlin metadata errors will surface (this bit the earlier native attempt — see `project_habitiq_native_rebuild.md`).

---

## File Structure

```
C:\habitiq_jaswanth\
  settings.gradle.kts
  build.gradle.kts
  gradle\libs.versions.toml
  gradle\wrapper\gradle-wrapper.properties
  app\build.gradle.kts
  app\google-services.json          (Sai provides this — see Task 2)
  app\src\main\AndroidManifest.xml
  app\src\main\kotlin\habitiq\app\
    MainActivity.kt
    HabitiqApp.kt                    (root Composable + NavHost)
    auth\
      AuthRepository.kt
      AuthUiState.kt
      AuthErrorMapper.kt
      LoginViewModel.kt
      SignupViewModel.kt
    data\
      UsersRepository.kt
      UserProfile.kt
    ui\
      LoginScreen.kt
      SignupScreen.kt
      HomeScreen.kt
      theme\Theme.kt
  app\src\test\kotlin\habitiq\app\
    auth\AuthErrorMapperTest.kt
    data\UsersRepositoryTest.kt
```

Each file has one job: `AuthRepository` only wraps Firebase Auth calls (no UI, no navigation). `AuthErrorMapper` only turns Firebase exceptions into plain-language strings (pure function, fully unit-testable). ViewModels only hold screen state and call repositories. Screens only render state and forward user actions to ViewModels.

---

### Task 1: Gradle & Android project scaffold

**Files:**
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts`
- Create: `gradle/libs.versions.toml`
- Create: `app/build.gradle.kts`
- Create: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/kotlin/habitiq/app/MainActivity.kt`
- Create: `app/src/main/kotlin/habitiq/app/HabitiqApp.kt`
- Create: `app/src/main/kotlin/habitiq/app/ui/theme/Theme.kt`
- Create: `app/src/main/res/values/strings.xml`

**Interfaces:**
- Produces: `MainActivity` (entry point, hosts Compose content), `HabitiqApp()` composable (root of the UI tree — later tasks add real content inside it)

- [ ] **Step 1: Create `settings.gradle.kts`**

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Habitiq"
include(":app")
```

- [ ] **Step 2: Create `gradle/libs.versions.toml`**

```toml
[versions]
agp = "8.13.0"
kotlin = "2.2.10"
coreKtx = "1.15.0"
lifecycle = "2.9.0"
activityCompose = "1.10.0"
composeBom = "2025.09.00"
navigationCompose = "2.9.0"
googleServices = "4.5.0"
firebaseBom = "34.15.0"
credentials = "1.5.0"
googleid = "1.1.1"
kotlinxCoroutines = "1.9.0"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycle" }
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycle" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
androidx-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-material3 = { group = "androidx.compose.material3", name = "material3" }
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigationCompose" }
firebase-bom = { group = "com.google.firebase", name = "firebase-bom", version.ref = "firebaseBom" }
firebase-auth = { group = "com.google.firebase", name = "firebase-auth" }
firebase-firestore = { group = "com.google.firebase", name = "firebase-firestore" }
androidx-credentials = { group = "androidx.credentials", name = "credentials", version.ref = "credentials" }
androidx-credentials-play-services-auth = { group = "androidx.credentials", name = "credentials-play-services-auth", version.ref = "credentials" }
googleid = { group = "com.google.android.libraries.identity.googleid", name = "googleid", version.ref = "googleid" }
kotlinx-coroutines-play-services = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-play-services", version.ref = "kotlinxCoroutines" }
junit = { group = "junit", name = "junit", version = "4.13.2" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
google-services = { id = "com.google.gms.google-services", version.ref = "googleServices" }
```

- [ ] **Step 3: Create root `build.gradle.kts`**

```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.google.services) apply false
}
```

- [ ] **Step 4: Create `app/build.gradle.kts`**

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "habitiq.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "habitiq.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)

    testImplementation(libs.junit)
}
```

(Note: this task deliberately has no Firebase plugin/dependencies yet — those arrive in Task 2, so this task's build failure surface is small and easy to debug in isolation.)

- [ ] **Step 5: Create `app/src/main/AndroidManifest.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.INTERNET" />

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="Habitiq"
        android:theme="@style/Theme.Habitiq">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.Habitiq">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

- [ ] **Step 6: Create `app/src/main/res/values/strings.xml`**

```xml
<resources>
    <string name="app_name">Habitiq</string>
</resources>
```

- [ ] **Step 7: Create `app/src/main/kotlin/habitiq/app/ui/theme/Theme.kt`**

```kotlin
package habitiq.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme()
private val LightColors = lightColorScheme()

@Composable
fun HabitiqTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, content = content)
}
```

- [ ] **Step 8: Create `app/src/main/kotlin/habitiq/app/HabitiqApp.kt`**

```kotlin
package habitiq.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import habitiq.app.ui.theme.HabitiqTheme

@Composable
fun HabitiqApp() {
    HabitiqTheme {
        Surface(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
            Box(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
                Text("Habitiq — foundation scaffold")
            }
        }
    }
}
```

- [ ] **Step 9: Create `app/src/main/kotlin/habitiq/app/MainActivity.kt`**

```kotlin
package habitiq.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HabitiqApp()
        }
    }
}
```

- [ ] **Step 10: Build and verify**

Run (from `C:\habitiq_jaswanth`, with `JAVA_HOME` pointed at Android Studio's bundled JBR):

```bash
"$JAVA_HOME/../gradle-9.4.1/bin/gradle" assembleDebug
```

(Use whichever invocation form was already working for the old scaffold's cached Gradle 9.4.1 install — see `project_habitiq_native_rebuild.md` for the exact working command line on this machine.)

Expected: `BUILD SUCCESSFUL`, and `app/build/outputs/apk/debug/app-debug.apk` exists.

- [ ] **Step 11: Install and visually verify**

Run: `adb install -r app/build/outputs/apk/debug/app-debug.apk`

Expected: app installs, launching it shows a blank screen with the text "Habitiq — foundation scaffold".

- [ ] **Step 12: Commit**

```bash
git add settings.gradle.kts build.gradle.kts gradle/libs.versions.toml app/build.gradle.kts app/src/main/AndroidManifest.xml app/src/main/kotlin app/src/main/res
git commit -m "feat: scaffold Android/Compose project, no Firebase yet"
```

---

### Task 2: Firebase SDK wiring

**Files:**
- Modify: `build.gradle.kts` (root)
- Modify: `app/build.gradle.kts`
- Modify: `gradle/libs.versions.toml`
- Create: `app/google-services.json` (Sai provides — see Step 1)
- Modify: `app/src/main/kotlin/habitiq/app/MainActivity.kt`

**Interfaces:**
- Consumes: nothing new
- Produces: `FirebaseApp` initialized automatically at process start (via the `google-services` plugin's auto-init — no code needed for this), available to later tasks via `Firebase.auth` / `Firebase.firestore`

- [ ] **Step 1: Obtain `google-services.json` from Sai**

This file cannot be fabricated — it's the real client config tied to the pre-registered Android app (`habitiq.app`) inside the production `habitiq-by-jaswanth` Firebase project. Ask Sai to download it from Firebase Console → Project Settings → the `habitiq.app` Android app → "Download google-services.json", and place it at `C:\habitiq_jaswanth\app\google-services.json`.

Do not proceed past this step without the real file in place — a placeholder/fake file will make every later Firebase call fail with an unhelpful error.

- [ ] **Step 2: Add the `google-services` plugin to the root build file**

In `build.gradle.kts` (root), the `google-services` plugin is already declared with `apply false` from Task 1 — no change needed there. Apply it in the app module instead (next step).

- [ ] **Step 3: Apply the plugin and add Firebase dependencies in `app/build.gradle.kts`**

Add to the `plugins { }` block (after the existing three plugins):

```kotlin
    alias(libs.plugins.google.services)
```

Add to the `dependencies { }` block (after the existing Compose dependencies):

```kotlin
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.kotlinx.coroutines.play.services)
```

- [ ] **Step 4: Build and verify Firebase initializes**

Run: `gradle assembleDebug` (same invocation as Task 1, Step 10)

Expected: `BUILD SUCCESSFUL`. If the build fails with "File google-services.json is missing", go back to Step 1 — Sai's file isn't in place yet.

Install and launch (`adb install -r app/build/outputs/apk/debug/app-debug.apk`), then check logs:

Run: `adb logcat | grep -i firebase`

Expected: no `FirebaseApp initialization unsuccessful` or `No matching client found for package name` errors. (The Google Services plugin auto-initializes `FirebaseApp` — no code required in `MainActivity` for this step.)

- [ ] **Step 5: Commit**

```bash
git add build.gradle.kts app/build.gradle.kts gradle/libs.versions.toml app/google-services.json
git commit -m "feat: wire up Firebase SDK (Auth + Firestore) against production habitiq-by-jaswanth"
```

---

### Task 3: Auth error mapping (pure logic, unit tested)

**Files:**
- Create: `app/src/main/kotlin/habitiq/app/auth/AuthErrorMapper.kt`
- Test: `app/src/test/kotlin/habitiq/app/auth/AuthErrorMapperTest.kt`

**Interfaces:**
- Produces: `fun mapAuthError(exception: Exception): String` — turns a Firebase Auth exception into a plain-language message. Later tasks (Login/Signup ViewModels) call this directly.

- [ ] **Step 1: Write the failing test**

```kotlin
package habitiq.app.auth

import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

class AuthErrorMapperTest {

    @Test
    fun `invalid credentials maps to plain wrong password message`() {
        val exception = FirebaseAuthInvalidCredentialsException("ERROR_WRONG_PASSWORD", "bad creds")
        assertEquals("Incorrect email or password.", mapAuthError(exception))
    }

    @Test
    fun `user collision maps to account exists message`() {
        val exception = FirebaseAuthUserCollisionException("ERROR_EMAIL_ALREADY_IN_USE", "exists")
        assertEquals("An account already exists with this email.", mapAuthError(exception))
    }

    @Test
    fun `weak password maps to plain weak password message`() {
        val exception = FirebaseAuthWeakPasswordException("ERROR_WEAK_PASSWORD", "weak", "PASSWORD_DOES_NOT_MEET_REQUIREMENTS")
        assertEquals("Password is too weak — use at least 6 characters.", mapAuthError(exception))
    }

    @Test
    fun `network error maps to connectivity message`() {
        val exception = IOException("network down")
        assertEquals("No internet connection. Please try again.", mapAuthError(exception))
    }

    @Test
    fun `unknown error maps to generic fallback message`() {
        val exception = RuntimeException("something else")
        assertEquals("Something went wrong. Please try again.", mapAuthError(exception))
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `gradle test --tests "habitiq.app.auth.AuthErrorMapperTest"`
Expected: FAIL — `mapAuthError` is unresolved (function doesn't exist yet).

- [ ] **Step 3: Write minimal implementation**

```kotlin
package habitiq.app.auth

import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import java.io.IOException

fun mapAuthError(exception: Exception): String = when (exception) {
    is FirebaseAuthInvalidCredentialsException -> "Incorrect email or password."
    is FirebaseAuthInvalidUserException -> "No account found with this email."
    is FirebaseAuthUserCollisionException -> "An account already exists with this email."
    is FirebaseAuthWeakPasswordException -> "Password is too weak — use at least 6 characters."
    is IOException -> "No internet connection. Please try again."
    else -> "Something went wrong. Please try again."
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `gradle test --tests "habitiq.app.auth.AuthErrorMapperTest"`
Expected: PASS, all 5 tests green.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/kotlin/habitiq/app/auth/AuthErrorMapper.kt app/src/test/kotlin/habitiq/app/auth/AuthErrorMapperTest.kt
git commit -m "feat: add plain-language Firebase Auth error mapping"
```

---

### Task 4: AuthRepository (Firebase Auth wrapper)

**Files:**
- Create: `app/src/main/kotlin/habitiq/app/auth/AuthRepository.kt`
- Create: `app/src/main/kotlin/habitiq/app/auth/AuthUiState.kt`

**Interfaces:**
- Consumes: `mapAuthError(exception: Exception): String` (Task 3)
- Produces:
  - `class AuthRepository` with:
    - `val currentUser: kotlinx.coroutines.flow.StateFlow<com.google.firebase.auth.FirebaseUser?>`
    - `suspend fun signUpWithEmail(email: String, password: String): Result<Unit>`
    - `suspend fun signInWithEmail(email: String, password: String): Result<Unit>`
    - `suspend fun signInWithGoogleIdToken(idToken: String): Result<Unit>`
    - `fun signOut()`
  - These exact names/signatures are what Task 5 (ViewModels) calls.

- [ ] **Step 1: Create `AuthUiState.kt`**

```kotlin
package habitiq.app.auth

sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Loading : AuthUiState
    data class Error(val message: String) : AuthUiState
    data object Success : AuthUiState
}
```

- [ ] **Step 2: Create `AuthRepository.kt`**

```kotlin
package habitiq.app.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private val _currentUser = MutableStateFlow(firebaseAuth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser

    init {
        firebaseAuth.addAuthStateListener { auth ->
            _currentUser.value = auth.currentUser
        }
    }

    suspend fun signUpWithEmail(email: String, password: String): Result<Unit> = runCatching {
        firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        Unit
    }.recoverCatching { throw IllegalStateException(mapAuthError(it as Exception)) }

    suspend fun signInWithEmail(email: String, password: String): Result<Unit> = runCatching {
        firebaseAuth.signInWithEmailAndPassword(email, password).await()
        Unit
    }.recoverCatching { throw IllegalStateException(mapAuthError(it as Exception)) }

    suspend fun signInWithGoogleIdToken(idToken: String): Result<Unit> = runCatching {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential).await()
        Unit
    }.recoverCatching { throw IllegalStateException(mapAuthError(it as Exception)) }

    fun signOut() {
        firebaseAuth.signOut()
    }
}
```

Note: `recoverCatching` re-wraps the original Firebase exception's message (via `mapAuthError`) into an `IllegalStateException` so callers get a ready-to-display string via `result.exceptionOrNull()?.message` — this keeps `mapAuthError` (Task 3, already unit tested) as the single source of truth for user-facing error text, with no duplicate mapping logic in the ViewModel layer.

- [ ] **Step 3: Build and verify it compiles**

Run: `gradle compileDebugKotlin`
Expected: `BUILD SUCCESSFUL`.

(No new automated test here — this class is a thin wrapper over the Firebase SDK with no branching logic of its own beyond what Task 3 already covers; it's verified end-to-end by the manual sign-in checklist in Task 7.)

- [ ] **Step 4: Commit**

```bash
git add app/src/main/kotlin/habitiq/app/auth/AuthRepository.kt app/src/main/kotlin/habitiq/app/auth/AuthUiState.kt
git commit -m "feat: add AuthRepository wrapping Firebase Auth (email + Google)"
```

---

### Task 5: UsersRepository (create user profile doc on first sign-in)

**Files:**
- Create: `app/src/main/kotlin/habitiq/app/data/UserProfile.kt`
- Create: `app/src/main/kotlin/habitiq/app/data/UsersRepository.kt`
- Test: `app/src/test/kotlin/habitiq/app/data/UsersRepositoryTest.kt`

**Interfaces:**
- Consumes: nothing new
- Produces:
  - `data class UserProfile(val uid: String, val email: String, val displayName: String?)`
  - `class UsersRepository` with `suspend fun ensureUserDocument(profile: UserProfile): Result<Unit>` — Task 6 (Home screen / post-login flow) calls this once right after a successful sign-in.
  - `fun shouldCreateDocument(existingData: Map<String, Any?>?): Boolean` — top-level pure function (not a member of `UsersRepository`), unit-tested directly, called internally by `ensureUserDocument`.

- [ ] **Step 1: Write the failing test for the pure decision logic**

```kotlin
package habitiq.app.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UsersRepositoryTest {

    @Test
    fun `should create document when no existing data`() {
        assertTrue(shouldCreateDocument(existingData = null))
    }

    @Test
    fun `should not create document when data already exists`() {
        assertFalse(shouldCreateDocument(existingData = mapOf("email" to "a@b.com")))
    }

    @Test
    fun `should create document when existing data is empty map`() {
        assertTrue(shouldCreateDocument(existingData = emptyMap()))
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `gradle test --tests "habitiq.app.data.UsersRepositoryTest"`
Expected: FAIL — `shouldCreateDocument` is unresolved.

- [ ] **Step 3: Write `UserProfile.kt`**

```kotlin
package habitiq.app.data

data class UserProfile(
    val uid: String,
    val email: String,
    val displayName: String?
)
```

- [ ] **Step 4: Write `UsersRepository.kt`, including the pure decision function**

```kotlin
package habitiq.app.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

fun shouldCreateDocument(existingData: Map<String, Any?>?): Boolean =
    existingData == null || existingData.isEmpty()

class UsersRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun ensureUserDocument(profile: UserProfile): Result<Unit> = runCatching {
        val docRef = firestore.collection("users").document(profile.uid)
        val snapshot = docRef.get().await()
        if (shouldCreateDocument(snapshot.data)) {
            docRef.set(
                mapOf(
                    "email" to profile.email,
                    "displayName" to profile.displayName
                )
            ).await()
        }
        Unit
    }
}
```

This matches the web app's `users/{userId}` schema (per `C:\garbage\firestore.rules`, `match /users/{userId}`) — same collection, same document ID convention (Firebase Auth UID), so a user created via the native app is indistinguishable from one created via the web app.

- [ ] **Step 5: Run test to verify it passes**

Run: `gradle test --tests "habitiq.app.data.UsersRepositoryTest"`
Expected: PASS, all 3 tests green.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/kotlin/habitiq/app/data/UserProfile.kt app/src/main/kotlin/habitiq/app/data/UsersRepository.kt app/src/test/kotlin/habitiq/app/data/UsersRepositoryTest.kt
git commit -m "feat: add UsersRepository, create users/{uid} doc on first sign-in"
```

---

### Task 6: Login/Signup/Home screens + ViewModels + navigation shell

**Files:**
- Create: `app/src/main/kotlin/habitiq/app/auth/LoginViewModel.kt`
- Create: `app/src/main/kotlin/habitiq/app/auth/SignupViewModel.kt`
- Create: `app/src/main/kotlin/habitiq/app/ui/LoginScreen.kt`
- Create: `app/src/main/kotlin/habitiq/app/ui/SignupScreen.kt`
- Create: `app/src/main/kotlin/habitiq/app/ui/HomeScreen.kt`
- Modify: `app/src/main/kotlin/habitiq/app/HabitiqApp.kt`

**Interfaces:**
- Consumes: `AuthRepository` (Task 4), `UsersRepository` + `UserProfile` (Task 5)
- Produces: a session-aware root NavHost — no other task depends on this task's internals, it's the top of the tree.

- [ ] **Step 1: Create `LoginViewModel.kt`**

```kotlin
package habitiq.app.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import habitiq.app.data.UserProfile
import habitiq.app.data.UsersRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val usersRepository: UsersRepository
) : ViewModel() {

    private val _state = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val state: StateFlow<AuthUiState> = _state

    fun signInWithEmail(email: String, password: String) {
        _state.value = AuthUiState.Loading
        viewModelScope.launch {
            val result = authRepository.signInWithEmail(email, password)
            onAuthResult(result)
        }
    }

    fun signInWithGoogleIdToken(idToken: String) {
        _state.value = AuthUiState.Loading
        viewModelScope.launch {
            val result = authRepository.signInWithGoogleIdToken(idToken)
            onAuthResult(result)
        }
    }

    private suspend fun onAuthResult(result: Result<Unit>) {
        result.onSuccess {
            val user = authRepository.currentUser.value
            if (user != null) {
                usersRepository.ensureUserDocument(
                    UserProfile(uid = user.uid, email = user.email.orEmpty(), displayName = user.displayName)
                )
            }
            _state.value = AuthUiState.Success
        }.onFailure { error ->
            _state.value = AuthUiState.Error(error.message ?: "Something went wrong. Please try again.")
        }
    }
}
```

- [ ] **Step 2: Create `SignupViewModel.kt`**

```kotlin
package habitiq.app.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import habitiq.app.data.UserProfile
import habitiq.app.data.UsersRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SignupViewModel(
    private val authRepository: AuthRepository,
    private val usersRepository: UsersRepository
) : ViewModel() {

    private val _state = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val state: StateFlow<AuthUiState> = _state

    fun signUpWithEmail(email: String, password: String) {
        _state.value = AuthUiState.Loading
        viewModelScope.launch {
            val result = authRepository.signUpWithEmail(email, password)
            result.onSuccess {
                val user = authRepository.currentUser.value
                if (user != null) {
                    usersRepository.ensureUserDocument(
                        UserProfile(uid = user.uid, email = user.email.orEmpty(), displayName = user.displayName)
                    )
                }
                _state.value = AuthUiState.Success
            }.onFailure { error ->
                _state.value = AuthUiState.Error(error.message ?: "Something went wrong. Please try again.")
            }
        }
    }
}
```

- [ ] **Step 3: Create `LoginScreen.kt`**

```kotlin
package habitiq.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import habitiq.app.auth.AuthUiState
import habitiq.app.auth.LoginViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onSignedIn: () -> Unit,
    onNavigateToSignup: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val state by viewModel.state.collectAsStateWithLifecycleCompat()

    Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
        Text("Log in to Habitiq")
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") })
        Button(onClick = { viewModel.signInWithEmail(email, password) }) {
            Text("Log in")
        }
        Button(onClick = onNavigateToSignup) {
            Text("Need an account? Sign up")
        }
        when (val current = state) {
            is AuthUiState.Loading -> Text("Signing in…")
            is AuthUiState.Error -> Text(current.message)
            is AuthUiState.Success -> onSignedIn()
            else -> {}
        }
    }
}
```

- [ ] **Step 4: Create `SignupScreen.kt`**

```kotlin
package habitiq.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import habitiq.app.auth.AuthUiState
import habitiq.app.auth.SignupViewModel

@Composable
fun SignupScreen(
    viewModel: SignupViewModel,
    onSignedUp: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val state by viewModel.state.collectAsStateWithLifecycleCompat()

    Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
        Text("Create your Habitiq account")
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password (min 6 characters)") })
        Button(onClick = { viewModel.signUpWithEmail(email, password) }) {
            Text("Sign up")
        }
        when (val current = state) {
            is AuthUiState.Loading -> Text("Creating account…")
            is AuthUiState.Error -> Text(current.message)
            is AuthUiState.Success -> onSignedUp()
            else -> {}
        }
    }
}
```

- [ ] **Step 5: Create `HomeScreen.kt`**

```kotlin
package habitiq.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseUser

@Composable
fun HomeScreen(user: FirebaseUser?, onSignOut: () -> Unit) {
    Column(modifier = Modifier.padding(24.dp)) {
        Text("Signed in as: ${user?.email ?: "unknown"}")
        Button(onClick = onSignOut) {
            Text("Sign out")
        }
    }
}
```

- [ ] **Step 6: Add the `collectAsStateWithLifecycleCompat` helper**

The screens above use a small helper so this plan doesn't pull in the separate `androidx.lifecycle:lifecycle-runtime-compose` artifact just for one function. Create `app/src/main/kotlin/habitiq/app/ui/ComposeUtil.kt`:

```kotlin
package habitiq.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.StateFlow

@Composable
fun <T> StateFlow<T>.collectAsStateWithLifecycleCompat(): State<T> = this.collectAsState()
```

(This is a plain `collectAsState` alias today; if a future task needs true lifecycle-aware collection — pausing updates when the app is backgrounded — swap this one function's body, and every screen using it upgrades automatically.)

- [ ] **Step 7: Wire up the navigation shell in `HabitiqApp.kt`**

Replace the entire file contents with:

```kotlin
package habitiq.app

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import habitiq.app.auth.AuthRepository
import habitiq.app.auth.LoginViewModel
import habitiq.app.auth.SignupViewModel
import habitiq.app.data.UsersRepository
import habitiq.app.ui.HomeScreen
import habitiq.app.ui.LoginScreen
import habitiq.app.ui.SignupScreen
import habitiq.app.ui.theme.HabitiqTheme

private object Routes {
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val HOME = "home"
}

@Composable
fun HabitiqApp() {
    val authRepository = remember { AuthRepository() }
    val usersRepository = remember { UsersRepository() }
    val navController = rememberNavController()
    val currentUser by authRepository.currentUser.collectAsState()

    HabitiqTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = if (currentUser != null) Routes.HOME else Routes.LOGIN
            ) {
                composable(Routes.LOGIN) {
                    val viewModel = remember { LoginViewModel(authRepository, usersRepository) }
                    LoginScreen(
                        viewModel = viewModel,
                        onSignedIn = { navController.navigate(Routes.HOME) { popUpTo(Routes.LOGIN) { inclusive = true } } },
                        onNavigateToSignup = { navController.navigate(Routes.SIGNUP) }
                    )
                }
                composable(Routes.SIGNUP) {
                    val viewModel = remember { SignupViewModel(authRepository, usersRepository) }
                    SignupScreen(
                        viewModel = viewModel,
                        onSignedUp = { navController.navigate(Routes.HOME) { popUpTo(Routes.LOGIN) { inclusive = true } } }
                    )
                }
                composable(Routes.HOME) {
                    HomeScreen(user = currentUser, onSignOut = {
                        authRepository.signOut()
                        navController.navigate(Routes.LOGIN) { popUpTo(Routes.HOME) { inclusive = true } }
                    })
                }
            }
        }
    }
}
```

Add the missing imports this file needs (`androidx.compose.runtime.remember`, `androidx.compose.runtime.collectAsState`) alongside the existing ones at the top.

- [ ] **Step 8: Add the navigation-compose and remaining imports to `app/build.gradle.kts` if not already present**

(Already added in Task 1's `libs.versions.toml`/`app/build.gradle.kts` — `androidx.navigation.compose` — nothing further needed here.)

- [ ] **Step 9: Build and verify**

Run: `gradle assembleDebug`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 10: Commit**

```bash
git add app/src/main/kotlin/habitiq/app
git commit -m "feat: add Login/Signup/Home screens with session-aware navigation"
```

---

### Task 7: Google Sign-In via Credential Manager

**Files:**
- Modify: `app/src/main/kotlin/habitiq/app/auth/LoginViewModel.kt` (no change needed — already exposes `signInWithGoogleIdToken`, added in Task 6)
- Modify: `app/src/main/kotlin/habitiq/app/ui/LoginScreen.kt`
- Create: `app/src/main/kotlin/habitiq/app/auth/GoogleSignInLauncher.kt`
- Modify: `app/src/main/res/values/strings.xml`

**Interfaces:**
- Consumes: `AuthRepository.signInWithGoogleIdToken(idToken: String)` (Task 4), `LoginViewModel.signInWithGoogleIdToken` (Task 6)
- Produces: `suspend fun launchGoogleSignIn(context: android.content.Context, webClientId: String): String?` — returns a Google ID token on success, `null` if the user cancels. `LoginScreen` calls this and forwards the result to the ViewModel.

- [ ] **Step 1: Get the Web Client ID from Sai**

Google Sign-In via Credential Manager needs the Firebase project's **Web Client ID** (an OAuth client ID, NOT a secret — safe to put in app resources). Ask Sai to get it from Firebase Console → Project Settings → General → scroll to "Your apps" → the Web app entry → `Web client ID` under "SDK setup and configuration" (or Google Cloud Console → APIs & Services → Credentials → the auto-created "Web client (Auto created by Google Service)" entry for `habitiq-by-jaswanth`).

- [ ] **Step 2: Add the Web Client ID to `strings.xml`**

Add this line inside the existing `<resources>` block in `app/src/main/res/values/strings.xml`:

```xml
    <string name="google_web_client_id" translatable="false">PASTE_WEB_CLIENT_ID_HERE</string>
```

Replace `PASTE_WEB_CLIENT_ID_HERE` with the real value from Step 1 before building.

- [ ] **Step 3: Create `GoogleSignInLauncher.kt`**

```kotlin
package habitiq.app.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

suspend fun launchGoogleSignIn(context: Context, webClientId: String): String? {
    val option = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(webClientId)
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(option)
        .build()

    return try {
        val response = CredentialManager.create(context).getCredential(context, request)
        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(response.credential.data)
        googleIdTokenCredential.idToken
    } catch (e: GetCredentialCancellationException) {
        null
    }
}
```

- [ ] **Step 4: Add the Google Sign-In button to `LoginScreen.kt`**

Add these imports at the top:

```kotlin
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.rememberCoroutineScope
import habitiq.app.R
import kotlinx.coroutines.launch
```

Inside the `LoginScreen` composable, add after the "Need an account? Sign up" button and before the `when (val current = state)` block:

```kotlin
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        val webClientId = androidx.compose.ui.res.stringResource(R.string.google_web_client_id)
        Button(onClick = {
            coroutineScope.launch {
                val idToken = habitiq.app.auth.launchGoogleSignIn(context, webClientId)
                if (idToken != null) {
                    viewModel.signInWithGoogleIdToken(idToken)
                }
            }
        }) {
            Text("Sign in with Google")
        }
```

- [ ] **Step 5: Build and verify**

Run: `gradle assembleDebug`
Expected: `BUILD SUCCESSFUL`.

(No unit test here — this task is Android SDK/Credential Manager plumbing with no branching logic of its own; the real ID-token round-trip is only meaningful on a device with a real Google account, which Task 8's manual checklist covers.)

- [ ] **Step 6: Commit**

```bash
git add app/src/main/kotlin/habitiq/app/auth/GoogleSignInLauncher.kt app/src/main/kotlin/habitiq/app/ui/LoginScreen.kt app/src/main/res/values/strings.xml
git commit -m "feat: add Google Sign-In via Credential Manager"
```

---

### Task 8: Manual on-device verification with real accounts, then tag

This task has no new source files — it's the real-world test that everything before it actually works against production Firebase, using real accounts, which is the whole point of this plan.

- [ ] **Step 1: Install the current build**

Run: `gradle assembleDebug && adb install -r app/build/outputs/apk/debug/app-debug.apk`

- [ ] **Step 2: Manual test — email/password sign-up**

On the device: launch Habitiq, go to Sign Up, enter a real test email + password (6+ characters), tap Sign Up.
Expected: navigates to Home screen showing "Signed in as: <that email>".
Verify in Firebase Console → Authentication → Users: the new user appears. Verify in Firestore → `users/{uid}`: a document exists with matching `email`.

- [ ] **Step 3: Manual test — sign out and email/password sign-in**

Tap Sign Out → back at Login screen. Enter the same email/password, tap Log in.
Expected: navigates to Home screen again, same UID/email.

- [ ] **Step 4: Manual test — wrong password**

Sign out, try logging in with the same email and a deliberately wrong password.
Expected: screen shows "Incorrect email or password." (from `AuthErrorMapper`, Task 3), not a raw exception or crash.

- [ ] **Step 5: Manual test — second distinct real account**

Sign out, sign up a second, different real email address.
Expected: a second, separate `users/{uid}` document appears in Firestore — confirms multiple real users can use the app independently, which is what Sai asked this build to demonstrate.

- [ ] **Step 6: Manual test — Google Sign-In**

Sign out. Tap "Sign in with Google", pick a real Google account in the account picker.
Expected: navigates to Home screen showing that Google account's email. Verify in Firebase Console → Authentication → Users: the account appears with provider "Google". Verify a matching `users/{uid}` document exists in Firestore.

- [ ] **Step 7: Manual test — airplane mode**

Turn on airplane mode, attempt to log in.
Expected: screen shows "No internet connection. Please try again." within a few seconds — not an infinite spinner, not a crash. Turn airplane mode back off before continuing.

- [ ] **Step 8: Tag this milestone**

```bash
git tag -a v0.1.0-foundation-auth -m "Foundation + real Firebase Auth working end to end, verified with 2 real accounts"
```

---

## Self-Review Notes

- **Spec coverage for this slice:** Section 1 (architecture: repositories, MVVM, no server) ✅ Task 1/4/5. Section 5 (Auth: Google + Email/Password, session persistence, user doc creation matching web schema) ✅ Tasks 3, 4, 5, 6, 7. Section 6 (error handling, no offline/no silent failures) ✅ Task 3 + Task 8 Step 7. Section 7 (Play Store non-functional bar: no secrets in APK, loading/error states) ✅ Global Constraints + Task 6 screens' Loading/Error branches. Remaining spec sections (Flats/Members, Tasks, Expenses, Settlements, Discover, Rewards, automated CI-style testing beyond unit tests) are out of scope for this plan by design — each gets its own follow-up plan.
- **Google Sign-In via Credential Manager**: initially drafted as deferred, then corrected during self-review — the approved spec requires Google + Email/Password for v1, so it's included as Task 7, with its own manual verification step in Task 8. Both providers are now covered end-to-end in this plan, not just email/password.
- **Two known external dependencies this plan cannot self-serve**: `app/google-services.json` (Task 2, Step 1) and the Web Client ID (Task 7, Step 1) both must come from Sai via the Firebase Console — flagged inline at the point each is needed rather than assumed.
