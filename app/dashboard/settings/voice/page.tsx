"use client"
import Link from 'next/link'
import { ArrowLeft } from 'lucide-react'
import VoiceSettings from '@/components/VoiceSettings'

export default function VoiceSettingsPage() {
  return (
    <div className="space-y-6 max-w-2xl mx-auto">
      <div>
        <Link 
          href="/dashboard/settings" 
          className="inline-flex items-center gap-1.5 text-sm text-muted-foreground hover:text-foreground mb-4 transition-colors"
        >
          <ArrowLeft size={16} />
          Back to Settings
        </Link>
        <h1 className="text-3xl font-bold tracking-tight">Voice Assistant</h1>
        <p className="text-muted-foreground mt-1">Configure your voice preferences and interactions.</p>
      </div>

      <VoiceSettings />
    </div>
  )
}
