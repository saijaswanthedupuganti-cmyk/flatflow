'use client'
import { usePathname } from 'next/navigation'
import PhoneFrame from './mockups/PhoneFrame'
import HomeScreen from './mockups/HomeScreen'
import DiscoverFlatScreen from './mockups/DiscoverFlatScreen'
import VoiceScreen from './mockups/VoiceScreen'

const PHONES: Record<string, { label: string; Screen: () => React.ReactElement }> = {
  '/discover': { label: 'Discover: flat listings in Hyderabad with rent per head and a connect button', Screen: DiscoverFlatScreen },
  '/flat-manager': { label: "Habitiq home: your flat, tasks for you, what you owe and today's tasks", Screen: HomeScreen },
  '/voice': { label: 'Voice assistant: "I just spent 500 on groceries" becomes a ₹500 Groceries expense to save', Screen: VoiceScreen },
}

export default function PageHeroPhone({ className }: { className?: string }) {
  const path = usePathname()
  const entry = path ? PHONES[path.replace(/\/+$/, '') || '/'] : undefined
  if (!entry) return null
  return <div className={className}><PhoneFrame label={entry.label}><entry.Screen /></PhoneFrame></div>
}
