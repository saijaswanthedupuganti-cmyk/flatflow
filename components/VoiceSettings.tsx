"use client"
import { useState, useEffect } from 'react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Switch } from '@/components/ui/switch'
import { Mic, Volume2, MessageSquare, Lightbulb } from 'lucide-react'

export default function VoiceSettings() {
  const [voiceEnabled, setVoiceEnabled] = useState(true)
  const [ttsEnabled, setTtsEnabled] = useState(true)
  const [showTranscript, setShowTranscript] = useState(true)

  useEffect(() => {
    if (typeof window === 'undefined') return
    setVoiceEnabled(localStorage.getItem('habitiq-voice') !== 'false')
    setTtsEnabled(localStorage.getItem('habitiq-voice-tts') !== 'false')
    setShowTranscript(localStorage.getItem('habitiq-voice-transcript') !== 'false')
  }, [])

  const handleVoiceToggle = (checked: boolean) => {
    setVoiceEnabled(checked)
    if (typeof window !== 'undefined') {
      localStorage.setItem('habitiq-voice', checked ? 'true' : 'false')
      // Let the app know the preference changed without a reload if possible
      window.dispatchEvent(new Event('storage'))
    }
  }

  const handleTtsToggle = (checked: boolean) => {
    setTtsEnabled(checked)
    if (typeof window !== 'undefined') {
      localStorage.setItem('habitiq-voice-tts', checked ? 'true' : 'false')
    }
  }

  const handleTranscriptToggle = (checked: boolean) => {
    setShowTranscript(checked)
    if (typeof window !== 'undefined') {
      localStorage.setItem('habitiq-voice-transcript', checked ? 'true' : 'false')
    }
  }

  return (
    <div className="space-y-6">
      <Card className="shadow-sm">
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Mic size={20} className="text-violet-500" />
            Voice Assistant
          </CardTitle>
          <CardDescription>Configure how you interact with Habitiq using your voice.</CardDescription>
        </CardHeader>
        <CardContent className="space-y-6">
          <div className="flex items-center justify-between">
            <div className="space-y-0.5">
              <label className="text-sm font-semibold">Enable Voice Assistant</label>
              <p className="text-xs text-muted-foreground">Show the microphone button in the navigation bar.</p>
            </div>
            <Switch checked={voiceEnabled} onCheckedChange={handleVoiceToggle} />
          </div>

          <div className="flex items-center justify-between">
            <div className="space-y-0.5">
              <label className="text-sm font-semibold flex items-center gap-1.5">
                <Volume2 size={14} className="text-muted-foreground" /> Speak Responses
              </label>
              <p className="text-xs text-muted-foreground">The assistant will speak out loud when completing tasks.</p>
            </div>
            <Switch checked={ttsEnabled} onCheckedChange={handleTtsToggle} disabled={!voiceEnabled} />
          </div>

          <div className="flex items-center justify-between">
            <div className="space-y-0.5">
              <label className="text-sm font-semibold flex items-center gap-1.5">
                <MessageSquare size={14} className="text-muted-foreground" /> Show Transcripts
              </label>
              <p className="text-xs text-muted-foreground">Display text transcripts of your spoken commands.</p>
            </div>
            <Switch checked={showTranscript} onCheckedChange={handleTranscriptToggle} disabled={!voiceEnabled} />
          </div>
        </CardContent>
      </Card>

      <Card className="shadow-sm border-dashed">
        <CardHeader className="pb-3">
          <CardTitle className="flex items-center gap-2 text-base">
            <Lightbulb size={18} className="text-amber-500" />
            Things you can say
          </CardTitle>
        </CardHeader>
        <CardContent>
          <ul className="space-y-3">
            <li className="flex items-start gap-2">
              <span className="text-violet-500 font-bold leading-none mt-0.5">·</span>
              <p className="text-sm text-foreground">"Kitchen is done"</p>
            </li>
            <li className="flex items-start gap-2">
              <span className="text-violet-500 font-bold leading-none mt-0.5">·</span>
              <p className="text-sm text-foreground">"I spent 500 on groceries"</p>
            </li>
            <li className="flex items-start gap-2">
              <span className="text-violet-500 font-bold leading-none mt-0.5">·</span>
              <p className="text-sm text-foreground">"How much does Alex owe me?"</p>
            </li>
            <li className="flex items-start gap-2">
              <span className="text-violet-500 font-bold leading-none mt-0.5">·</span>
              <p className="text-sm text-foreground">"What are my tasks for today?"</p>
            </li>
            <li className="flex items-start gap-2">
              <span className="text-violet-500 font-bold leading-none mt-0.5">·</span>
              <p className="text-sm text-foreground">"Can someone cover my task?"</p>
            </li>
          </ul>
        </CardContent>
      </Card>
    </div>
  )
}
