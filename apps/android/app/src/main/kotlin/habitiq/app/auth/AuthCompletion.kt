package habitiq.app.auth

import habitiq.app.analytics.AppAnalytics
import habitiq.app.data.UserProfile
import habitiq.app.data.UsersRepository

internal suspend fun completeAuthFlow(
    authRepository: AuthRepository,
    usersRepository: UsersRepository,
    analytics: AppAnalytics,
    method: String,
    isSignup: Boolean
): AuthUiState {
    val user = authRepository.currentUser.value
        ?: return AuthUiState.Error("Signed in, but session was lost. Please try again.")

    val profileResult = usersRepository.ensureUserDocument(
        UserProfile(uid = user.uid, email = user.email.orEmpty(), displayName = user.displayName)
    )

    return profileResult.fold(
        onSuccess = {
            if (isSignup) analytics.logSignUp(method) else analytics.logLogin(method)
            val hasFlat = usersRepository.getActiveFlatId(user.uid).getOrNull() != null
            AuthUiState.Success(hasActiveFlat = hasFlat)
        },
        onFailure = {
            val msg = if (isSignup) {
                "Account created, but we couldn't set up your profile. Please try again."
            } else {
                "Signed in, but we couldn't load your profile. Please try again."
            }
            AuthUiState.Error(msg)
        }
    )
}
