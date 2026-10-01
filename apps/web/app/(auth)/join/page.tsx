import { redirect } from 'next/navigation'

// The join-a-flat flow lives in /onboarding (step: 'join').
// This route is kept for backwards-compat but redirects there.
export default function JoinRedirect() {
  redirect('/onboarding')
}
