# Technical Fixes & Account Deletion Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix two real architectural gaps found by an external audit (ViewModels not surviving configuration changes; Firestore listeners never pausing in the background) and add the account deletion Google Play requires before launch.

**Architecture:** Same shape as prior plans in this project — repository methods return `Result<T>`, errors map through the existing `mapAuthError`/plain-language pattern, ViewModels expose `StateFlow`, screens render state.

**Tech Stack:** Same project (Kotlin, Jetpack Compose, Firebase Auth/Firestore) — adds one new dependency (`androidx.lifecycle:lifecycle-runtime-compose`) for real lifecycle-aware state collection.

## Global Constraints

- **Not in scope** (see the spec for why): no new `firestore.rules` file, no approval-mode joining, no `FlatHomeScreen` visual redesign, no `minifyEnabled`/`shrinkResources`.
- **`viewModel()` calls must use `LocalViewModelStoreOwner.current` implicitly** (the default) — inside a `NavHost`'s `composable { }` block this automatically resolves to that destination's own `NavBackStackEntry`, which is what makes the ViewModel survive recomposition and configuration changes while still being cleared when the destination leaves the back stack. Do not pass an explicit `viewModelStoreOwner` unless a task says to.
- **`FlatHomeViewModel` needs a `key = flatId` parameter to `viewModel()`** — without it, navigating between two different flats would incorrectly reuse the first flat's ViewModel instance (this replaces the old `remember(flatId) { ... }` keying, which must have the same effect).
- **Account deletion is genuinely destructive** — the delete button must sit behind a confirmation dialog. Never call `deleteAccount()` directly from a button's `onClick`.
- **Build tooling**: Gradle 9.4.1 at `C:\Users\user\.gradle\wrapper\dists\gradle-9.4.1-bin\arn2x92ynaizyzdaamcbpbhtj\gradle-9.4.1\bin\gradle.bat`, `JAVA_HOME` must point at `C:\Program Files\Android\Android Studio\jbr`. Do not modify `gradle.properties` or `app/build.gradle.kts`'s `testOptions`/signing config sections — both are already correct and verified working across two prior plans; a past task lost real time to an implementer misdiagnosing a `gradle.properties` encoding bug as "insufficient memory," so report build errors exactly rather than touching JVM/heap settings.
- **The correct command to run a specific unit test class is `gradle testDebugUnitTest --tests "ClassName"`** — not `gradle test --tests`, which fails (`:app:test` is a lifecycle task, not a filterable `Test` task).

---

## File Structure

```
C:\habitiq_jaswanth\app\src\main\kotlin\habitiq\app\
  ui\
    ComposeUtil.kt              (MODIFY: real collectAsStateWithLifecycle)
    HomeScreen.kt                (MODIFY: remove sign-out, add settings icon)
    SettingsScreen.kt            (NEW)
  auth\
    AuthErrorMapper.kt            (MODIFY: add FirebaseAuthRecentLoginRequiredException case)
    AuthRepository.kt              (MODIFY: add deleteAccount())
  data\
    UsersRepository.kt              (MODIFY: add deleteUserData())
  settings\
    SettingsViewModel.kt              (NEW)
  HabitiqApp.kt                       (MODIFY: viewModel() factories, SETTINGS route)

app\src\test\kotlin\habitiq\app\auth\
  AuthErrorMapperTest.kt          (MODIFY: add a test case for the new mapping)

gradle\libs.versions.toml           (MODIFY: add lifecycle-runtime-compose)
app\build.gradle.kts                 (MODIFY: add the dependency)
```

Each file keeps its existing single responsibility: `ComposeUtil.kt` only wraps state collection, `AuthRepository`/`AuthErrorMapper` only handle Firebase Auth calls/errors, `UsersRepository` only touches the `users/{uid}` document, `SettingsViewModel`/`SettingsScreen` only handle the Settings feature.

---

### Task 1: Lifecycle-aware state collection

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `app/build.gradle.kts`
- Modify: `app/src/main/kotlin/habitiq/app/ui/ComposeUtil.kt`

**Interfaces:**
- Consumes: nothing new
- Produces: `collectAsStateWithLifecycleCompat()`'s behavior changes (same signature, same call sites — every screen that already uses it gets the fix automatically, no other files need editing)

- [ ] **Step 1: Add the dependency to `gradle/libs.versions.toml`**

Add to the `[libraries]` section, alongside `androidx-lifecycle-viewmodel-compose`:

```toml
androidx-lifecycle-runtime-compose = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose", version.ref = "lifecycle" }
```

(Uses the existing `lifecycle = "2.9.0"` version already defined in `[versions]` — no new version entry needed.)

- [ ] **Step 2: Add it to `app/build.gradle.kts`**

Add to the `dependencies { }` block, alongside `implementation(libs.androidx.lifecycle.viewmodel.compose)`:

```kotlin
    implementation(libs.androidx.lifecycle.runtime.compose)
```

- [ ] **Step 3: Rewrite `ComposeUtil.kt`**

Replace the entire file contents:

```kotlin
package habitiq.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.StateFlow

@Composable
fun <T> StateFlow<T>.collectAsStateWithLifecycleCompat(): State<T> = this.collectAsStateWithLifecycle()
```

- [ ] **Step 4: Build and verify**

Run: `gradle assembleDebug --project-dir "C:\habitiq_jaswanth"`
Expected: `BUILD SUCCESSFUL`. Every screen using `collectAsStateWithLifecycleCompat()` (Login, Signup, Home, CreateFlat, JoinFlat, FlatHome) now pauses collection when the app is backgrounded, with zero call-site changes.

- [ ] **Step 5: Commit**

```bash
git add gradle/libs.versions.toml app/build.gradle.kts app/src/main/kotlin/habitiq/app/ui/ComposeUtil.kt
git commit -m "fix: make collectAsStateWithLifecycleCompat actually lifecycle-aware"
```

---

### Task 2: ViewModel lifecycle fix (survive configuration changes)

**Files:**
- Modify: `app/src/main/kotlin/habitiq/app/HabitiqApp.kt`

**Interfaces:**
- Consumes: `viewModel()`/`viewModelFactory { initializer { } }` from `androidx.lifecycle.viewmodel.compose`/`androidx.lifecycle.viewmodel` (already available via the existing `lifecycle-viewmodel-compose` dependency, no new dependency needed for this task)
- Produces: no new public API — this is a pure internal-wiring change. Every existing nav route keeps its exact same behavior, just backed by a configuration-surviving ViewModel instead of one tied to Compose's `remember`.

- [ ] **Step 1: Replace the imports and every `composable { }` block in `HabitiqApp.kt`**

Read the current file first. Add these imports (alongside the existing ones):

```kotlin
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewmodel.initializer
```

**Corrected 2026-07-11 during Task 2's implementation**: the original text above had `viewModelFactory` under `.compose`, which doesn't exist. Only the `viewModel()` Composable function itself lives in `androidx.lifecycle.viewmodel.compose`; the `viewModelFactory { initializer { ... } }` DSL is a plain-Kotlin API in `androidx.lifecycle.viewmodel` (no `.compose`). Verified against the actual AndroidX Lifecycle jar during implementation and confirmed independently during review.

Replace each `remember { XViewModel(...) }` line with the `viewModel()` factory form. The full set of replacements, in place, one per existing composable block:

```kotlin
                composable(Routes.LOGIN) {
                    val viewModel: LoginViewModel = viewModel(
                        factory = viewModelFactory { initializer { LoginViewModel(authRepository, usersRepository) } }
                    )
                    LoginScreen(
                        viewModel = viewModel,
                        onSignedIn = { navController.navigate(Routes.HOME) { popUpTo(Routes.LOGIN) { inclusive = true } } },
                        onNavigateToSignup = { navController.navigate(Routes.SIGNUP) }
                    )
                }
                composable(Routes.SIGNUP) {
                    val viewModel: SignupViewModel = viewModel(
                        factory = viewModelFactory { initializer { SignupViewModel(authRepository, usersRepository) } }
                    )
                    SignupScreen(
                        viewModel = viewModel,
                        onSignedUp = { navController.navigate(Routes.HOME) { popUpTo(Routes.LOGIN) { inclusive = true } } }
                    )
                }
                composable(Routes.HOME) {
                    val homeViewModel: HomeViewModel = viewModel(
                        factory = viewModelFactory { initializer { HomeViewModel(authRepository, usersRepository) } }
                    )
                    HomeScreen(
                        user = currentUser,
                        homeViewModel = homeViewModel,
                        onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                        onCreateFlat = { navController.navigate(Routes.CREATE_FLAT) },
                        onJoinFlat = { navController.navigate(Routes.JOIN_FLAT) },
                        onViewFlat = { flatId -> navController.navigate(Routes.flatHome(flatId)) }
                    )
                }
                composable(Routes.CREATE_FLAT) {
                    val viewModel: CreateFlatViewModel = viewModel(
                        factory = viewModelFactory { initializer { CreateFlatViewModel(authRepository, flatsRepository) } }
                    )
                    CreateFlatScreen(
                        viewModel = viewModel,
                        onDone = { navController.popBackStack() }
                    )
                }
                composable(Routes.JOIN_FLAT) {
                    val viewModel: JoinFlatViewModel = viewModel(
                        factory = viewModelFactory { initializer { JoinFlatViewModel(authRepository, flatsRepository) } }
                    )
                    JoinFlatScreen(
                        viewModel = viewModel,
                        onJoined = { flatId ->
                            navController.navigate(Routes.flatHome(flatId)) {
                                popUpTo(Routes.HOME) { inclusive = false }
                            }
                        }
                    )
                }
                composable(
                    route = Routes.FLAT_HOME,
                    arguments = listOf(navArgument("flatId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val flatId = backStackEntry.arguments?.getString("flatId") ?: return@composable
                    val viewModel: FlatHomeViewModel = viewModel(
                        key = flatId,
                        factory = viewModelFactory { initializer { FlatHomeViewModel(flatId, flatsRepository, membersRepository) } }
                    )
                    FlatHomeScreen(viewModel = viewModel, currentUid = currentUser?.uid.orEmpty())
                }
```

**Important — this task changes `HomeScreen`'s call site to pass `onOpenSettings` instead of `onSignOut`, and references `Routes.SETTINGS`, which don't exist until Task 5.** To keep this task's build green on its own, ALSO make these two minimal additions now (Task 5 will build the real Settings screen on top of them):
1. Add `const val SETTINGS = "settings"` to the `Routes` object.
2. Temporarily change `HomeScreen`'s call above to keep compiling against the *current* (pre-Task-5) `HomeScreen.kt` signature — i.e., for THIS task only, replace `onOpenSettings = { navController.navigate(Routes.SETTINGS) }` with the current parameter name `onSignOut = { authRepository.signOut(); navController.navigate(Routes.LOGIN) { popUpTo(Routes.HOME) { inclusive = true } } }` (the exact same body as before). Task 5 will change both `HomeScreen.kt`'s signature and this call site together, in the same task, so they never go out of sync.

(This note exists so this task's `assembleDebug` succeeds in isolation. If you're executing this plan straight through without stopping between tasks, you may skip the temporary keep-`onSignOut`-for-now step and go directly to the final `onOpenSettings` form — just make sure Task 5 lands in the same session before anyone tries to build in between.)

- [ ] **Step 2: Build and verify**

Run: `gradle assembleDebug --project-dir "C:\habitiq_jaswanth"`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/kotlin/habitiq/app/HabitiqApp.kt
git commit -m "fix: use viewModel() factories instead of remember{} so ViewModels survive configuration changes"
```

---

### Task 3: AuthRepository.deleteAccount()

**Files:**
- Modify: `app/src/main/kotlin/habitiq/app/auth/AuthErrorMapper.kt`
- Modify: `app/test/kotlin/habitiq/app/auth/AuthErrorMapperTest.kt` (path as it actually exists: `app/src/test/kotlin/habitiq/app/auth/AuthErrorMapperTest.kt`)
- Modify: `app/src/main/kotlin/habitiq/app/auth/AuthRepository.kt`

**Interfaces:**
- Produces: `AuthRepository.deleteAccount(): Result<Unit>` — consumed by Task 5's `SettingsViewModel`.

- [ ] **Step 1: Read the current `AuthErrorMapper.kt` and write the failing test first**

Add this test case to the existing `AuthErrorMapperTest.kt` (inside the existing `class AuthErrorMapperTest`, alongside the other `@Test` methods — do not remove any existing test):

```kotlin
    @Test
    fun `recent login required maps to a plain re-authenticate message`() {
        val exception = FirebaseAuthRecentLoginRequiredException("ERROR_REQUIRES_RECENT_LOGIN", "stale session")
        assertEquals(
            "Please sign out and sign back in, then try deleting your account again.",
            mapAuthError(exception)
        )
    }
```

Add the import: `import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException`

- [ ] **Step 2: Run test to verify it fails**

Run: `gradle testDebugUnitTest --tests "habitiq.app.auth.AuthErrorMapperTest"`
Expected: FAIL (either "no test case satisfies" or an actual assertion mismatch, since this exact message case doesn't exist in `mapAuthError` yet).

- [ ] **Step 3: Add the case to `mapAuthError`**

**Verify the exception class hierarchy before placing this branch** — a prior task in this project found that `FirebaseAuthWeakPasswordException` is actually a subclass of `FirebaseAuthInvalidCredentialsException` in the real Firebase SDK, which silently broke a `when` that checked the parent type first. Before assuming `FirebaseAuthRecentLoginRequiredException` is a plain sibling of the other `FirebaseAuthException` subtypes already in this `when`, check its actual superclass (e.g. via your IDE's "go to definition," or by checking whether adding it as the FIRST branch changes any existing test's result — if it does, that's evidence of an inheritance relationship worth investigating further). Given the uncertainty, add it as the **first** branch in the `when` (the safest position — if it turns out to be an unrelated sibling, order doesn't matter; if it turns out to be a superclass of something else, being first would be wrong, so if you discover that, stop and report it rather than guessing):

```kotlin
fun mapAuthError(exception: Exception): String = when (exception) {
    is FirebaseAuthRecentLoginRequiredException -> "Please sign out and sign back in, then try deleting your account again."
    is FirebaseAuthWeakPasswordException -> "Password is too weak — use at least 6 characters."
    is FirebaseAuthInvalidCredentialsException -> "Incorrect email or password."
    is FirebaseAuthInvalidUserException -> "No account found with this email."
    is FirebaseAuthUserCollisionException -> "An account already exists with this email."
    is IOException -> "No internet connection. Please try again."
    else -> "Something went wrong. Please try again."
}
```

Add the import: `import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException`

- [ ] **Step 4: Run test to verify it passes**

Run: `gradle testDebugUnitTest --tests "habitiq.app.auth.AuthErrorMapperTest"`
Expected: PASS, all test cases green (the new one plus every pre-existing one — confirms nothing regressed).

- [ ] **Step 5: Add `deleteAccount()` to `AuthRepository.kt`**

Add this method inside the existing `AuthRepository` class, alongside `signOut()`:

```kotlin
    suspend fun deleteAccount(): Result<Unit> = runCatching {
        firebaseAuth.currentUser?.delete()?.await()
        Unit
    }.recoverCatching { throw IllegalStateException(mapAuthError(it as Exception), it) }
```

(No new imports needed — `firebaseAuth`, `runCatching`, `.await()`, and `mapAuthError` are all already used elsewhere in this file.)

- [ ] **Step 6: Build and verify**

Run: `gradle assembleDebug --project-dir "C:\habitiq_jaswanth"`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 7: Commit**

```bash
git add app/src/main/kotlin/habitiq/app/auth/AuthErrorMapper.kt app/src/test/kotlin/habitiq/app/auth/AuthErrorMapperTest.kt app/src/main/kotlin/habitiq/app/auth/AuthRepository.kt
git commit -m "feat: add AuthRepository.deleteAccount() with recent-login-required handling"
```

---

### Task 4: UsersRepository.deleteUserData()

**Files:**
- Modify: `app/src/main/kotlin/habitiq/app/data/UsersRepository.kt`

**Interfaces:**
- Produces: `UsersRepository.deleteUserData(uid: String): Result<Unit>` — consumed by Task 5's `SettingsViewModel`.

- [ ] **Step 1: Add the method**

Add inside the existing `UsersRepository` class, alongside `ensureUserDocument`/`getActiveFlatId`:

```kotlin
    suspend fun deleteUserData(uid: String): Result<Unit> = runCatching {
        firestore.collection("users").document(uid).delete().await()
        Unit
    }
```

- [ ] **Step 2: Build and verify**

Run: `gradle assembleDebug --project-dir "C:\habitiq_jaswanth"`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/kotlin/habitiq/app/data/UsersRepository.kt
git commit -m "feat: add UsersRepository.deleteUserData() for basic account-deletion cleanup"
```

(This deletes only the top-level `users/{uid}` document — deep cleanup of a deleted user's flat memberships/activity logs needs a Cloud Function, since the client can't act as that user anymore right after their Auth account is deleted. Explicitly out of scope, matching the spec.)

---

### Task 5: Settings screen + nav wiring

**Files:**
- Create: `app/src/main/kotlin/habitiq/app/settings/SettingsViewModel.kt`
- Create: `app/src/main/kotlin/habitiq/app/ui/SettingsScreen.kt`
- Modify: `app/src/main/kotlin/habitiq/app/ui/HomeScreen.kt`
- Modify: `app/src/main/kotlin/habitiq/app/HabitiqApp.kt`

**Interfaces:**
- Consumes: `AuthRepository.deleteAccount()` (Task 3), `UsersRepository.deleteUserData()` (Task 4), `AuthRepository.signOut()`/`currentUser` (existing)
- Produces: `class SettingsViewModel(...)` exposing `val deleteState: StateFlow<DeleteAccountState>`, `fun deleteAccount()`. `HomeScreen`'s signature changes: `onSignOut: () -> Unit` is replaced with `onOpenSettings: () -> Unit`.

- [ ] **Step 1: Create `SettingsViewModel.kt`**

```kotlin
package habitiq.app.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import habitiq.app.auth.AuthRepository
import habitiq.app.data.UsersRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface DeleteAccountState {
    data object Idle : DeleteAccountState
    data object Deleting : DeleteAccountState
    data class Error(val message: String) : DeleteAccountState
    data object Deleted : DeleteAccountState
}

class SettingsViewModel(
    private val authRepository: AuthRepository,
    private val usersRepository: UsersRepository
) : ViewModel() {

    private val _deleteState = MutableStateFlow<DeleteAccountState>(DeleteAccountState.Idle)
    val deleteState: StateFlow<DeleteAccountState> = _deleteState

    fun deleteAccount() {
        val user = authRepository.currentUser.value
        if (user == null) {
            _deleteState.value = DeleteAccountState.Error("You must be signed in.")
            return
        }
        val uid = user.uid
        _deleteState.value = DeleteAccountState.Deleting
        viewModelScope.launch {
            authRepository.deleteAccount().fold(
                onSuccess = {
                    // Best-effort cleanup after the Auth account is gone -- a failure here
                    // shouldn't block the user from completing account deletion, since the
                    // Auth account (the part they actually asked to delete) already succeeded.
                    usersRepository.deleteUserData(uid)
                    _deleteState.value = DeleteAccountState.Deleted
                },
                onFailure = { error ->
                    _deleteState.value = DeleteAccountState.Error(
                        error.message ?: "Something went wrong. Please try again."
                    )
                }
            )
        }
    }
}
```

- [ ] **Step 2: Create `SettingsScreen.kt`**

```kotlin
package habitiq.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseUser
import habitiq.app.settings.DeleteAccountState
import habitiq.app.settings.SettingsViewModel

@Composable
fun SettingsScreen(
    user: FirebaseUser?,
    viewModel: SettingsViewModel,
    onSignOut: () -> Unit,
    onAccountDeleted: () -> Unit
) {
    var showConfirmDialog by remember { mutableStateOf(false) }
    val deleteState by viewModel.deleteState.collectAsStateWithLifecycleCompat()

    LaunchedEffect(deleteState) {
        if (deleteState is DeleteAccountState.Deleted) {
            onAccountDeleted()
        }
    }

    Column(modifier = Modifier.padding(24.dp)) {
        Text("Settings")
        Text(user?.email ?: "unknown")
        Spacer(Modifier.height(24.dp))
        Button(onClick = onSignOut) {
            Text("Sign out")
        }
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = { showConfirmDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB3261E))
        ) {
            Text("Delete Account")
        }
        when (val current = deleteState) {
            is DeleteAccountState.Deleting -> Text("Deleting your account…")
            is DeleteAccountState.Error -> Text(current.message)
            else -> {}
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Delete your account?") },
            text = { Text("This permanently deletes your account. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    viewModel.deleteAccount()
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
```

- [ ] **Step 3: Replace `HomeScreen.kt` entirely**

```kotlin
package habitiq.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseUser
import habitiq.app.flats.HomeFlatStatus
import habitiq.app.flats.HomeViewModel

@Composable
fun HomeScreen(
    user: FirebaseUser?,
    homeViewModel: HomeViewModel,
    onOpenSettings: () -> Unit,
    onCreateFlat: () -> Unit,
    onJoinFlat: () -> Unit,
    onViewFlat: (flatId: String) -> Unit
) {
    val flatStatus by homeViewModel.flatStatus.collectAsStateWithLifecycleCompat()

    Column(modifier = Modifier.padding(24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Signed in as: ${user?.email ?: "unknown"}")
            IconButton(onClick = onOpenSettings) {
                Icon(imageVector = Icons.Filled.Settings, contentDescription = "Settings")
            }
        }
        when (val status = flatStatus) {
            is HomeFlatStatus.Loading -> Text("Checking your flats…")
            is HomeFlatStatus.NoFlat -> {
                Text("What brings you here today?")
                Button(onClick = onCreateFlat) {
                    Text("Create a Flat")
                }
                Button(onClick = onJoinFlat) {
                    Text("Join with Code")
                }
            }
            is HomeFlatStatus.InFlat -> {
                Button(onClick = { onViewFlat(status.flatId) }) {
                    Text("View my flat")
                }
            }
            is HomeFlatStatus.Error -> {
                Text(status.message)
                Button(onClick = { homeViewModel.checkFlatStatus() }) {
                    Text("Retry")
                }
            }
        }
    }
}
```

- [ ] **Step 4: Update `HabitiqApp.kt`**

Add `const val SETTINGS = "settings"` to the `Routes` object (if Task 2 didn't already add it as its temporary measure).

Add these imports:

```kotlin
import habitiq.app.data.UsersRepository
import habitiq.app.settings.SettingsViewModel
import habitiq.app.ui.SettingsScreen
```

(`UsersRepository` is likely already imported from earlier tasks — don't duplicate the import if so.)

Replace the `Routes.HOME` composable's `onOpenSettings` (or `onSignOut`, if Task 2 used the temporary form) so it navigates to Settings instead of signing out directly:

```kotlin
                composable(Routes.HOME) {
                    val homeViewModel: HomeViewModel = viewModel(
                        factory = viewModelFactory { initializer { HomeViewModel(authRepository, usersRepository) } }
                    )
                    HomeScreen(
                        user = currentUser,
                        homeViewModel = homeViewModel,
                        onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                        onCreateFlat = { navController.navigate(Routes.CREATE_FLAT) },
                        onJoinFlat = { navController.navigate(Routes.JOIN_FLAT) },
                        onViewFlat = { flatId -> navController.navigate(Routes.flatHome(flatId)) }
                    )
                }
```

Add a new `composable(Routes.SETTINGS) { ... }` block (placed after the `Routes.HOME` block, order among routes doesn't otherwise matter):

```kotlin
                composable(Routes.SETTINGS) {
                    val viewModel: SettingsViewModel = viewModel(
                        factory = viewModelFactory { initializer { SettingsViewModel(authRepository, usersRepository) } }
                    )
                    SettingsScreen(
                        user = currentUser,
                        viewModel = viewModel,
                        onSignOut = {
                            authRepository.signOut()
                            navController.navigate(Routes.LOGIN) { popUpTo(Routes.HOME) { inclusive = true } }
                        },
                        onAccountDeleted = {
                            authRepository.signOut()
                            navController.navigate(Routes.LOGIN) { popUpTo(Routes.HOME) { inclusive = true } }
                        }
                    )
                }
```

(`onAccountDeleted` also calls `signOut()` defensively — the Firebase Auth account is already gone at that point, but this clears any locally-cached auth state and guarantees `authRepository.currentUser` becomes `null`, so the app doesn't briefly show a signed-in UI for a user that no longer exists.)

- [ ] **Step 5: Build and verify**

Run: `gradle assembleDebug --project-dir "C:\habitiq_jaswanth"`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/kotlin/habitiq/app/settings/SettingsViewModel.kt app/src/main/kotlin/habitiq/app/ui/SettingsScreen.kt app/src/main/kotlin/habitiq/app/ui/HomeScreen.kt app/src/main/kotlin/habitiq/app/HabitiqApp.kt
git commit -m "feat: add Settings screen with sign-out and account deletion"
```

---

### Task 6: Manual on-device verification, then tag

No new source files. Verifies the fixes actually work, using the real device/emulator setup already used for prior plans in this project.

- [ ] **Step 1: Install the current build**

Run: `gradle assembleDebug --project-dir "C:\habitiq_jaswanth"` then `adb install -r app/build/outputs/apk/debug/app-debug.apk`.

- [ ] **Step 2: Manual test — ViewModel survives rotation**

Sign in, navigate to Home. Rotate the device/emulator (or toggle auto-rotate and turn the screen sideways). Expected: the screen does NOT show a loading flicker or reset — `HomeViewModel`'s `flatStatus` should already be resolved and stay resolved, since the ViewModel isn't recreated. (Before this fix, rotating would have re-run `checkFlatStatus()` from scratch.)

- [ ] **Step 3: Manual test — Settings navigation and sign out**

From Home, tap the gear icon. Expected: navigates to Settings, showing the signed-in email. Tap "Sign out." Expected: returns to Login, same as the old inline sign-out button used to.

- [ ] **Step 4: Manual test — account deletion**

Sign in with a throwaway test account created earlier in this project's testing (not a real account you want to keep). Navigate to Settings, tap "Delete Account." Expected: a confirmation dialog appears. Tap "Delete." Expected: shows "Deleting your account…" briefly, then returns to Login. Verify in Firebase Console → Authentication: the user is gone. Verify in Firestore: `users/{uid}` document is gone.

- [ ] **Step 5: Manual test — recent-login-required path (best effort)**

This is hard to trigger reliably on demand (Firebase's exact staleness window isn't user-controlled), so treat this as best-effort: if a "Please sign out and sign back in, then try deleting your account again" message is ever seen during real usage instead of a crash, that confirms the mapping works. Do not block sign-off on reproducing this specific case.

- [ ] **Step 6: Tag this milestone**

```bash
git tag -a v0.3.0-lifecycle-and-account-deletion -m "ViewModel lifecycle fix, lifecycle-aware state collection, and account deletion, verified on-device"
```

---

## Self-Review Notes

- **Spec coverage:** ViewModel lifecycle fix ✅ Task 2. Lifecycle-aware collection ✅ Task 1. Account deletion (AuthRepository, UsersRepository, SettingsScreen, nav) ✅ Tasks 3-5. Explicitly-rejected items (firestore.rules, approval flow, dashboard redesign, minification) correctly absent from every task.
- **Placeholder scan:** none found.
- **Type consistency:** `HomeScreen`'s `onSignOut: () -> Unit` → `onOpenSettings: () -> Unit` signature change is made consistently in both the Task 5 `HomeScreen.kt` rewrite and the `HabitiqApp.kt` call site update in the same task — no task leaves them mismatched. `SettingsViewModel.deleteState`/`DeleteAccountState` shape matches what `SettingsScreen.kt` pattern-matches against exactly.
- **Known ambiguity flagged rather than guessed at:** Task 3's Step 3 explicitly tells the implementer to verify `FirebaseAuthRecentLoginRequiredException`'s real superclass rather than assuming it's a plain `FirebaseAuthException` sibling, given this exact class of bug (`FirebaseAuthWeakPasswordException` subclassing `FirebaseAuthInvalidCredentialsException`) was already found once in this project.
- **Cross-task sequencing note:** Task 2 introduces a reference to `Routes.SETTINGS`/`onOpenSettings` before Task 5 builds the real Settings screen. The plan handles this explicitly (Task 2's Step 1 includes a fallback so it builds green in isolation) rather than leaving a silent gap — flagged inline in the task itself, not left implicit.
