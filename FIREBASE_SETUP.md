# Firebase Setup Guide for FlatFlow

This guide walks you through connecting FlatFlow to a real Firebase project so Google login and live data sync works for all roommates.

---

## Step 1 — Create a Firebase Project

1. Go to **[https://console.firebase.google.com](https://console.firebase.google.com)**
2. Click **"Add project"**
3. Name it `flatflow` (or anything you like)
4. Disable Google Analytics if you don't need it → Click **"Create project"**

---

## Step 2 — Add a Web App

1. In your project, click the **`</>`** (Web) icon to add a web app
2. Register app name: `flatflow-web`
3. **Do NOT** check "Firebase Hosting" (we use Next.js)
4. Click **"Register app"**
5. Firebase shows you a `firebaseConfig` object — **copy all the values**

---

## Step 3 — Enable Authentication

1. In the sidebar → **Build → Authentication → Get started**
2. Click **"Sign-in method"** tab
3. Enable **Google** → Set your project's public-facing name → Save
4. Enable **Email/Password** → Save

---

## Step 4 — Enable Firestore

1. In the sidebar → **Build → Firestore Database → Create database**
2. Choose **"Start in test mode"** (we'll add proper rules next)
3. Select a region close to you → **"Enable"**

---

## Step 5 — Deploy Firestore Security Rules

The file `firestore.rules` in this project has the correct rules. Deploy them:

```bash
# Install Firebase CLI (already done if you ran the setup)
npm install -g firebase-tools

# Login to Firebase
firebase login

# Init Firebase in this project
cd C:\garbage
firebase init firestore

# When asked, select your project and accept defaults
# Then deploy rules:
firebase deploy --only firestore:rules
```

---

## Step 6 — Create your `.env.local` file

Copy `.env.local.example` to `.env.local` and fill in your values:

```
NEXT_PUBLIC_FIREBASE_API_KEY=AIza...your_api_key...
NEXT_PUBLIC_FIREBASE_AUTH_DOMAIN=your-project-id.firebaseapp.com
NEXT_PUBLIC_FIREBASE_PROJECT_ID=your-project-id
NEXT_PUBLIC_FIREBASE_STORAGE_BUCKET=your-project-id.firebasestorage.app
NEXT_PUBLIC_FIREBASE_MESSAGING_SENDER_ID=123456789
NEXT_PUBLIC_FIREBASE_APP_ID=1:123456789:web:abcdef123456
```

All these values come from the `firebaseConfig` object in Step 2.

---

## Step 7 — Add Authorized Domains (for Google Login)

1. Firebase Console → **Authentication → Settings → Authorized domains**
2. Add `localhost` (should already be there)
3. When you deploy, add your production domain too

---

## Step 8 — Restart the Dev Server

```bash
# Stop the current server (Ctrl+C), then:
npm run dev
```

The green shield icon on the login page will confirm Firebase is connected.

---

## How the App Works After Setup

| Action | What Happens |
|--------|-------------|
| First login (Google/email) | User is created in Firebase Auth |
| "Create a Flat" | A new flat is created in Firestore, you become admin |
| "Join a Flat" | You're added to that flat as a member |
| Tasks / swap requests | Real-time sync via Firestore listeners |
| Any roommate marks a task done | Everyone sees it instantly |

---

## Troubleshooting

**Google login popup blocked**: Allow popups for `localhost` in your browser.

**"Firebase: Error (auth/unauthorized-domain)"**: Add `localhost` to authorized domains in Firebase Console → Authentication → Settings.

**"Missing or insufficient permissions"**: Deploy the Firestore rules from Step 5.

**Data not loading**: Check your `.env.local` — all 6 keys must be present and correct.
