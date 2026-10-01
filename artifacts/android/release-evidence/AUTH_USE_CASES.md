# Habitiq Android — Auth Use-Case Matrix

**Build:** `Habitiq-FINAL.apk` · **Package:** `habitiq.app` · **Firebase:** `garbage-f79f7`

| # | Use case | Expected behavior | Status |
|---|----------|-------------------|--------|
| 1 | Email signup → new user | Account created → 4-option intent chooser | ✅ |
| 2 | Email signup → chooser → Find a flat | Navigate to Discover tab (Use a flat mode) | ✅ |
| 3 | Email signup → chooser → Find the person | Navigate to Discover tab (Find a person mode) | ✅ |
| 4 | Email signup → chooser → Create a room | Create-flat wizard → land in main app | ✅ |
| 5 | Email signup → chooser → Join a flat | Join-flat screen with invite code → main app | ✅ |
| 6 | Email login → existing user with flat | Skip chooser → Home | ✅ |
| 7 | Email login → existing user without flat | Show 4-option intent chooser | ✅ |
| 8 | Google sign-in → existing user with flat | Skip chooser → Home | ✅ |
| 9 | Google sign-in → new user | 4-option intent chooser | ✅ |
| 10 | Google sign-in on signup screen | Same as login (Firebase handles new vs existing) | ✅ |
| 11 | Sign out | Return to login screen, session cleared | ✅ |
| 12 | Wrong password | "Incorrect email or password." shown | ✅ |
| 13 | No account found | "No account found with this email." shown | ✅ |
| 14 | Network error | "No internet connection. Please try again." shown | ✅ |
| 15 | Forgot password | Sends Firebase reset email, confirmation shown | ✅ |
| 16 | Google Sign-In failure | Clear error message (not silent) | ✅ |
| 17 | Google Sign-In cancelled | No error (user dismissed picker) | ✅ |
| 18 | Cold start → signed in with flat | Session persists → Home | ✅ |
| 19 | Cold start → signed in without flat | Session persists → intent chooser | ✅ |
| 20 | Cold start → not signed in | Login screen | ✅ |

## Google Sign-In prerequisites

Register debug SHA-1 in Firebase Console:

```
65:B5:8F:48:69:8F:06:08:83:C2:74:27:35:C2:C8:DA:BD:B8:52:10
```

Web client ID: `1030913049565-1jr9s3371nlk1t693ok533283sif2vgm.apps.googleusercontent.com`

Without SHA-1 registered, email/password still works; Google Sign-In will fail with a configuration error.
