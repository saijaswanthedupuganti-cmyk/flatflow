import Link from 'next/link'
import type { Metadata } from 'next'

export const metadata: Metadata = {
  title: 'Safety Tips — Habitiq',
  description: 'How to find a room or flatmate safely on Habitiq Discover — scams to avoid, safe viewings, and how to report.',
}

export default function SafetyPage() {
  return (
    <main className="min-h-screen bg-background text-foreground">
      <div className="max-w-2xl mx-auto px-6 py-16">

        {/* Header */}
        <div className="mb-10">
          <Link href="/" className="text-sm text-muted-foreground hover:text-foreground mb-6 inline-block">
            ← Back to Habitiq
          </Link>
          <h1 className="text-3xl font-bold tracking-tight mb-2">Safety Tips</h1>
          <p className="text-muted-foreground text-sm">
            For Discover — finding a room, a flat, or a flatmate · Last updated: 3 October 2026
          </p>
        </div>

        <div className="prose prose-sm dark:prose-invert max-w-none space-y-8 text-foreground">

          {/* Intro */}
          <section>
            <p className="text-muted-foreground leading-relaxed">
              Discover (in the Oddroof app) helps you find rooms and flatmates. Most people you meet are genuine — but
              housing scams and unsafe situations are common in India, especially on WhatsApp and Facebook groups.
              These tips take two minutes to read and can save you money and stress.
            </p>
            <div className="mt-4 rounded-lg border border-border p-4">
              <p className="text-foreground font-medium">
                Habitiq never collects rent, deposits, or booking fees.
              </p>
              <p className="text-muted-foreground mt-1">
                Anyone asking you to pay &quot;through Habitiq&quot; or to &quot;block the room&quot; before you have
                seen it is not acting for us.
              </p>
            </div>
          </section>

          <hr className="border-border" />

          {/* Money */}
          <section>
            <h2 className="text-lg font-semibold mb-3">1. Never pay before viewing</h2>
            <ul className="text-muted-foreground space-y-2 list-disc list-inside">
              <li>Don&apos;t send a token amount, deposit, or rent by UPI, bank transfer, or gift card before you have visited the room in person.</li>
              <li>Don&apos;t accept &quot;overpayments&quot; and refund the difference — the original payment usually bounces.</li>
              <li>Get the rent, deposit, and notice period in writing before you pay anything.</li>
              <li>Pay only the person you have met and confirmed is the owner, the owner&apos;s representative, or a current flatmate.</li>
            </ul>
          </section>

          {/* Red flags */}
          <section>
            <h2 className="text-lg font-semibold mb-3">2. Red flags</h2>
            <ul className="text-muted-foreground space-y-2 list-disc list-inside">
              <li>Rent far below other listings in the same area</li>
              <li>Pressure to decide or pay today (&quot;three others are interested&quot;)</li>
              <li>Refuses a video call or an in-person visit, or says they are &quot;out of town&quot;</li>
              <li>Photos that look like a hotel or stock image, or details that change between messages</li>
              <li>Wants to move the conversation to WhatsApp or phone straight away</li>
              <li>Asks for your Aadhaar, PAN, OTPs, or bank details early in the chat</li>
              <li>Romantic or sexual messages, or comments about your appearance</li>
            </ul>
          </section>

          {/* Chat */}
          <section>
            <h2 className="text-lg font-semibold mb-3">3. Keep the chat in the app</h2>
            <ul className="text-muted-foreground space-y-2 list-disc list-inside">
              <li>Use in-app messaging until you have met and feel comfortable. It keeps a record and lets you report or block.</li>
              <li>Don&apos;t share your phone number, home address, workplace, or ID documents in early messages.</li>
              <li>Never share an OTP with anyone — no genuine flatmate or owner needs it.</li>
            </ul>
          </section>

          {/* Viewings */}
          <section>
            <h2 className="text-lg font-semibold mb-3">4. Safe viewings</h2>
            <ul className="text-muted-foreground space-y-2 list-disc list-inside">
              <li>Visit in daylight, and bring a friend if you can.</li>
              <li>Tell someone where you are going and share your live location with them.</li>
              <li>For a first meeting with a possible flatmate, meet somewhere public first.</li>
              <li>Trust your instincts — you can leave at any point.</li>
              <li>Check the room, water, power, locks, and who else lives there before you agree.</li>
            </ul>
          </section>

          {/* Listing your room */}
          <section>
            <h2 className="text-lg font-semibold mb-3">5. If you are listing a room</h2>
            <ul className="text-muted-foreground space-y-2 list-disc list-inside">
              <li>Only list a room that is genuinely available and that you are allowed to let.</li>
              <li>Use your own recent photos. Don&apos;t include your flat number, invite code, or documents in photos.</li>
              <li>Ask to see ID at the viewing, not in chat, and follow local police-verification rules for new tenants.</li>
              <li>If you set a gender preference, it must not be used to exclude transgender persons.</li>
            </ul>
          </section>

          {/* Report */}
          <section>
            <h2 className="text-lg font-semibold mb-3">6. Report and block</h2>
            <p className="text-muted-foreground leading-relaxed">
              In the app, open the profile, listing, or chat and choose <strong className="text-foreground">Report</strong> or{' '}
              <strong className="text-foreground">Block</strong>. Blocking hides that person from you and stops
              new messages. Reports are reviewed by a person on our team. You can also email{' '}
              <a href="mailto:hello@habitiq.app" className="text-violet-500 hover:underline">hello@habitiq.app</a>{' '}
              with the subject &quot;Safety report&quot; and screenshots.
            </p>
            <p className="text-muted-foreground leading-relaxed mt-2">
              We may remove listings and suspend accounts that break our{' '}
              <Link href="/terms" className="text-violet-500 hover:underline">Terms of Service</Link>.
            </p>
          </section>

          {/* Emergency */}
          <section>
            <h2 className="text-lg font-semibold mb-3">7. If you are in danger or have lost money</h2>
            <p className="text-muted-foreground mb-3">Habitiq is not an emergency service. In India, contact:</p>
            <table className="w-full text-sm text-muted-foreground border-collapse">
              <tbody className="divide-y divide-border">
                <tr>
                  <td className="py-2 pr-4 font-medium text-foreground">112</td>
                  <td className="py-2">Emergency (police, ambulance, fire)</td>
                </tr>
                <tr>
                  <td className="py-2 pr-4 font-medium text-foreground">181</td>
                  <td className="py-2">Women helpline (most states)</td>
                </tr>
                <tr>
                  <td className="py-2 pr-4 font-medium text-foreground">1930</td>
                  <td className="py-2">Cyber fraud helpline — call quickly if you sent money to a scammer</td>
                </tr>
                <tr>
                  <td className="py-2 pr-4 font-medium text-foreground">cybercrime.gov.in</td>
                  <td className="py-2">National Cyber Crime Reporting Portal</td>
                </tr>
              </tbody>
            </table>
          </section>

          <hr className="border-border" />

          <p className="text-xs text-muted-foreground">
            Habitiq · habitiq.app · India ·{' '}
            <Link href="/privacy" className="text-violet-500 hover:underline">Privacy Policy</Link> ·{' '}
            <Link href="/terms" className="text-violet-500 hover:underline">Terms of Service</Link>
          </p>
        </div>
      </div>
    </main>
  )
}
