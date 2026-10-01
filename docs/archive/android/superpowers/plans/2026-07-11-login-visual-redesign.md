# Login/Signup/Home Visual Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix the stray Android ActionBar bug, apply the real `DESIGN.md` brand tokens to Login/Signup/Home (currently bare-bones default Material3), and live-diagnose + fix the reported Google Sign-In "Something went wrong" error.

**Architecture:** Same as `CreateFlatScreen`/`JoinFlatScreen`/`FlatOnboardingHeader` — screens define their own explicit `Color(0xFF......)` values rather than relying on `MaterialTheme.colorScheme`, now centralized in one shared `HabitiqBrand` object so all screens reference the same token set instead of each screen re-declaring hex literals.

**Tech Stack:** Same project, no new dependencies.

## Global Constraints

- **Button Law (from `DESIGN.md`, non-negotiable):** primary buttons use violet fill (`#7C3AED`) with **dark** text (`#0C0B0F`) — never white text on a violet primary button.
- **No changes to Login/Signup/Home's business logic** — `LoginViewModel`/`SignupViewModel`/`HomeViewModel` and all their existing method calls, state handling, and navigation callbacks (`onSignedIn`, `onNavigateToSignup`, `onSignedUp`, `onOpenSettings`, `onCreateFlat`, `onJoinFlat`, `onViewFlat`) stay wired exactly as they are. This plan is visual/theme only, plus one live-diagnosed bug fix in Task 5.
- **Task 5 does not have a pre-written fix.** The Google Sign-In error's root cause is unknown until a live `logcat` capture during reproduction. Follow the systematic-debugging process (Phase 1: gather evidence, Phase 2: compare against the working email/password path, Phase 3: single hypothesis + minimal test, Phase 4: fix once confirmed) — do not guess-patch based on the task list below.
- **Build tooling**: Gradle 9.4.1 at `C:\Users\user\.gradle\wrapper\dists\gradle-9.4.1-bin\arn2x92ynaizyzdaamcbpbhtj\gradle-9.4.1\bin\gradle.bat`, `JAVA_HOME` must point at `C:\Program Files\Android\Android Studio\jbr`. Do not modify `gradle.properties` or `app/build.gradle.kts`'s `testOptions`/signing config.

---

## File Structure

```
C:\habitiq_jaswanth\app\src\main\
  res\values\styles.xml                          (MODIFY: NoActionBar + dark window background)
  kotlin\habitiq\app\ui\theme\
    BrandColors.kt                                 (NEW: shared DESIGN.md token values)
  kotlin\habitiq\app\ui\
    LoginScreen.kt                                  (MODIFY: full visual rewrite)
    SignupScreen.kt                                  (MODIFY: full visual rewrite)
    HomeScreen.kt                                     (MODIFY: restyle only, logic untouched)
```

---

### Task 1: Fix the ActionBar bug + shared brand color tokens

**Files:**
- Modify: `app/src/main/res/values/styles.xml`
- Create: `app/src/main/kotlin/habitiq/app/ui/theme/BrandColors.kt`

**Interfaces:**
- Produces: `object HabitiqBrand` with `Canvas`, `Ink`, `InkMute`, `Primary`, `PrimarySoft`, `OnPrimary`, `InputBackground`, `InputBorder`, `Error`, `SecondaryFill`, `SecondaryText` — consumed by Tasks 2-4.

- [ ] **Step 1: Fix `styles.xml`**

Replace the entire file:

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.Habitiq" parent="android:Theme.Material.Light.NoActionBar">
        <item name="android:windowBackground">#FF0C0B0F</item>
    </style>
</resources>
```

(`NoActionBar` removes the stray system title bar showing "Habitiq" that was covering the top of every screen. The dark `windowBackground` prevents a light flash before Compose's first frame draws.)

- [ ] **Step 2: Create `BrandColors.kt`**

```kotlin
package habitiq.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Shared color tokens, pulled directly from C:\garbage\DESIGN.md (the established
 * Habitiq brand system) -- not invented here. See DESIGN.md's "Button Law": primary
 * buttons always use dark text (OnPrimary) on the violet Primary fill, never white.
 */
object HabitiqBrand {
    val Canvas = Color(0xFF0C0B0F)
    val Ink = Color(0xFFF4F3F8)
    val InkMute = Color(0xFF514E61)
    val Primary = Color(0xFF7C3AED)
    val PrimarySoft = Color(0xFFA78BFA)
    val OnPrimary = Color(0xFF0C0B0F)
    val InputBackground = Color(0xFF1A1820)
    val InputBorder = Color(0xFF2A2635)
    val Error = Color(0xFFEF4444)
    val SecondaryFill = Color(0xFFF4F3F8)
    val SecondaryText = Color(0xFF0C0B0F)
}
```

- [ ] **Step 3: Build and verify**

Run: `gradle assembleDebug --project-dir "C:\habitiq_jaswanth"`
Expected: `BUILD SUCCESSFUL`. (No screens use `HabitiqBrand` yet — that's Tasks 2-4 — so this task just needs to compile and the ActionBar fix needs a manual look, which Task 5 covers on-device.)

- [ ] **Step 4: Commit**

```bash
git add app/src/main/res/values/styles.xml app/src/main/kotlin/habitiq/app/ui/theme/BrandColors.kt
git commit -m "fix: remove stray ActionBar, add shared DESIGN.md brand color tokens"
```

---

### Task 2: Redesign LoginScreen

**Files:**
- Modify: `app/src/main/kotlin/habitiq/app/ui/LoginScreen.kt`

**Interfaces:**
- Consumes: `HabitiqBrand` (Task 1)
- Produces: no signature change — `LoginScreen(viewModel, onSignedIn, onNavigateToSignup)` stays identical, so `HabitiqApp.kt`'s call site needs no changes.

- [ ] **Step 1: Replace the entire file**

```kotlin
package habitiq.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.exceptions.GetCredentialException
import habitiq.app.R
import habitiq.app.auth.AuthUiState
import habitiq.app.auth.LoginViewModel
import habitiq.app.auth.launchGoogleSignIn
import habitiq.app.auth.mapAuthError
import habitiq.app.ui.theme.HabitiqBrand
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onSignedIn: () -> Unit,
    onNavigateToSignup: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val state by viewModel.state.collectAsStateWithLifecycleCompat()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val webClientId = stringResource(R.string.google_web_client_id)

    LaunchedEffect(state) {
        if (state is AuthUiState.Success) {
            onSignedIn()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HabitiqBrand.Canvas)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Spacer(Modifier.height(32.dp))
        Text("Habitiq", color = HabitiqBrand.Ink, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text(
            "Split expenses. Manage chores. Run your shared home.",
            color = HabitiqBrand.InkMute,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
        )

        Text("Log in", color = HabitiqBrand.Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = HabitiqBrand.InputBackground,
                unfocusedContainerColor = HabitiqBrand.InputBackground,
                focusedTextColor = HabitiqBrand.Ink,
                unfocusedTextColor = HabitiqBrand.Ink,
                focusedBorderColor = HabitiqBrand.Primary,
                unfocusedBorderColor = HabitiqBrand.InputBorder,
                focusedLabelColor = HabitiqBrand.Primary,
                unfocusedLabelColor = HabitiqBrand.InkMute,
                cursorColor = HabitiqBrand.Primary
            )
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = HabitiqBrand.InputBackground,
                unfocusedContainerColor = HabitiqBrand.InputBackground,
                focusedTextColor = HabitiqBrand.Ink,
                unfocusedTextColor = HabitiqBrand.Ink,
                focusedBorderColor = HabitiqBrand.Primary,
                unfocusedBorderColor = HabitiqBrand.InputBorder,
                focusedLabelColor = HabitiqBrand.Primary,
                unfocusedLabelColor = HabitiqBrand.InkMute,
                cursorColor = HabitiqBrand.Primary
            )
        )
        Spacer(Modifier.height(20.dp))

        Button(
            onClick = { viewModel.signInWithEmail(email, password) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = HabitiqBrand.Primary,
                contentColor = HabitiqBrand.OnPrimary
            )
        ) {
            Text("Log in")
        }
        Spacer(Modifier.height(12.dp))
        TextButton(
            onClick = onNavigateToSignup,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Need an account? Sign up", color = HabitiqBrand.Ink)
        }
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = {
                coroutineScope.launch {
                    try {
                        val idToken = launchGoogleSignIn(context, webClientId)
                        if (idToken != null) {
                            viewModel.signInWithGoogleIdToken(idToken)
                        }
                    } catch (e: GetCredentialException) {
                        viewModel.onGoogleSignInFailed(mapAuthError(e))
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = HabitiqBrand.SecondaryFill,
                contentColor = HabitiqBrand.SecondaryText
            )
        ) {
            Text("Sign in with Google")
        }

        Spacer(Modifier.height(16.dp))
        when (val current = state) {
            is AuthUiState.Loading -> Text("Signing in…", color = HabitiqBrand.InkMute)
            is AuthUiState.Error -> Text(current.message, color = HabitiqBrand.Error)
            else -> {}
        }
    }
}
```

- [ ] **Step 2: Build and verify**

Run: `gradle assembleDebug --project-dir "C:\habitiq_jaswanth"`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/kotlin/habitiq/app/ui/LoginScreen.kt
git commit -m "feat: apply DESIGN.md brand tokens to LoginScreen"
```

---

### Task 3: Redesign SignupScreen

**Files:**
- Modify: `app/src/main/kotlin/habitiq/app/ui/SignupScreen.kt`

**Interfaces:**
- Consumes: `HabitiqBrand` (Task 1)
- Produces: no signature change — `SignupScreen(viewModel, onSignedUp)` stays identical.

- [ ] **Step 1: Replace the entire file**

```kotlin
package habitiq.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import habitiq.app.auth.AuthUiState
import habitiq.app.auth.SignupViewModel
import habitiq.app.ui.theme.HabitiqBrand

@Composable
fun SignupScreen(
    viewModel: SignupViewModel,
    onSignedUp: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val state by viewModel.state.collectAsStateWithLifecycleCompat()

    LaunchedEffect(state) {
        if (state is AuthUiState.Success) {
            onSignedUp()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HabitiqBrand.Canvas)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Spacer(Modifier.height(32.dp))
        Text("Habitiq", color = HabitiqBrand.Ink, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text(
            "Split expenses. Manage chores. Run your shared home.",
            color = HabitiqBrand.InkMute,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
        )

        Text("Create your account", color = HabitiqBrand.Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = HabitiqBrand.InputBackground,
                unfocusedContainerColor = HabitiqBrand.InputBackground,
                focusedTextColor = HabitiqBrand.Ink,
                unfocusedTextColor = HabitiqBrand.Ink,
                focusedBorderColor = HabitiqBrand.Primary,
                unfocusedBorderColor = HabitiqBrand.InputBorder,
                focusedLabelColor = HabitiqBrand.Primary,
                unfocusedLabelColor = HabitiqBrand.InkMute,
                cursorColor = HabitiqBrand.Primary
            )
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password (min 6 characters)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = HabitiqBrand.InputBackground,
                unfocusedContainerColor = HabitiqBrand.InputBackground,
                focusedTextColor = HabitiqBrand.Ink,
                unfocusedTextColor = HabitiqBrand.Ink,
                focusedBorderColor = HabitiqBrand.Primary,
                unfocusedBorderColor = HabitiqBrand.InputBorder,
                focusedLabelColor = HabitiqBrand.Primary,
                unfocusedLabelColor = HabitiqBrand.InkMute,
                cursorColor = HabitiqBrand.Primary
            )
        )
        Spacer(Modifier.height(20.dp))

        Button(
            onClick = { viewModel.signUpWithEmail(email, password) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = HabitiqBrand.Primary,
                contentColor = HabitiqBrand.OnPrimary
            )
        ) {
            Text("Sign up")
        }

        Spacer(Modifier.height(16.dp))
        when (val current = state) {
            is AuthUiState.Loading -> Text("Creating account…", color = HabitiqBrand.InkMute)
            is AuthUiState.Error -> Text(current.message, color = HabitiqBrand.Error)
            else -> {}
        }
    }
}
```

- [ ] **Step 2: Build and verify**

Run: `gradle assembleDebug --project-dir "C:\habitiq_jaswanth"`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/kotlin/habitiq/app/ui/SignupScreen.kt
git commit -m "feat: apply DESIGN.md brand tokens to SignupScreen"
```

---

### Task 4: Restyle HomeScreen

**Files:**
- Modify: `app/src/main/kotlin/habitiq/app/ui/HomeScreen.kt`

**Interfaces:**
- Consumes: `HabitiqBrand` (Task 1)
- Produces: no signature change — same 6 parameters, same callbacks, same `HomeFlatStatus` branches. Only visual styling changes.

- [ ] **Step 1: Replace the entire file**

```kotlin
package habitiq.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseUser
import habitiq.app.flats.HomeFlatStatus
import habitiq.app.flats.HomeViewModel
import habitiq.app.ui.theme.HabitiqBrand

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HabitiqBrand.Canvas)
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Signed in as: ${user?.email ?: "unknown"}", color = HabitiqBrand.Ink)
            IconButton(onClick = onOpenSettings) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Settings",
                    tint = HabitiqBrand.Ink
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        when (val status = flatStatus) {
            is HomeFlatStatus.Loading -> Text("Checking your flats…", color = HabitiqBrand.InkMute)
            is HomeFlatStatus.NoFlat -> {
                Text(
                    "What brings you here today?",
                    color = HabitiqBrand.Ink,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onCreateFlat,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HabitiqBrand.Primary,
                        contentColor = HabitiqBrand.OnPrimary
                    )
                ) {
                    Text("Create a Flat")
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onJoinFlat,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HabitiqBrand.SecondaryFill,
                        contentColor = HabitiqBrand.SecondaryText
                    )
                ) {
                    Text("Join with Code")
                }
            }
            is HomeFlatStatus.InFlat -> {
                Button(
                    onClick = { onViewFlat(status.flatId) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HabitiqBrand.Primary,
                        contentColor = HabitiqBrand.OnPrimary
                    )
                ) {
                    Text("View my flat")
                }
            }
            is HomeFlatStatus.Error -> {
                Text(status.message, color = HabitiqBrand.Error)
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { homeViewModel.checkFlatStatus() },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HabitiqBrand.Primary,
                        contentColor = HabitiqBrand.OnPrimary
                    )
                ) {
                    Text("Retry")
                }
            }
        }
    }
}
```

- [ ] **Step 2: Build and verify**

Run: `gradle assembleDebug --project-dir "C:\habitiq_jaswanth"`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/kotlin/habitiq/app/ui/HomeScreen.kt
git commit -m "feat: apply DESIGN.md brand tokens to HomeScreen"
```

---

### Task 5: Live-diagnose the Google Sign-In error, fix it, verify everything on-device, then tag

**No pre-written fix.** This task follows the systematic-debugging process live, using the actual device. Do not skip to a guessed fix.

- [ ] **Step 1: Install the current build**

Run: `gradle assembleDebug --project-dir "C:\habitiq_jaswanth"` then `adb install -r app/build/outputs/apk/debug/app-debug.apk`.

- [ ] **Step 2: Confirm the ActionBar fix visually**

Launch the app. Expected: no gray system title bar — the dark Compose Login screen fills the whole display from the very top (below the status bar), matching the "Habitiq" wordmark + tagline design.

- [ ] **Step 3: Phase 1 — reproduce the Google Sign-In error with logcat capturing**

Start log capture BEFORE reproducing:

```bash
adb logcat -c
adb logcat *:E AndroidRuntime:E FirebaseAuth:V Credential:V habitiq:V > google-signin-repro.log &
```

Then on the device: tap "Sign in with Google," pick an account, observe the error. Stop the log capture and read `google-signin-repro.log` in full — do not skip past warnings. Look specifically for:
- Any `FirebaseAuthException` subtype name and message
- Any `GetCredentialException` subtype name and message
- Any stack trace pointing at `habitiq.app.auth.GoogleSignInLauncher`, `AuthRepository`, or `LoginViewModel`

- [ ] **Step 4: Phase 2 — compare against the working email/password path**

Email/password sign-in already works (confirmed in an earlier session). Compare: does Google Sign-In use a different Firebase project, different credential type, or hit a different code path that email/password doesn't? (It does — `signInWithCredential(GoogleAuthProvider.getCredential(idToken, null))` vs `signInWithEmailAndPassword`.) Check whether the specific Google account picked has any unusual state (e.g., was `saijaswanthedupuganti@gmail.com` or another account from the picker ever used to create an email/password account in this Firebase project already? If so, `FirebaseAuthUserCollisionException` or an account-exists-with-different-credential error is a real, specific hypothesis to test — check the actual log output from Step 3 to confirm or rule this out rather than assuming).

- [ ] **Step 5: Phase 3 — form one hypothesis, test it minimally**

Based on the actual logcat evidence (not a guess), state the hypothesis explicitly (e.g., "the error is X because the log shows Y"). Make the smallest possible change to test it.

- [ ] **Step 6: Phase 4 — implement the real fix**

Once the hypothesis is confirmed, implement the fix. If it's an account-collision case, the correct fix is almost certainly surfacing a clear, specific error message (e.g., "This Google account is linked to a different sign-in method — try logging in with your password instead," matching `mapAuthError`'s existing pattern) rather than silently linking credentials, since credential linking is a larger feature decision Sai hasn't made yet — do not implement automatic account linking without checking first if that's what the evidence points to needing.

Commit the fix separately from the visual tasks above, with a message describing the actual root cause found:

```bash
git add <files changed>
git commit -m "fix: <actual root cause found, from logcat evidence>"
```

- [ ] **Step 7: Full manual verification pass**

With the fix in place, re-verify the complete flow: sign up with email/password (new account), sign out via Settings, sign back in, wrong password shows a clear error, Google Sign-In completes successfully end-to-end, Create a Flat shows the illustrated onboarding correctly, share the invite code, join with a second account, rotate the device on Home (state should survive, per the earlier ViewModel lifecycle fix), delete a throwaway test account via Settings.

- [ ] **Step 8: Tag this milestone**

```bash
git tag -a v0.4.0-visual-redesign-and-signin-fix -m "Login/Signup/Home visual redesign, ActionBar fix, Google Sign-In error root-caused and fixed"
```

---

## Self-Review Notes

- **Spec coverage:** ActionBar fix ✅ Task 1. Real DESIGN.md tokens (not invented) ✅ Task 1's `BrandColors.kt`, applied in Tasks 2-4. Button Law honored ✅ every primary button uses `OnPrimary` (dark) text on `Primary` (violet) fill, never white-on-violet. Google Sign-In bug ✅ Task 5, explicitly without a pre-written fix, per the spec's explicit deferral.
- **Placeholder scan:** none found (Task 5's lack of a pre-written fix is intentional per the spec, not an oversight — flagged explicitly in the task text itself, not left silent).
- **Type/signature consistency:** all three screens (`LoginScreen`, `SignupScreen`, `HomeScreen`) keep their exact existing function signatures — confirmed no call-site changes needed in `HabitiqApp.kt` for Tasks 2-4.
- **Business logic untouched, confirmed:** every `onClick`/`LaunchedEffect`/state-branch in the rewritten screens calls the exact same ViewModel methods and navigation callbacks as before — only `Modifier`, `colors`, `shape`, and added `Text`/`Spacer` elements changed.
