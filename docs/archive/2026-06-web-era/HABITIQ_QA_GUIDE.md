# Habitiq — QA Tester Guide
**Version:** 1.0 | **Date:** June 2026 | **App:** https://garbage-liart.vercel.app

---

## Welcome

This guide is for anyone testing Habitiq. It covers what the product does, how to set up a test environment, every feature to test, what "passing" looks like, and how to report issues.

Read Section 1 and 2 first. Then go feature by feature using the checklists in Section 4.

---

## 1. What Is Habitiq?

Habitiq manages shared living duties for flatmates. The core of the product is a **rotation engine** — an algorithm that automatically assigns tasks to flatmates in turn, skips people who are away, and tracks who completed what.

**Key concepts to understand before testing:**

| Term | What It Means |
|------|--------------|
| **Flat** | A group of people sharing a living space in Habitiq |
| **Admin** | The person who created the flat — can create/delete tasks and manage members |
| **Member** | A regular flatmate — can complete tasks, request swaps, view everything |
| **Rotation queue** | The ordered list of who does a task next, next after that, and so on |
| **Out of Station (OOS)** | A member who has marked themselves as temporarily away — skipped in rotation |
| **Swap request** | A formal request from one member asking another to cover their task |
| **Recurring bill** | A repeating expense (e.g. electricity) with a rotating payer |
| **Reliability score** | A percentage score per member based on how often they complete tasks on time |

---

## 2. Test Environment Setup

### What You Need
- A laptop or desktop browser (Chrome recommended for primary testing)
- A mobile phone browser (Chrome on Android or Safari on iOS for mobile testing)
- Two Google accounts OR two email addresses — you need at least 2 users to test multi-member features

### How to Set Up a Test Flat

**User A (Admin):**
1. Go to https://garbage-liart.vercel.app
2. Sign in with Google or create an account with email/password
3. Select "Create a flat"
4. Enter a flat name (e.g. "QA Test Flat")
5. Copy the invite code shown on screen

**User B (Member):**
1. Open a different browser or incognito window
2. Go to https://garbage-liart.vercel.app
3. Sign in with a different account
4. Select "Join a flat"
5. Enter the invite code from User A

Now you have a 2-person flat. For fuller testing, add a 3rd user with a 3rd account.

### Test Data Suggestions
- Create 3–4 tasks with different frequencies (one daily, one weekly, one monthly)
- Add tasks with different emojis and priorities
- Add a recurring bill (electricity, ₹2000, monthly, day 1)
- Add a one-off expense (groceries, ₹500, equal split)

---

## 3. Devices and Browsers to Test

| Priority | Device | Browser |
|----------|--------|---------|
| P1 | Android mobile | Chrome |
| P1 | Desktop | Chrome |
| P2 | iPhone | Safari |
| P2 | Desktop | Firefox |
| P3 | iPad | Safari |
| P3 | Desktop | Edge |

For each bug found, note which device and browser you were using.

---

## 4. Feature-by-Feature Test Checklists

---

### 4.1 Authentication

**Sign-in flows:**
- [ ] Sign in with Google (desktop) — lands on dashboard or onboarding
- [ ] Sign in with Google (mobile) — no popup blocker issues
- [ ] Sign in with email/password — works with valid credentials
- [ ] Sign in with wrong password — shows a friendly error (not a Firebase error code)
- [ ] Sign in with email that doesn't exist — shows a friendly error
- [ ] Session persists — close and reopen browser, user stays logged in
- [ ] Sign out — user is returned to login page, session cleared

**What passing looks like:**
- No raw Firebase error codes visible to the user (e.g. "auth/user-not-found" should NOT appear)
- Redirects are correct after login (new users → onboarding, existing users with flat → dashboard)

---

### 4.2 Onboarding — Create Flat

- [ ] Enter a flat name and submit — flat is created, invite code appears
- [ ] Invite code is copyable
- [ ] Invite code sharing works (share button/link)
- [ ] Admin is taken to dashboard after creation
- [ ] Flat name appears in the dashboard header

---

### 4.3 Onboarding — Join Flat

- [ ] Enter correct invite code → joins flat, lands on dashboard
- [ ] Enter wrong/invalid invite code → clear error message, does not crash
- [ ] Enter invite code for a flat you already belong to → handled gracefully (no duplicate join)

---

### 4.4 Task Management (Admin)

- [ ] Admin can create a task — name, emoji, frequency, priority, start date
- [ ] Task appears on dashboard after creation
- [ ] Admin can edit a task name and emoji
- [ ] Admin can delete a task — task disappears for all members
- [ ] Non-admin (member) does NOT see create/delete controls
- [ ] Task with daily frequency — due date is correct (today or next day)
- [ ] Task with weekly frequency — due date is 7 days from start
- [ ] Task with fortnightly frequency — due date is 14 days from start
- [ ] Task with monthly frequency — due date is ~30 days from start
- [ ] Overdue task — visually distinct from pending and completed tasks

---

### 4.5 Task Completion

- [ ] Member can mark their assigned task as complete — one tap
- [ ] Task moves to completed state visually
- [ ] Next person in the rotation queue becomes the assignee
- [ ] Activity log records the completion event
- [ ] Completing a task not assigned to you — test that this is restricted (only the assignee should complete it, or admin)
- [ ] Overdue task — completing it clears the overdue flag

---

### 4.6 Rotation Queue

- [ ] Rotation Order card shows all members in queue order for each task
- [ ] Current assignee is highlighted with "YOU" badge on their own view
- [ ] Queue updates correctly after completion — next person moves to top
- [ ] New member added to flat — appears in rotation queue for ALL tasks

---

### 4.7 Out of Station (OOS)

- [ ] Member can mark themselves as Out of Station
- [ ] OOS member is skipped in rotation — next in-station member is assigned
- [ ] OOS status is visible to all flat members
- [ ] Member marks themselves as returned — re-enters rotation at correct position (not bumped to back)
- [ ] Admin can mark another member as OOS
- [ ] Test with all members OOS — edge case: should not crash, should show appropriate state

---

### 4.8 Swap Requests

- [ ] Member A can request a swap from Member B for their assigned task
- [ ] Member B sees a pending banner on their dashboard
- [ ] Member B navigates to Swaps page — request appears in "Received" column
- [ ] Member B accepts → task assignment transfers to Member B, Member A's swap shows "Accepted"
- [ ] Member B declines → swap shows "Declined" for Member A
- [ ] Swaps page — Received and Sent columns are side by side on desktop
- [ ] Swaps page — Received and Sent stack vertically on mobile
- [ ] Accepted/declined swaps remain in history (not deleted)
- [ ] Member cannot swap a task that is not assigned to them

---

### 4.9 Admin Controls

- [ ] Admin sees "Org View" — all tasks, all members visible
- [ ] Admin can manually override task assignment
- [ ] Admin sees invite code and can share it
- [ ] Admin can transfer admin role to another member
- [ ] After transfer, previous admin becomes a regular member (loses admin controls)
- [ ] New admin sees admin controls

---

### 4.10 Member Management

**Voluntary leave:**
- [ ] Member can leave the flat from Settings
- [ ] On leave, member is removed from all rotation queues
- [ ] If leaving member is admin and NOT the last member → must transfer admin role first
- [ ] System prompts for role transfer before allowing leave
- [ ] If leaving member is the last member → flat is deleted entirely

**Kick member (admin):**
- [ ] Admin can kick a member from the Members page
- [ ] Kicked member is removed from rotation queues immediately
- [ ] Kicked member sees a clean "you've been removed" message when they next open the app
- [ ] Kicked member cannot access the flat's data after being kicked

---

### 4.11 Multi-Flat Support

- [ ] User can create a second flat while in the first
- [ ] User can join a second flat while in the first
- [ ] Flat Switcher dropdown appears when user belongs to 2+ flats
- [ ] Switching flats — dashboard, tasks, members all update instantly without logout
- [ ] Each flat's data is isolated (tasks in Flat A do not appear in Flat B)

---

### 4.12 Analytics

- [ ] Completion grid shows correct data for each member
- [ ] Reliability score updates after task completions
- [ ] Reliability score is visible per member
- [ ] New flat with no completions — analytics page shows empty state (no crash)

---

### 4.13 Calendar View

- [ ] Calendar shows all tasks for the current month
- [ ] Completed tasks appear differently from pending
- [ ] Filter by member — calendar shows only that member's tasks
- [ ] Navigate to previous/next month

---

### 4.14 Activity Log

- [ ] Task completion appears in log immediately
- [ ] Member joining appears in log
- [ ] Member leaving appears in log
- [ ] Swap accepted/declined appears in log
- [ ] Log is visible to all members
- [ ] Log cannot be edited or deleted (read-only)

---

### 4.15 Bills & Expenses Module

**Recurring bills:**
- [ ] Admin can create a recurring bill — name, amount, frequency, billing day, rotation or fixed payer
- [ ] Bill appears in the Expenses page
- [ ] Bill with billing day > 28 (e.g. day 30) — is handled correctly in short months (does not break in February)
- [ ] Edit a recurring bill — changes save correctly
- [ ] Delete a recurring bill — removed from list

**One-off expenses:**
- [ ] Any member can add a one-off expense — name, amount, currency, who paid, who splits
- [ ] Equal split — balances calculated correctly
- [ ] Custom split — amounts must total the expense amount
- [ ] Multi-currency: INR, USD, EUR, GBP, AED, SGD, AUD — all selectable
- [ ] Balance tracker shows correct amounts owed

**Settle Up:**
- [ ] Settle Up flow marks a balance as settled
- [ ] After settling, balances update correctly

---

### 4.16 Real-Time Sync

This requires two browsers/devices logged into the same flat.

- [ ] Member A completes a task — Member B sees the update without refreshing
- [ ] Admin creates a task — all members see it appear immediately
- [ ] Member joins flat — admin sees new member appear immediately
- [ ] Activity log updates in real-time

---

### 4.17 Progressive Web App (PWA)

*(Android Chrome only)*

- [ ] Install prompt appears after using the app for a few seconds
- [ ] App installs to home screen
- [ ] Installed app opens without browser bar (standalone mode)
- [ ] App icon appears correctly on home screen

---

### 4.18 UI and Responsiveness

- [ ] Mobile — bottom navigation bar is visible and functional
- [ ] Desktop — sidebar is visible and functional
- [ ] Dark mode — all pages readable, no invisible text
- [ ] Light mode — all pages readable, no invisible text
- [ ] Toggle between dark and light — works on all pages
- [ ] No horizontal scroll on mobile (no layout overflow)
- [ ] Text is not clipped or truncated unexpectedly

---

### 4.19 Edge Cases to Specifically Test

| Scenario | What to Test |
|----------|-------------|
| Single-member flat | Create flat, don't invite anyone. All task/rotation flows should handle 1 person. |
| All members OOS | Mark everyone Out of Station. Should not crash. |
| Very long flat name | Enter 100+ character flat name. Should not break layout. |
| Very long task name | Enter 100+ character task name. Should truncate gracefully. |
| Flat with 6 members | Add maximum members. Performance and layout should hold. |
| Rapid taps on Complete | Tap complete button quickly multiple times. Should not duplicate completions. |
| Joining with expired/wrong code | Enter an invalid join code. Should show error, not crash. |

---

## 5. How to Report a Bug

When you find something wrong, report it with:

**Bug Report Format:**
```
Title: [short description]
Steps to reproduce:
  1. [step 1]
  2. [step 2]
  3. [step 3]
Expected result: [what should happen]
Actual result: [what actually happened]
Device: [e.g. iPhone 14, Chrome Android, Desktop Chrome]
Screenshot/screen recording: [attach if possible]
Severity: Critical / High / Medium / Low
```

**Severity guide:**
| Level | Meaning |
|-------|---------|
| Critical | App crashes, data is lost, user is locked out |
| High | Feature doesn't work at all, no workaround |
| Medium | Feature works partially, workaround exists |
| Low | Visual/cosmetic issue, minor UX friction |

---

## 6. What Is Out of Scope for QA Right Now

The following are known limitations — do not report these as bugs:

- Push notifications — not built yet
- WhatsApp reminders — not built yet
- Offline mode — PWA has a basic offline page but full offline sync is not built
- iOS PWA install — Android only currently
- Password reset — UI not built yet (email/password users cannot reset via app)
- Task history beyond 30 days — not archived yet

---

## 7. Known Issues (Already Logged)

| ID | Description | Status |
|----|-------------|--------|
| BUG-1 | NPS banner dismiss | Fixed |
| BUG-3 | Recurring bill edits not saving | Fixed |
| BUG-4 | Billing day 29–31 not working in short months | Fixed |
| ENH-2 | Swap requests layout | Fixed |

---

## 8. Contact

If you have questions about expected behaviour or need access to a test flat, contact:

**Venkata Sai Jaswanth E**
saijaswanthedupuganti@gmail.com

---

*Last updated: 2026-06-05 | Habitiq v0.1.0*
