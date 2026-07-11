# Flats/Members Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let a signed-in user create a flat (becoming its admin), see and share its invite code via the native Android share sheet, let a second user join that flat by entering the code, and show both users a simple member list — all backed by the same production Firestore data and rules the web app already uses.

**Architecture:** Same shape as the Foundation & Authentication plan — `FlatsRepository` and `MembersRepository` own Firestore calls and business logic for this feature; ViewModels hold screen state; Composable screens render state and forward actions. `C:\garbage\lib\flatService.ts` is the source of truth being ported faithfully, not redesigned.

**Tech Stack:** Same as the existing project (Kotlin, Jetpack Compose, Firebase Firestore, Navigation Compose) — no new dependencies needed for this plan.

## Global Constraints

- **Firestore schema must match the web app exactly** — `flats/{flatId}` (fields: `name`, `adminUid`, `createdAt`, `memberCount`, `subscriptionStatus`, `trialEndDate`), `flats/{flatId}/members/{uid}` (fields: `uid`, `nickname`, `email`, `role`, `status`, `reliabilityScore`, `joinedAt`), and `users/{uid}` gains `activeFlatId` + `flatIds` (array). No native-only fields, no renamed fields — these are read by the same web app and same Firestore Security Rules.
- **The flat ID is the invite code** — format `FLAT-XXXX`, 4 characters from the alphabet `ABCDEFGHJKLMNPQRSTUVWXYZ23456789` (matches `C:\garbage\lib\flatService.ts`'s `generateFlatId()` exactly — this specific alphabet excludes visually-ambiguous characters like `0`/`O`/`1`/`I`, so don't "simplify" it to A-Z0-9).
- **8-member cap, enforced transactionally** — matches the web app's `joinFlat` and the production Firestore rule (`get(...).data.get('memberCount', 0) < 8` in `C:\garbage\firestore.rules`).
- **Instant-join only** — no approval-mode join requests in this plan (explicitly deferred, see the spec).
- **Standing visual-design principle**: every screen must be understandable from icons/layout/short labels alone — no walls of instructional text. One clear primary action per screen.
- **No offline mode, no silently swallowed failures** — same as the Foundation & Auth plan; every repository call surfaces a typed `Result`, every screen has a working Loading/Error state.
- **Build tooling**: Gradle 9.4.1 at `C:\Users\user\.gradle\wrapper\dists\gradle-9.4.1-bin\arn2x92ynaizyzdaamcbpbhtj\gradle-9.4.1\bin\gradle.bat`, `JAVA_HOME` must point at `C:\Program Files\Android\Android Studio\jbr`. Do not modify `gradle.properties`, `app/build.gradle.kts`'s `testOptions`/signing config, or `app/google-services.json` — all already correctly configured and verified working; a prior plan lost real time to an implementer misdiagnosing a `gradle.properties` encoding bug as "insufficient memory," so don't touch JVM/heap settings to fix a build issue — report the exact error instead.

---

## File Structure

```
C:\habitiq_jaswanth\app\src\main\kotlin\habitiq\app\
  flats\
    FlatIdGenerator.kt        (pure: generate + validate invite codes)
    FlatException.kt          (sealed exception hierarchy + mapFlatError)
    FlatModels.kt              (FlatInfo, Member data classes)
    FlatsRepository.kt         (createFlat, joinFlat, getFlat)
    MembersRepository.kt       (observeMembers real-time Flow)
    FlatUiState.kt             (Idle/Loading/Error/Success, mirrors AuthUiState)
    ShareLauncher.kt           (native Android share-sheet wrapper)
    HomeViewModel.kt
    CreateFlatViewModel.kt
    JoinFlatViewModel.kt
    FlatHomeViewModel.kt
  data\
    UsersRepository.kt         (MODIFY: add getActiveFlatId)
  ui\
    HomeScreen.kt               (MODIFY: routes to Create/Join or the flat's home)
    CreateFlatScreen.kt
    JoinFlatScreen.kt
    FlatHomeScreen.kt
  HabitiqApp.kt                 (MODIFY: add createFlat/joinFlat/flatHome routes)

app\src\test\kotlin\habitiq\app\flats\
  FlatIdGeneratorTest.kt
  FlatErrorMapperTest.kt
```

Each file has one job: `FlatIdGenerator` only generates/validates code strings (no Firestore). `FlatException`/`mapFlatError` only classify and phrase errors. `FlatsRepository`/`MembersRepository` only talk to Firestore for their one collection each. `ShareLauncher` only wraps the Android share intent. Each ViewModel only holds one screen's state.

---

### Task 1: Invite-code generation (pure logic, unit tested)

**Files:**
- Create: `app/src/main/kotlin/habitiq/app/flats/FlatIdGenerator.kt`
- Test: `app/src/test/kotlin/habitiq/app/flats/FlatIdGeneratorTest.kt`

**Interfaces:**
- Produces: `fun generateFlatId(random: kotlin.random.Random = kotlin.random.Random.Default): String`, `fun isValidFlatIdFormat(flatId: String): Boolean` — both used by `FlatsRepository` (Task 3).

- [ ] **Step 1: Write the failing test**

```kotlin
package habitiq.app.flats

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class FlatIdGeneratorTest {

    @Test
    fun `generated id has FLAT dash prefix and 4 character code`() {
        val id = generateFlatId(Random(seed = 42))
        assertTrue(id.startsWith("FLAT-"))
        assertEquals(9, id.length) // "FLAT-" (5) + 4 chars
    }

    @Test
    fun `generated id only uses the unambiguous alphabet`() {
        val id = generateFlatId(Random(seed = 1))
        val code = id.removePrefix("FLAT-")
        val allowed = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toSet()
        assertTrue(code.all { it in allowed })
    }

    @Test
    fun `same seed produces the same id (deterministic for testing)`() {
        val first = generateFlatId(Random(seed = 7))
        val second = generateFlatId(Random(seed = 7))
        assertEquals(first, second)
    }

    @Test
    fun `valid format is accepted`() {
        assertTrue(isValidFlatIdFormat("FLAT-A3B9"))
    }

    @Test
    fun `missing prefix is rejected`() {
        assertFalse(isValidFlatIdFormat("A3B9"))
    }

    @Test
    fun `wrong code length is rejected`() {
        assertFalse(isValidFlatIdFormat("FLAT-A3B"))
        assertFalse(isValidFlatIdFormat("FLAT-A3B99"))
    }

    @Test
    fun `ambiguous characters like O, 0, I, 1 are rejected`() {
        assertFalse(isValidFlatIdFormat("FLAT-O0I1"))
    }

    @Test
    fun `lowercase is rejected (codes are always uppercase)`() {
        assertFalse(isValidFlatIdFormat("flat-a3b9"))
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `gradle testDebugUnitTest --tests "habitiq.app.flats.FlatIdGeneratorTest"`
Expected: FAIL — `generateFlatId`/`isValidFlatIdFormat` unresolved.

- [ ] **Step 3: Write minimal implementation**

```kotlin
package habitiq.app.flats

import kotlin.random.Random

private const val FLAT_ID_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
private const val FLAT_ID_CODE_LENGTH = 4
private const val FLAT_ID_PREFIX = "FLAT-"
private val FLAT_ID_PATTERN = Regex("^$FLAT_ID_PREFIX[$FLAT_ID_ALPHABET]{$FLAT_ID_CODE_LENGTH}$")

fun generateFlatId(random: Random = Random.Default): String {
    val code = (1..FLAT_ID_CODE_LENGTH)
        .map { FLAT_ID_ALPHABET[random.nextInt(FLAT_ID_ALPHABET.length)] }
        .joinToString("")
    return "$FLAT_ID_PREFIX$code"
}

fun isValidFlatIdFormat(flatId: String): Boolean = FLAT_ID_PATTERN.matches(flatId)
```

- [ ] **Step 4: Run test to verify it passes**

Run: `gradle testDebugUnitTest --tests "habitiq.app.flats.FlatIdGeneratorTest"`
Expected: PASS, all 8 tests green.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/kotlin/habitiq/app/flats/FlatIdGenerator.kt app/src/test/kotlin/habitiq/app/flats/FlatIdGeneratorTest.kt
git commit -m "feat: add invite-code generation and format validation"
```

---

### Task 2: Flat errors (pure logic, unit tested)

**Files:**
- Create: `app/src/main/kotlin/habitiq/app/flats/FlatException.kt`
- Test: `app/src/test/kotlin/habitiq/app/flats/FlatErrorMapperTest.kt`

**Interfaces:**
- Produces: `sealed class FlatException`, subclasses `FlatNotFoundException`, `FlatFullException`, `AlreadyMemberException`; `fun mapFlatError(exception: Exception): String` — used by `FlatsRepository` (Task 3).

- [ ] **Step 1: Write the failing test**

```kotlin
package habitiq.app.flats

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

class FlatErrorMapperTest {

    @Test
    fun `flat not found maps to plain not-found message`() {
        assertEquals(
            "Flat not found. Check the invite code and try again.",
            mapFlatError(FlatNotFoundException())
        )
    }

    @Test
    fun `flat full maps to plain full message`() {
        assertEquals(
            "This flat is full (maximum 8 members).",
            mapFlatError(FlatFullException())
        )
    }

    @Test
    fun `already member maps to plain already-member message`() {
        assertEquals(
            "You are already a member of this flat.",
            mapFlatError(AlreadyMemberException())
        )
    }

    @Test
    fun `network error maps to connectivity message`() {
        assertEquals(
            "No internet connection. Please try again.",
            mapFlatError(IOException("network down"))
        )
    }

    @Test
    fun `unknown error maps to generic fallback message`() {
        assertEquals(
            "Something went wrong. Please try again.",
            mapFlatError(RuntimeException("something else"))
        )
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `gradle testDebugUnitTest --tests "habitiq.app.flats.FlatErrorMapperTest"`
Expected: FAIL — `FlatNotFoundException`/`FlatFullException`/`AlreadyMemberException`/`mapFlatError` unresolved.

- [ ] **Step 3: Write minimal implementation**

```kotlin
package habitiq.app.flats

import java.io.IOException

sealed class FlatException(message: String) : Exception(message)
class FlatNotFoundException : FlatException("Flat not found. Check the invite code and try again.")
class FlatFullException : FlatException("This flat is full (maximum 8 members).")
class AlreadyMemberException : FlatException("You are already a member of this flat.")

fun mapFlatError(exception: Exception): String = when (exception) {
    is FlatException -> exception.message ?: "Something went wrong. Please try again."
    is IOException -> "No internet connection. Please try again."
    else -> "Something went wrong. Please try again."
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `gradle testDebugUnitTest --tests "habitiq.app.flats.FlatErrorMapperTest"`
Expected: PASS, all 5 tests green.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/kotlin/habitiq/app/flats/FlatException.kt app/src/test/kotlin/habitiq/app/flats/FlatErrorMapperTest.kt
git commit -m "feat: add Flat exception hierarchy and plain-language error mapping"
```

---

### Task 3: FlatsRepository (create + join + get, matching flatService.ts)

**Files:**
- Create: `app/src/main/kotlin/habitiq/app/flats/FlatModels.kt`
- Create: `app/src/main/kotlin/habitiq/app/flats/FlatsRepository.kt`

**Interfaces:**
- Consumes: `generateFlatId()`, `isValidFlatIdFormat()` (Task 1), `FlatNotFoundException`/`FlatFullException`/`AlreadyMemberException`/`mapFlatError()` (Task 2)
- Produces:
  - `data class FlatInfo(val id: String, val name: String, val adminUid: String, val memberCount: Int)`
  - `class FlatsRepository` with `suspend fun createFlat(name: String, uid: String, nickname: String, email: String): Result<String>` (returns the new flatId), `suspend fun joinFlat(flatId: String, uid: String, nickname: String, email: String): Result<Unit>`, `suspend fun getFlat(flatId: String): Result<FlatInfo>` — all consumed by Task 7/8/9's ViewModels.

- [ ] **Step 1: Create `FlatModels.kt`**

```kotlin
package habitiq.app.flats

data class FlatInfo(
    val id: String,
    val name: String,
    val adminUid: String,
    val memberCount: Int
)
```

- [ ] **Step 2: Create `FlatsRepository.kt`**

```kotlin
package habitiq.app.flats

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.time.Instant

private const val MAX_MEMBERS = 8
private const val MAX_ID_GENERATION_ATTEMPTS = 5
private const val TRIAL_DURATION_SECONDS = 30L * 24 * 60 * 60

class FlatsRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun createFlat(name: String, uid: String, nickname: String, email: String): Result<String> = runCatching {
        var flatId = generateFlatId()
        var attempts = 0
        while (flatExists(flatId) && attempts < MAX_ID_GENERATION_ATTEMPTS) {
            flatId = generateFlatId()
            attempts++
        }

        val flatData = hashMapOf(
            "name" to name,
            "adminUid" to uid,
            "createdAt" to FieldValue.serverTimestamp(),
            "memberCount" to 1,
            "subscriptionStatus" to "trial",
            "trialEndDate" to Instant.now().plusSeconds(TRIAL_DURATION_SECONDS).toString()
        )
        firestore.collection("flats").document(flatId).set(flatData).await()

        val memberData = hashMapOf(
            "uid" to uid,
            "nickname" to nickname,
            "email" to email,
            "role" to "admin",
            "status" to "available",
            "reliabilityScore" to 100,
            "joinedAt" to Instant.now().toString()
        )
        firestore.collection("flats").document(flatId).collection("members").document(uid)
            .set(memberData).await()

        firestore.collection("users").document(uid).set(
            mapOf(
                "activeFlatId" to flatId,
                "flatIds" to FieldValue.arrayUnion(flatId),
                "email" to email
            ),
            SetOptions.merge()
        ).await()

        flatId
    }.recoverCatching { throw IllegalStateException(mapFlatError(it as Exception), it) }

    suspend fun joinFlat(flatId: String, uid: String, nickname: String, email: String): Result<Unit> = runCatching {
        if (!flatExists(flatId)) throw FlatNotFoundException()

        firestore.runTransaction { transaction ->
            val flatRef = firestore.collection("flats").document(flatId)
            val memberRef = flatRef.collection("members").document(uid)

            val flatSnap = transaction.get(flatRef)
            val memberSnap = transaction.get(memberRef)

            if (!flatSnap.exists()) throw FlatNotFoundException()
            val memberCount = flatSnap.getLong("memberCount") ?: 0L
            if (memberCount >= MAX_MEMBERS) throw FlatFullException()
            if (memberSnap.exists()) throw AlreadyMemberException()

            val memberData = hashMapOf(
                "uid" to uid,
                "nickname" to nickname,
                "email" to email,
                "role" to "member",
                "status" to "available",
                "reliabilityScore" to 100,
                "joinedAt" to Instant.now().toString()
            )
            transaction.set(memberRef, memberData)
            transaction.update(flatRef, "memberCount", FieldValue.increment(1))

            val userRef = firestore.collection("users").document(uid)
            transaction.set(
                userRef,
                mapOf(
                    "activeFlatId" to flatId,
                    "flatIds" to FieldValue.arrayUnion(flatId),
                    "email" to email
                ),
                SetOptions.merge()
            )

            null
        }.await()

        Unit
    }.recoverCatching { throw IllegalStateException(mapFlatError(it as Exception), it) }

    suspend fun getFlat(flatId: String): Result<FlatInfo> = runCatching {
        val snap = firestore.collection("flats").document(flatId).get().await()
        if (!snap.exists()) throw FlatNotFoundException()
        FlatInfo(
            id = flatId,
            name = snap.getString("name") ?: flatId,
            adminUid = snap.getString("adminUid").orEmpty(),
            memberCount = (snap.getLong("memberCount") ?: 0L).toInt()
        )
    }.recoverCatching { throw IllegalStateException(mapFlatError(it as Exception), it) }

    private suspend fun flatExists(flatId: String): Boolean {
        val snap = firestore.collection("flats").document(flatId).get().await()
        return snap.exists()
    }
}
```

Note: `recoverCatching` re-wraps failures into `IllegalStateException(mapFlatError(...), cause)` the same way `AuthRepository` does (Task 4 of the Foundation & Auth plan) — a prior review caught and fixed a version of this that dropped the original exception as `cause`; this plan starts from the corrected two-argument form directly.

- [ ] **Step 3: Build and verify it compiles**

Run: `gradle compileDebugKotlin --project-dir "C:\habitiq_jaswanth"`
Expected: `BUILD SUCCESSFUL`.

(No new automated test here beyond Tasks 1-2 — `FlatsRepository` is a thin Firestore wrapper around already-tested pure logic; its actual behavior is verified end-to-end in Task 10's manual device pass, same as `AuthRepository` was.)

- [ ] **Step 4: Commit**

```bash
git add app/src/main/kotlin/habitiq/app/flats/FlatModels.kt app/src/main/kotlin/habitiq/app/flats/FlatsRepository.kt
git commit -m "feat: add FlatsRepository (create/join/get), ported from flatService.ts"
```

---

### Task 4: MembersRepository (real-time member list)

**Files:**
- Create: `app/src/main/kotlin/habitiq/app/flats/MembersRepository.kt`

**Interfaces:**
- Produces: `data class Member(val uid: String, val nickname: String, val role: String)`, `class MembersRepository` with `fun observeMembers(flatId: String): Flow<List<Member>>` — consumed by Task 9's `FlatHomeViewModel`.

- [ ] **Step 1: Create `MembersRepository.kt`**

```kotlin
package habitiq.app.flats

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

data class Member(
    val uid: String,
    val nickname: String,
    val role: String
)

class MembersRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun observeMembers(flatId: String): Flow<List<Member>> = callbackFlow {
        val registration = firestore.collection("flats").document(flatId).collection("members")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val members = snapshot?.documents.orEmpty().mapNotNull { doc ->
                    val uid = doc.getString("uid") ?: return@mapNotNull null
                    Member(
                        uid = uid,
                        nickname = doc.getString("nickname").orEmpty(),
                        role = doc.getString("role") ?: "member"
                    )
                }
                trySend(members)
            }
        awaitClose { registration.remove() }
    }
}
```

- [ ] **Step 2: Build and verify it compiles**

Run: `gradle compileDebugKotlin --project-dir "C:\habitiq_jaswanth"`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/kotlin/habitiq/app/flats/MembersRepository.kt
git commit -m "feat: add MembersRepository with real-time member list"
```

---

### Task 5: FlatUiState + UsersRepository.getActiveFlatId

**Files:**
- Create: `app/src/main/kotlin/habitiq/app/flats/FlatUiState.kt`
- Modify: `app/src/main/kotlin/habitiq/app/data/UsersRepository.kt`

**Interfaces:**
- Produces: `sealed interface FlatUiState { Idle, Loading, data class Error(val message: String), Success }` — used by every ViewModel in this plan (Tasks 7-9).
- Produces: `suspend fun UsersRepository.getActiveFlatId(uid: String): String?` — used by Task 6's `HomeViewModel`.

- [ ] **Step 1: Create `FlatUiState.kt`**

```kotlin
package habitiq.app.flats

sealed interface FlatUiState {
    data object Idle : FlatUiState
    data object Loading : FlatUiState
    data class Error(val message: String) : FlatUiState
    data object Success : FlatUiState
}
```

- [ ] **Step 2: Add `getActiveFlatId` to `UsersRepository.kt`**

Read the current file first (`app/src/main/kotlin/habitiq/app/data/UsersRepository.kt` from the Foundation & Auth plan) and add this method inside the `UsersRepository` class, alongside `ensureUserDocument`:

```kotlin
    suspend fun getActiveFlatId(uid: String): String? {
        val snap = firestore.collection("users").document(uid).get().await()
        return snap.getString("activeFlatId")
    }
```

(No new imports needed — `firestore` and `.await()` are already used by `ensureUserDocument` in the same file.)

- [ ] **Step 3: Build and verify it compiles**

Run: `gradle compileDebugKotlin --project-dir "C:\habitiq_jaswanth"`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/kotlin/habitiq/app/flats/FlatUiState.kt app/src/main/kotlin/habitiq/app/data/UsersRepository.kt
git commit -m "feat: add FlatUiState and UsersRepository.getActiveFlatId"
```

---

### Task 6: ShareLauncher (native Android share sheet)

**Files:**
- Create: `app/src/main/kotlin/habitiq/app/flats/ShareLauncher.kt`

**Interfaces:**
- Produces: `fun launchShareInviteCode(context: Context, flatName: String, flatId: String)` — used by Task 7's `CreateFlatScreen` and Task 9's `FlatHomeScreen`.

- [ ] **Step 1: Create `ShareLauncher.kt`**

```kotlin
package habitiq.app.flats

import android.content.Context
import android.content.Intent

fun launchShareInviteCode(context: Context, flatName: String, flatId: String) {
    val message = "Join my flat \"$flatName\" on Habitiq! Use code $flatId to join."
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, message)
    }
    val chooser = Intent.createChooser(sendIntent, "Share invite code")
    context.startActivity(chooser)
}
```

This is a plain function wrapping a standard Android intent (not a suspend function, no callback) — the system share sheet (WhatsApp, Messages, Gmail, etc.) is populated automatically by Android from apps that declare `ACTION_SEND` support; no per-app integration code is needed or possible from this side.

- [ ] **Step 2: Build and verify it compiles**

Run: `gradle compileDebugKotlin --project-dir "C:\habitiq_jaswanth"`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/kotlin/habitiq/app/flats/ShareLauncher.kt
git commit -m "feat: add native share-sheet launcher for invite codes"
```

---

### Task 7: Shared onboarding visual components + Create Flat screen

**REVISED 2026-07-11**: Sai shared 3 screenshots of the web app's (`C:\garbage`) desktop Create/Join Flat flow and asked that the same "visual story" carry into these native screens — not the bare text-field forms originally planned here. Confirmed with Sai: illustration artwork is replaced with an icon + gradient band (no image assets available this pass); everything else (color-coding, benefit bullets, role-callout box) is carried over. This task now includes a shared header component (per Jetpack Compose guidance: don't duplicate layouts — `FlatOnboardingHeader`/`FlatRoleCallout` are reused by both this screen and Task 8's `JoinFlatScreen`) and a new dependency for icons beyond the small default set.

**Files:**
- Modify: `gradle/libs.versions.toml` (add material-icons-extended)
- Modify: `app/build.gradle.kts` (add the dependency)
- Create: `app/src/main/kotlin/habitiq/app/ui/FlatOnboardingHeader.kt`
- Create: `app/src/main/kotlin/habitiq/app/flats/CreateFlatViewModel.kt`
- Create: `app/src/main/kotlin/habitiq/app/ui/CreateFlatScreen.kt`

**Interfaces:**
- Consumes: `FlatsRepository.createFlat(...)` (Task 3), `AuthRepository.currentUser` (existing), `FlatUiState` (Task 5), `launchShareInviteCode` (Task 6)
- Produces: `class CreateFlatViewModel(...)` exposing `val state: StateFlow<FlatUiState>`, `val createdFlatId: StateFlow<String?>`, `fun createFlat(flatName: String)`. `@Composable fun FlatOnboardingHeader(...)` and `@Composable fun FlatRoleCallout(...)` in `habitiq.app.ui`, reused by Task 8's `JoinFlatScreen`.

- [ ] **Step 1: Add the icons dependency**

Add to `gradle/libs.versions.toml`'s `[libraries]` section:

```toml
androidx-material-icons-extended = { group = "androidx.compose.material", name = "material-icons-extended" }
```

(No version needed — covered by the existing `androidx.compose:compose-bom` platform, same as `androidx-ui`/`androidx-material3`.)

Add to `app/build.gradle.kts`'s `dependencies { }` block, alongside the other Compose UI dependencies:

```kotlin
    implementation(libs.androidx.material.icons.extended)
```

- [ ] **Step 2: Create `FlatOnboardingHeader.kt`**

```kotlin
package habitiq.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val FlatOnboardingBackground = Color(0xFF0A0A0F)
val FlatOnboardingSubtitleColor = Color(0xFFB0B0B0)
val FlatOnboardingBenefitColor = Color(0xFFD0D0D0)

@Composable
fun FlatOnboardingHeader(
    accentColor: Color,
    imageRes: Int,
    titleLine1: String,
    titleLine2: String,
    subtitle: String,
    benefits: List<String>
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) {
            Image(
                painter = painterResource(imageRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            )
            // Fades the illustration into the screen's background color so the hero
            // image reads as part of the page, not a pasted-in rectangle.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, FlatOnboardingBackground),
                            startY = 550f
                        )
                    )
            )
        }
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
            Text(titleLine1, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Text(titleLine2, color = accentColor, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Text(
                subtitle,
                color = FlatOnboardingSubtitleColor,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
            )
            benefits.forEach { benefit ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        benefit,
                        color = FlatOnboardingBenefitColor,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FlatRoleCallout(accentColor: Color, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, color = Color(0xFFE0E0E0), fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}
```

**REVISED again, same day**: Sai provided the real illustration assets (`onboard-create.png`, `onboard-join.png` — the exact artwork from the web app reference). Resized to 1080px wide and converted to WebP (quality 82) to go from ~7-8MB each down to ~110-150KB, placed at `app/src/main/res/drawable/onboard_create.webp` / `onboard_join.webp`. `FlatOnboardingHeader` takes `imageRes: Int` (a `@DrawableRes` resource ID) instead of an `ImageVector` icon, rendered via `Image(painter = painterResource(imageRes), contentScale = ContentScale.Crop, ...)` in a 300dp-tall `Box`, with a vertical gradient overlay fading into `FlatOnboardingBackground` at the bottom so the image blends into the page rather than looking pasted in. The code block above already reflects this final version — the icon-based version further above in this task's history is superseded.

- [ ] **Step 3: Create `CreateFlatViewModel.kt`**

```kotlin
package habitiq.app.flats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import habitiq.app.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CreateFlatViewModel(
    private val authRepository: AuthRepository,
    private val flatsRepository: FlatsRepository
) : ViewModel() {

    private val _state = MutableStateFlow<FlatUiState>(FlatUiState.Idle)
    val state: StateFlow<FlatUiState> = _state

    private val _createdFlatId = MutableStateFlow<String?>(null)
    val createdFlatId: StateFlow<String?> = _createdFlatId

    fun createFlat(flatName: String) {
        val user = authRepository.currentUser.value
        if (user == null) {
            _state.value = FlatUiState.Error("You must be signed in to create a flat.")
            return
        }
        _state.value = FlatUiState.Loading
        viewModelScope.launch {
            val nickname = user.displayName ?: user.email.orEmpty()
            val result = flatsRepository.createFlat(flatName, user.uid, nickname, user.email.orEmpty())
            result.onSuccess { flatId ->
                _createdFlatId.value = flatId
                _state.value = FlatUiState.Success
            }.onFailure { error ->
                _state.value = FlatUiState.Error(error.message ?: "Something went wrong. Please try again.")
            }
        }
    }
}
```

- [ ] **Step 4: Create `CreateFlatScreen.kt`**

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import habitiq.app.flats.CreateFlatViewModel
import habitiq.app.flats.FlatUiState
import habitiq.app.flats.launchShareInviteCode

val CreateFlatAccent = Color(0xFFF97316)

@Composable
fun CreateFlatScreen(
    viewModel: CreateFlatViewModel,
    onDone: () -> Unit
) {
    var flatName by remember { mutableStateOf("") }
    val state by viewModel.state.collectAsStateWithLifecycleCompat()
    val createdFlatId by viewModel.createdFlatId.collectAsStateWithLifecycleCompat()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FlatOnboardingBackground)
            .verticalScroll(rememberScrollState())
    ) {
        FlatOnboardingHeader(
            accentColor = CreateFlatAccent,
            icon = Icons.Filled.Home,
            titleLine1 = "Your Flat.",
            titleLine2 = "Your Rules.",
            subtitle = "Become the admin. Invite your roommates, set up chore rotation and bills — your shared home, on autopilot.",
            benefits = listOf(
                "Invite roommates with a code",
                "Auto-rotate chores fairly",
                "Split and track every expense"
            )
        )

        Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
            if (createdFlatId == null) {
                OutlinedTextField(
                    value = flatName,
                    onValueChange = { flatName = it },
                    label = { Text("Flat name") },
                    placeholder = { Text("e.g., The Boys Apartment") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = CreateFlatAccent,
                        unfocusedBorderColor = Color(0xFF3A3A3A),
                        focusedLabelColor = CreateFlatAccent,
                        unfocusedLabelColor = Color(0xFF9A9A9A)
                    )
                )
                Spacer(Modifier.height(16.dp))
                FlatRoleCallout(
                    accentColor = CreateFlatAccent,
                    text = "You'll be the admin and can manage everything in your flat."
                )
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = { viewModel.createFlat(flatName) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = CreateFlatAccent)
                ) {
                    Text("Create Flat")
                }
                when (val current = state) {
                    is FlatUiState.Loading -> Text(
                        "Creating…",
                        color = Color.White,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                    is FlatUiState.Error -> Text(
                        current.message,
                        color = Color(0xFFFF6B6B),
                        modifier = Modifier.padding(top = 12.dp)
                    )
                    else -> {}
                }
            } else {
                Text("Your flat is ready!", color = Color.White)
                Text("Invite code: $createdFlatId", color = CreateFlatAccent)
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = { launchShareInviteCode(context, flatName, createdFlatId!!) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = CreateFlatAccent)
                ) {
                    Text("Share invite code")
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onDone,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Done")
                }
            }
        }
    }
}
```

**Same asset-swap note as `FlatOnboardingHeader.kt` above applies here**: `CreateFlatScreen.kt`'s actual final version imports `habitiq.app.R` (not `Icons.Filled.Home`) and calls `FlatOnboardingHeader(..., imageRes = R.drawable.onboard_create, ...)`.

- [ ] **Step 5: Build and verify it compiles**

Run: `gradle assembleDebug --project-dir "C:\habitiq_jaswanth"`
Expected: `BUILD SUCCESSFUL`.

(This won't wire into navigation until Task 9 — verify it compiles standalone for now.)

- [ ] **Step 6: Commit**

```bash
git add gradle/libs.versions.toml app/build.gradle.kts app/src/main/kotlin/habitiq/app/ui/FlatOnboardingHeader.kt app/src/main/kotlin/habitiq/app/flats/CreateFlatViewModel.kt app/src/main/kotlin/habitiq/app/ui/CreateFlatScreen.kt app/src/main/res/drawable/onboard_create.webp
git commit -m "feat: add Create Flat screen with visual onboarding story (illustration hero, benefit bullets, role callout)"
```

---

### Task 8: Join with Code screen + ViewModel

**REVISED 2026-07-11**: same visual treatment as Task 7, reusing `FlatOnboardingHeader`/`FlatRoleCallout` with the purple/member variant.

**Files:**
- Create: `app/src/main/kotlin/habitiq/app/flats/JoinFlatViewModel.kt`
- Create: `app/src/main/kotlin/habitiq/app/ui/JoinFlatScreen.kt`

**Interfaces:**
- Consumes: `FlatsRepository.joinFlat(...)` (Task 3), `isValidFlatIdFormat` (Task 1), `AuthRepository.currentUser` (existing), `FlatUiState` (Task 5), `FlatOnboardingHeader`/`FlatRoleCallout`/`FlatOnboardingBackground` (Task 7)
- Produces: `class JoinFlatViewModel(...)` exposing `val state: StateFlow<FlatUiState>`, `val joinedFlatId: StateFlow<String?>`, `fun joinFlat(code: String)` — Task 9's nav wiring consumes `joinedFlatId` to navigate onward.

- [ ] **Step 1: Create `JoinFlatViewModel.kt`**

```kotlin
package habitiq.app.flats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import habitiq.app.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class JoinFlatViewModel(
    private val authRepository: AuthRepository,
    private val flatsRepository: FlatsRepository
) : ViewModel() {

    private val _state = MutableStateFlow<FlatUiState>(FlatUiState.Idle)
    val state: StateFlow<FlatUiState> = _state

    private val _joinedFlatId = MutableStateFlow<String?>(null)
    val joinedFlatId: StateFlow<String?> = _joinedFlatId

    fun joinFlat(code: String) {
        val normalizedCode = code.trim().uppercase()
        if (!isValidFlatIdFormat(normalizedCode)) {
            _state.value = FlatUiState.Error("That doesn't look like a valid invite code.")
            return
        }
        val user = authRepository.currentUser.value
        if (user == null) {
            _state.value = FlatUiState.Error("You must be signed in to join a flat.")
            return
        }
        _state.value = FlatUiState.Loading
        viewModelScope.launch {
            val nickname = user.displayName ?: user.email.orEmpty()
            val result = flatsRepository.joinFlat(normalizedCode, user.uid, nickname, user.email.orEmpty())
            result.onSuccess {
                _joinedFlatId.value = normalizedCode
                _state.value = FlatUiState.Success
            }.onFailure { error ->
                _state.value = FlatUiState.Error(error.message ?: "Something went wrong. Please try again.")
            }
        }
    }
}
```

- [ ] **Step 2: Create `JoinFlatScreen.kt`**

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import habitiq.app.R
import habitiq.app.flats.FlatUiState
import habitiq.app.flats.JoinFlatViewModel

val JoinFlatAccent = Color(0xFF7B5CFA)

@Composable
fun JoinFlatScreen(
    viewModel: JoinFlatViewModel,
    onJoined: (flatId: String) -> Unit
) {
    var code by remember { mutableStateOf("") }
    val state by viewModel.state.collectAsStateWithLifecycleCompat()
    val joinedFlatId by viewModel.joinedFlatId.collectAsStateWithLifecycleCompat()

    LaunchedEffect(state, joinedFlatId) {
        val flatId = joinedFlatId
        if (state is FlatUiState.Success && flatId != null) {
            onJoined(flatId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FlatOnboardingBackground)
            .verticalScroll(rememberScrollState())
    ) {
        FlatOnboardingHeader(
            accentColor = JoinFlatAccent,
            imageRes = R.drawable.onboard_join,
            titleLine1 = "Join Your",
            titleLine2 = "Crew.",
            subtitle = "Have an invite code? Walk right in. Expenses, chores, bills — already set up and waiting for you.",
            benefits = listOf(
                "Step in instantly — no setup",
                "See balances and shared expenses",
                "Stay synced in real-time"
            )
        )

        Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
            OutlinedTextField(
                value = code,
                onValueChange = { code = it },
                label = { Text("Invite code") },
                placeholder = { Text("FLAT-A3B9") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = JoinFlatAccent,
                    unfocusedBorderColor = Color(0xFF3A3A3A),
                    focusedLabelColor = JoinFlatAccent,
                    unfocusedLabelColor = Color(0xFF9A9A9A)
                )
            )
            Spacer(Modifier.height(16.dp))
            FlatRoleCallout(
                accentColor = JoinFlatAccent,
                text = "You'll join as a member — all expenses and chores are already set up."
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = { viewModel.joinFlat(code) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = JoinFlatAccent)
            ) {
                Text("Join Flat")
            }
            when (val current = state) {
                is FlatUiState.Loading -> Text(
                    "Joining…",
                    color = Color.White,
                    modifier = Modifier.padding(top = 12.dp)
                )
                is FlatUiState.Error -> Text(
                    current.message,
                    color = Color(0xFFFF6B6B),
                    modifier = Modifier.padding(top = 12.dp)
                )
                else -> {}
            }
        }
    }
}
```

Note: this screen uses the `LaunchedEffect`-gated navigation pattern (fires once per state transition into `Success`, not on every recomposition) — the same pattern a prior plan's review caught and fixed for `LoginScreen`/`SignupScreen`. This task starts from the corrected pattern directly rather than repeating that bug. It also reuses `FlatOnboardingHeader`/`FlatRoleCallout`/`FlatOnboardingBackground` from Task 7 rather than duplicating that layout, per Jetpack Compose's "extract reusable layout patterns" guidance.

`R.drawable.onboard_join` already exists at `app/src/main/res/drawable/onboard_join.webp` (placed and compressed to ~147KB alongside `onboard_create.webp` during Task 7's asset swap) — no new asset work needed for this task.

- [ ] **Step 3: Build and verify it compiles**

Run: `gradle assembleDebug --project-dir "C:\habitiq_jaswanth"`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/kotlin/habitiq/app/flats/JoinFlatViewModel.kt app/src/main/kotlin/habitiq/app/ui/JoinFlatScreen.kt
git commit -m "feat: add Join with Code screen with visual onboarding story"
```

---

### Task 9: Flat Home screen + Home screen routing + nav wiring

**Files:**
- Create: `app/src/main/kotlin/habitiq/app/flats/HomeViewModel.kt`
- Create: `app/src/main/kotlin/habitiq/app/flats/FlatHomeViewModel.kt`
- Create: `app/src/main/kotlin/habitiq/app/ui/FlatHomeScreen.kt`
- Modify: `app/src/main/kotlin/habitiq/app/ui/HomeScreen.kt`
- Modify: `app/src/main/kotlin/habitiq/app/HabitiqApp.kt`

**Interfaces:**
- Consumes: `UsersRepository.getActiveFlatId` (Task 5), `FlatsRepository.getFlat` (Task 3), `MembersRepository.observeMembers` (Task 4), `CreateFlatScreen`/`CreateFlatViewModel` (Task 7), `JoinFlatScreen`/`JoinFlatViewModel` (Task 8)
- Produces: nav routes `createFlat`, `joinFlat`, `flatHome/{flatId}` added to the existing graph — no later task in this plan depends on these internals.

- [ ] **Step 1: Create `HomeViewModel.kt`**

```kotlin
package habitiq.app.flats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import habitiq.app.auth.AuthRepository
import habitiq.app.data.UsersRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface HomeFlatStatus {
    data object Loading : HomeFlatStatus
    data object NoFlat : HomeFlatStatus
    data class InFlat(val flatId: String) : HomeFlatStatus
}

class HomeViewModel(
    private val authRepository: AuthRepository,
    private val usersRepository: UsersRepository
) : ViewModel() {

    private val _flatStatus = MutableStateFlow<HomeFlatStatus>(HomeFlatStatus.Loading)
    val flatStatus: StateFlow<HomeFlatStatus> = _flatStatus

    init {
        checkFlatStatus()
    }

    fun checkFlatStatus() {
        val user = authRepository.currentUser.value
        if (user == null) {
            _flatStatus.value = HomeFlatStatus.NoFlat
            return
        }
        _flatStatus.value = HomeFlatStatus.Loading
        viewModelScope.launch {
            val activeFlatId = usersRepository.getActiveFlatId(user.uid)
            _flatStatus.value = if (activeFlatId != null) {
                HomeFlatStatus.InFlat(activeFlatId)
            } else {
                HomeFlatStatus.NoFlat
            }
        }
    }
}
```

- [ ] **Step 2: Create `FlatHomeViewModel.kt`**

```kotlin
package habitiq.app.flats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FlatHomeViewModel(
    private val flatId: String,
    private val flatsRepository: FlatsRepository,
    private val membersRepository: MembersRepository
) : ViewModel() {

    private val _flat = MutableStateFlow<FlatInfo?>(null)
    val flat: StateFlow<FlatInfo?> = _flat

    private val _members = MutableStateFlow<List<Member>>(emptyList())
    val members: StateFlow<List<Member>> = _members

    init {
        viewModelScope.launch {
            flatsRepository.getFlat(flatId).onSuccess { _flat.value = it }
        }
        viewModelScope.launch {
            membersRepository.observeMembers(flatId).collect { _members.value = it }
        }
    }
}
```

- [ ] **Step 3: Create `FlatHomeScreen.kt`**

```kotlin
package habitiq.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import habitiq.app.flats.FlatHomeViewModel
import habitiq.app.flats.launchShareInviteCode

@Composable
fun FlatHomeScreen(viewModel: FlatHomeViewModel, currentUid: String) {
    val flat by viewModel.flat.collectAsStateWithLifecycleCompat()
    val members by viewModel.members.collectAsStateWithLifecycleCompat()
    val context = LocalContext.current

    Column(modifier = Modifier.padding(24.dp)) {
        val currentFlat = flat
        if (currentFlat == null) {
            Text("Loading your flat…")
        } else {
            Text(currentFlat.name)
            Text("${currentFlat.memberCount} member(s)")
            if (currentFlat.adminUid == currentUid) {
                Text("Invite code: ${currentFlat.id}")
                Button(onClick = { launchShareInviteCode(context, currentFlat.name, currentFlat.id) }) {
                    Text("Share invite code")
                }
            }
            Text("Roommates:")
            members.forEach { member ->
                Text("${member.nickname} (${member.role})")
            }
        }
    }
}
```

- [ ] **Step 4: Modify `HomeScreen.kt`**

Read the current file first (from the Foundation & Auth plan) and replace its entire contents with:

```kotlin
package habitiq.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
    onSignOut: () -> Unit,
    onCreateFlat: () -> Unit,
    onJoinFlat: () -> Unit,
    onViewFlat: (flatId: String) -> Unit
) {
    val flatStatus by homeViewModel.flatStatus.collectAsStateWithLifecycleCompat()

    Column(modifier = Modifier.padding(24.dp)) {
        Text("Signed in as: ${user?.email ?: "unknown"}")
        Button(onClick = onSignOut) {
            Text("Sign out")
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
        }
    }
}
```

- [ ] **Step 5: Modify `HabitiqApp.kt`**

Read the current file first (from the Foundation & Auth plan) and apply these changes:

Add to the `Routes` object:

```kotlin
    const val CREATE_FLAT = "createFlat"
    const val JOIN_FLAT = "joinFlat"
    const val FLAT_HOME = "flatHome/{flatId}"
    fun flatHome(flatId: String) = "flatHome/$flatId"
```

Add these imports:

```kotlin
import androidx.navigation.NavType
import androidx.navigation.navArgument
import habitiq.app.flats.CreateFlatViewModel
import habitiq.app.flats.FlatHomeViewModel
import habitiq.app.flats.FlatsRepository
import habitiq.app.flats.HomeViewModel
import habitiq.app.flats.JoinFlatViewModel
import habitiq.app.flats.MembersRepository
import habitiq.app.ui.CreateFlatScreen
import habitiq.app.ui.FlatHomeScreen
import habitiq.app.ui.JoinFlatScreen
```

Add repository instances alongside the existing `authRepository`/`usersRepository`:

```kotlin
    val flatsRepository = remember { FlatsRepository() }
    val membersRepository = remember { MembersRepository() }
```

Replace the `composable(Routes.HOME) { ... }` block with:

```kotlin
                composable(Routes.HOME) {
                    val homeViewModel = remember { HomeViewModel(authRepository, usersRepository) }
                    HomeScreen(
                        user = currentUser,
                        homeViewModel = homeViewModel,
                        onSignOut = {
                            authRepository.signOut()
                            navController.navigate(Routes.LOGIN) { popUpTo(Routes.HOME) { inclusive = true } }
                        },
                        onCreateFlat = { navController.navigate(Routes.CREATE_FLAT) },
                        onJoinFlat = { navController.navigate(Routes.JOIN_FLAT) },
                        onViewFlat = { flatId -> navController.navigate(Routes.flatHome(flatId)) }
                    )
                }
                composable(Routes.CREATE_FLAT) {
                    val viewModel = remember { CreateFlatViewModel(authRepository, flatsRepository) }
                    CreateFlatScreen(
                        viewModel = viewModel,
                        onDone = { navController.popBackStack() }
                    )
                }
                composable(Routes.JOIN_FLAT) {
                    val viewModel = remember { JoinFlatViewModel(authRepository, flatsRepository) }
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
                    val viewModel = remember(flatId) { FlatHomeViewModel(flatId, flatsRepository, membersRepository) }
                    FlatHomeScreen(viewModel = viewModel, currentUid = currentUser?.uid.orEmpty())
                }
```

- [ ] **Step 6: Build and verify**

Run: `gradle assembleDebug --project-dir "C:\habitiq_jaswanth"`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 7: Commit**

```bash
git add app/src/main/kotlin/habitiq/app/flats/HomeViewModel.kt app/src/main/kotlin/habitiq/app/flats/FlatHomeViewModel.kt app/src/main/kotlin/habitiq/app/ui/FlatHomeScreen.kt app/src/main/kotlin/habitiq/app/ui/HomeScreen.kt app/src/main/kotlin/habitiq/app/HabitiqApp.kt
git commit -m "feat: wire Create/Join Flat and Flat Home into navigation"
```

---

### Task 10: Manual on-device verification, then tag

This task has no new source files — it verifies the full create/share/join loop against real production Firestore, using two real accounts (matching the pattern from the Foundation & Auth plan's Task 8).

- [ ] **Step 1: Install the current build**

Run: `gradle assembleDebug --project-dir "C:\habitiq_jaswanth"` then install the resulting APK on a connected device (`adb install -r app/build/outputs/apk/debug/app-debug.apk`).

- [ ] **Step 2: Manual test — create a flat**

Sign in with Account A (already created in the Foundation & Auth plan's testing). From Home, tap "Create a Flat," enter a name, tap Create.
Expected: shows "Your flat is ready!" with an invite code in `FLAT-XXXX` format. Verify in Firebase Console → Firestore: `flats/{code}` exists with `adminUid` = Account A's UID, `memberCount: 1`; `flats/{code}/members/{uid}` exists with `role: admin`.

- [ ] **Step 3: Manual test — share button**

Tap "Share invite code." Expected: the Android system share sheet opens, showing installed apps (WhatsApp, Messages, Gmail, etc.). Cancel out — no crash.

- [ ] **Step 4: Manual test — relaunch as the same admin**

Tap Done, sign out, sign back in as Account A. Expected: Home screen shows "View my flat" (not the Create/Join buttons) — confirms `activeFlatId` persisted and `HomeViewModel` correctly detects it. Tap it — expected: `FlatHomeScreen` shows the flat name, member count, the invite code + share button (since Account A is admin), and one member listed.

- [ ] **Step 5: Manual test — join with a second real account**

Sign out. Sign up or sign in with Account B (a different real email — one of Sai's friends). From Home, tap "Join with Code," enter the code from Step 2, tap Join.
Expected: navigates to `FlatHomeScreen` showing the same flat name, now 2 members listed, no invite code/share button shown (Account B is not admin). Verify in Firestore: `flats/{code}/members/{Account B's uid}` exists with `role: member`, and `flats/{code}.memberCount` is now `2`.

- [ ] **Step 6: Manual test — wrong/invalid code**

Sign out, sign in as a third account (or reuse one), tap "Join with Code," enter an obviously invalid code like `XXXX`.
Expected: shows "That doesn't look like a valid invite code." without attempting a Firestore call. Try a validly-formatted but nonexistent code like `FLAT-9999`. Expected: shows "Flat not found. Check the invite code and try again."

- [ ] **Step 7: Manual test — already a member**

While still signed in as Account B (already joined in Step 5), try joining the same code again.
Expected: shows "You are already a member of this flat."

- [ ] **Step 8: Tag this milestone**

```bash
git tag -a v0.2.0-flats-members -m "Create/share/join a flat working end to end, verified with 2 real accounts"
```

---

## Self-Review Notes

- **Spec coverage:** create flat ✅ Task 7, invite code ✅ Task 1/3, native share ✅ Task 6, join by code (instant) ✅ Task 8, member list ✅ Task 4/9, 8-member cap + already-member + not-found error cases ✅ Task 2/3, standing visual principle (icon-led, short labels) ✅ applied across Tasks 7-9's screen copy. Deferred items (approval-mode join, leave/kick/transfer-admin) intentionally absent — correct per spec.
- **Placeholder scan:** none found (`TBD`/`TODO` absent).
- **Type consistency:** `FlatsRepository.createFlat`/`joinFlat`/`getFlat` signatures match how Tasks 7-9 call them exactly (`Result<String>`, `Result<Unit>`, `Result<FlatInfo>`). `FlatUiState` shape matches `AuthUiState`'s established pattern (`Idle`/`Loading`/`Error(message)`/`Success`). `Member`/`FlatInfo` field names match what `FlatHomeScreen` reads (`nickname`, `role`, `name`, `memberCount`, `adminUid`, `id`).
- **Known carried-forward lessons applied directly** (not re-discovered by an implementer this time): `IllegalStateException(message, cause)` two-arg form used from the start in `FlatsRepository` (Task 4's fix from the prior plan); `LaunchedEffect`-gated navigation used from the start in `JoinFlatScreen` (Task 6's fix from the prior plan); `getActiveFlatId` reuses `UsersRepository`'s existing `firestore`/`.await()` imports rather than introducing a new pattern.
