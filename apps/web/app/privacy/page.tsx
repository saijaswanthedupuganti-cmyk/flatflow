import Link from 'next/link'
import type { Metadata } from 'next'

export const metadata: Metadata = {
  title: 'Privacy Policy — Habitiq',
  description: 'How Habitiq collects, uses, and protects your personal data.',
}

export default function PrivacyPage() {
  return (
    <main className="min-h-screen bg-background text-foreground">
      <div className="max-w-2xl mx-auto px-6 py-16">

        {/* Header */}
        <div className="mb-10">
          <Link href="/" className="text-sm text-muted-foreground hover:text-foreground mb-6 inline-block">
            ← Back to Habitiq
          </Link>
          <h1 className="text-3xl font-bold tracking-tight mb-2">Privacy Policy</h1>
          <p className="text-muted-foreground text-sm">
            Effective date: 13 June 2026 · Last updated: 3 October 2026
          </p>
        </div>

        <div className="prose prose-sm dark:prose-invert max-w-none space-y-8 text-foreground">

          {/* Intro */}
          <section>
            <p className="text-muted-foreground leading-relaxed">
              Habitiq is a shared living platform built for flats, PGs, and co-living spaces in India. It covers
              flat management (tasks, expenses, bills) and <strong className="text-foreground">Discover</strong>,
              where people list rooms and find flatmates. Our Android app is called{' '}
              <strong className="text-foreground">Oddroof</strong>; it is the same service, and this policy covers
              it, the website, and the web app.
            </p>
            <p className="text-muted-foreground leading-relaxed mt-3">
              This policy explains what personal data we collect, why, who can see it, and what rights you have
              over it. We try to say this plainly — not in a way designed to confuse you. It complies with
              India&apos;s{' '}
              <strong className="text-foreground">Digital Personal Data Protection Act, 2023 (DPDP Act)</strong>{' '}
              and the DPDP Rules, 2025.
            </p>
          </section>

          <hr className="border-border" />

          {/* Who we are */}
          <section>
            <h2 className="text-lg font-semibold mb-3">1. Who We Are</h2>
            <p className="text-muted-foreground leading-relaxed">
              Habitiq is operated by its founders, Venkata Sai Jaswanth E and Upputuri Bhanu Kalyan, based in India.
              We are the Data Fiduciary under the DPDP Act — the people responsible for how your data is handled.
            </p>
            <p className="text-muted-foreground leading-relaxed mt-2">
              Contact us at:{' '}
              <a href="mailto:hello@habitiq.app" className="text-violet-500 hover:underline">
                hello@habitiq.app
              </a>
            </p>
          </section>

          {/* What we collect */}
          <section>
            <h2 className="text-lg font-semibold mb-3">2. What Data We Collect</h2>

            <h3 className="font-medium mb-2">Account data</h3>
            <ul className="text-muted-foreground space-y-1 list-disc list-inside">
              <li>Email address</li>
              <li>Display name</li>
              <li>Profile photo URL (if you sign in with Google)</li>
            </ul>
            <p className="text-muted-foreground text-sm mt-2">
              We do not ask for your phone number, date of birth, government ID, or any payment information.
            </p>

            <h3 className="font-medium mb-2 mt-5">Flat and activity data</h3>
            <ul className="text-muted-foreground space-y-1 list-disc list-inside">
              <li>Flat name and invite code</li>
              <li>Task names, frequencies, and completion records</li>
              <li>Swap requests between members</li>
              <li>Activity log entries (who did what and when)</li>
              <li>Member roles (admin or member) and out-of-station status</li>
              <li>NPS survey responses (if you choose to submit one)</li>
            </ul>

            <h3 className="font-medium mb-2 mt-5">Expense and financial data</h3>
            <ul className="text-muted-foreground space-y-1 list-disc list-inside">
              <li>Expense descriptions, amounts, and who paid</li>
              <li>How an expense is split among members</li>
              <li>Settlement records between members</li>
              <li>Recurring bill configurations and payment status</li>
            </ul>
            <p className="text-muted-foreground text-sm mt-2">
              We do not process any actual payments. Habitiq records that a settlement happened (e.g., &quot;Rahul paid
              ₹2,000 via UPI&quot;) but no money moves through our platform.
            </p>

            <h3 className="font-medium mb-2 mt-5">Discover data (only if you use Discover)</h3>
            <ul className="text-muted-foreground space-y-1 list-disc list-inside">
              <li>
                <strong className="text-foreground">Seeker profile:</strong> display name, photo, city and areas
                you are looking in, budget, bio, gender (optional), lifestyle tags, and optional commute places such
                as your workplace or college (saved only as an approximate area, about 1 km — never the exact
                building)
              </li>
              <li>
                <strong className="text-foreground">Room listing</strong> (published by a flat admin): flat name,
                city and area, rent, deposit, beds and room type, availability date, preferred gender, lifestyle
                tags, description, and photos you upload
              </li>
              <li>
                <strong className="text-foreground">Connections and messages:</strong> connection requests (with the
                note you send), their status, and the messages you exchange with other users
              </li>
              <li>
                <strong className="text-foreground">Safety records:</strong> reports you submit (who or what you
                reported, the reason, and when) and the list of users you have blocked
              </li>
            </ul>

            <h3 className="font-medium mb-2 mt-5">Location data</h3>
            <p className="text-muted-foreground">
              When you choose a place on the map (for example a listing area or your workplace), we store that
              place&apos;s name and its map coordinates. If you allow location permission in the Android app, your
              device location is used only to centre the map while you pick a place — we do not track your location
              in the background or keep a location history.
            </p>

            <h3 className="font-medium mb-2 mt-5">Device, usage, and diagnostic data</h3>
            <ul className="text-muted-foreground space-y-1 list-disc list-inside">
              <li>Website: basic, aggregated page analytics through Vercel Analytics</li>
              <li>
                Android app: app-usage events and device information (model, OS version, app version) through
                Firebase Analytics, and crash reports through Firebase Crashlytics
              </li>
              <li>A device token, if you allow push notifications</li>
            </ul>
            <p className="text-muted-foreground text-sm mt-2">
              App-lock (fingerprint or face) is handled entirely by your phone. We never receive your biometric data.
            </p>
          </section>

          {/* Why we collect it */}
          <section>
            <h2 className="text-lg font-semibold mb-3">3. Why We Collect It</h2>
            <table className="w-full text-sm text-muted-foreground border-collapse">
              <thead>
                <tr className="border-b border-border">
                  <th className="text-left py-2 pr-4 font-medium text-foreground w-1/2">Purpose</th>
                  <th className="text-left py-2 font-medium text-foreground">Basis (DPDP)</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border">
                <tr>
                  <td className="py-2 pr-4">Create and manage your account</td>
                  <td className="py-2">Consent (at sign-up)</td>
                </tr>
                <tr>
                  <td className="py-2 pr-4">Run the duty rotation engine and sync flat data in real time</td>
                  <td className="py-2">Consent — the purpose you signed up for</td>
                </tr>
                <tr>
                  <td className="py-2 pr-4">Show your flatmates who completed a task, who owes what</td>
                  <td className="py-2">Consent — the purpose you signed up for</td>
                </tr>
                <tr>
                  <td className="py-2 pr-4">Show your seeker profile or room listing to other users on Discover</td>
                  <td className="py-2">Consent — given when you publish it, withdrawn when you remove it</td>
                </tr>
                <tr>
                  <td className="py-2 pr-4">Deliver connection requests and messages</td>
                  <td className="py-2">Consent — the purpose you use the feature for</td>
                </tr>
                <tr>
                  <td className="py-2 pr-4">Match listings to your areas, budget, tags, and commute</td>
                  <td className="py-2">Consent — given when you publish a seeker profile or use filters</td>
                </tr>
                <tr>
                  <td className="py-2 pr-4">Review reports, prevent fraud and harassment, enforce our Terms</td>
                  <td className="py-2">Legitimate use — safety of users and compliance with law</td>
                </tr>
                <tr>
                  <td className="py-2 pr-4">Send reminders and notifications (when you enable them)</td>
                  <td className="py-2">Consent (opt-in per channel)</td>
                </tr>
                <tr>
                  <td className="py-2 pr-4">Fix crashes and improve the product</td>
                  <td className="py-2">Consent (at sign-up); you can ask us to stop</td>
                </tr>
                <tr>
                  <td className="py-2 pr-4">Respond to support requests and complaints</td>
                  <td className="py-2">Legal obligation</td>
                </tr>
              </tbody>
            </table>

            <h3 className="font-medium mb-2 mt-5">Advertising and selling data</h3>
            <ul className="text-muted-foreground space-y-1 list-disc list-inside">
              <li>We do not sell your personal data, and we never will.</li>
              <li>We do not show ads or build advertising profiles today.</li>
              <li>
                If we ever offer sponsored listings or offers based on Discover activity (for example the areas or
                budget you search), we will ask for your separate, opt-in consent first, use only grouped data that
                cannot identify you, and never give advertisers your name, contact details, or messages.
              </li>
            </ul>
          </section>

          {/* Who can see what */}
          <section>
            <h2 className="text-lg font-semibold mb-3">4. Who Can See Your Information</h2>
            <ul className="text-muted-foreground space-y-2 list-disc list-inside">
              <li>
                <strong className="text-foreground">Flat data</strong> (tasks, expenses, activity) is visible only to
                members of that flat.
              </li>
              <li>
                <strong className="text-foreground">Your seeker profile and published room listings</strong> are
                visible to every signed-in user of Discover. Don&apos;t put your phone number, exact address, or ID
                details in them.
              </li>
              <li>
                <strong className="text-foreground">Exact addresses</strong> are never shown on Discover. Listings show
                city and area only; your invite code is never shown.
              </li>
              <li>
                <strong className="text-foreground">Flat signals on a listing:</strong> when an admin publishes a room,
                the listing can show summary signals about how the flat runs (for example whether tasks and expenses
                are kept up to date) and members&apos; nicknames and roles — never individual amounts or task logs.
              </li>
              <li>
                <strong className="text-foreground">Messages</strong> are visible only to you and the person you are
                talking to. Our team may read a conversation only when it is reported to us, to investigate it.
              </li>
              <li>
                <strong className="text-foreground">Reports</strong> are confidential. We do not tell the reported user
                who reported them.
              </li>
              <li>
                <strong className="text-foreground">Trust labels:</strong> any trust or verification label is based only
                on activity on Habitiq. It is not a background check. Before we use your task or payment history for a
                personal trust label, we will ask for your separate consent, and you can turn it off.
              </li>
              <li>
                <strong className="text-foreground">Authorities:</strong> we share data with police or other authorities
                only when required by Indian law, or when needed to protect someone from serious harm.
              </li>
            </ul>
          </section>

          {/* Third parties */}
          <section>
            <h2 className="text-lg font-semibold mb-3">5. Third-Party Services We Use</h2>
            <p className="text-muted-foreground mb-3">
              We use a small number of trusted services to run the platform. These act as Data Processors on our behalf.
            </p>
            <table className="w-full text-sm text-muted-foreground border-collapse">
              <thead>
                <tr className="border-b border-border">
                  <th className="text-left py-2 pr-4 font-medium text-foreground">Service</th>
                  <th className="text-left py-2 pr-4 font-medium text-foreground">Purpose</th>
                  <th className="text-left py-2 font-medium text-foreground">Data shared</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border">
                <tr>
                  <td className="py-2 pr-4">Google Firebase (Auth, Firestore, Storage)</td>
                  <td className="py-2 pr-4">Sign-in, database, photo storage</td>
                  <td className="py-2">Account, flat, Discover data, and photos</td>
                </tr>
                <tr>
                  <td className="py-2 pr-4">Firebase Analytics and Crashlytics</td>
                  <td className="py-2 pr-4">Usage measurement and crash reports (Android)</td>
                  <td className="py-2">App events, device info, crash logs</td>
                </tr>
                <tr>
                  <td className="py-2 pr-4">Firebase Cloud Messaging</td>
                  <td className="py-2 pr-4">Push notifications (opt-in)</td>
                  <td className="py-2">Device token</td>
                </tr>
                <tr>
                  <td className="py-2 pr-4">Google Maps Platform</td>
                  <td className="py-2 pr-4">Map display and place search</td>
                  <td className="py-2">Map locations you view or pick</td>
                </tr>
                <tr>
                  <td className="py-2 pr-4">Vercel</td>
                  <td className="py-2 pr-4">Website hosting and delivery</td>
                  <td className="py-2">Aggregated usage data</td>
                </tr>
                <tr>
                  <td className="py-2 pr-4">WhatsApp Cloud API (planned)</td>
                  <td className="py-2 pr-4">Task reminders (opt-in only)</td>
                  <td className="py-2">Name + reminder message</td>
                </tr>
              </tbody>
            </table>
            <p className="text-muted-foreground text-sm mt-3">
              Some Google servers are located outside India. Your data may be processed there under Google&apos;s
              security and privacy commitments, as permitted by the DPDP Act.
            </p>
          </section>

          {/* Data retention */}
          <section>
            <h2 className="text-lg font-semibold mb-3">6. How Long We Keep Your Data</h2>
            <ul className="text-muted-foreground space-y-2 list-disc list-inside">
              <li>
                <strong className="text-foreground">Active account:</strong> Data is retained as long as your account exists.
              </li>
              <li>
                <strong className="text-foreground">After leaving all flats:</strong> Your account data (email, name) is retained until you explicitly delete your account. Flat activity records (task logs, expenses) remain as part of the flat&apos;s history for other members.
              </li>
              <li>
                <strong className="text-foreground">Seeker profiles and listings:</strong> Removed from Discover as soon as you unpublish or delete them.
              </li>
              <li>
                <strong className="text-foreground">Messages:</strong> Kept while both accounts exist so the conversation history stays available to both people.
              </li>
              <li>
                <strong className="text-foreground">Reports:</strong> Kept for up to 2 years after review, so we can act on repeat abuse and respond to legal requests.
              </li>
              <li>
                <strong className="text-foreground">After account deletion:</strong> Your personal data — including your seeker profile, photos, connections, and messages you sent — is removed within 30 days. Anonymised references in flat logs (e.g., &quot;a member completed Kitchen&quot;) may be retained for data integrity, and we may keep records we are required to keep by law.
              </li>
            </ul>
          </section>

          {/* Your rights */}
          <section>
            <h2 className="text-lg font-semibold mb-3">7. Your Rights Under the DPDP Act, 2023</h2>
            <p className="text-muted-foreground mb-3">As a data principal under the DPDP Act, you have the following rights:</p>
            <ul className="text-muted-foreground space-y-2 list-disc list-inside">
              <li><strong className="text-foreground">Right to access:</strong> Know what personal data we hold about you.</li>
              <li><strong className="text-foreground">Right to correction:</strong> Request that inaccurate or incomplete data be corrected — including any trust label you think is wrong.</li>
              <li><strong className="text-foreground">Right to erasure:</strong> Request deletion of your personal data, subject to legal obligations.</li>
              <li><strong className="text-foreground">Right to withdraw consent:</strong> As easily as you gave it — for example by unpublishing your Discover profile, turning off notifications, or deleting your account.</li>
              <li><strong className="text-foreground">Right to grievance redressal:</strong> Raise a complaint and receive a response within a reasonable time.</li>
              <li><strong className="text-foreground">Right to nominate:</strong> Nominate someone to exercise your rights in the event of your incapacity or death.</li>
            </ul>
            <p className="text-muted-foreground mt-3">
              To exercise any of these rights, email us at{' '}
              <a href="mailto:hello@habitiq.app" className="text-violet-500 hover:underline">hello@habitiq.app</a>{' '}
              with the subject &quot;Data Request&quot;. We will respond within 30 days. If you are not satisfied with
              our response, you may complain to the Data Protection Board of India.
            </p>
          </section>

          {/* Security */}
          <section>
            <h2 className="text-lg font-semibold mb-3">8. Security</h2>
            <p className="text-muted-foreground leading-relaxed">
              We take security seriously. The app has undergone three internal security audits. We use Firebase&apos;s
              role-based security rules so members can only access data from flats they belong to, and messages
              only between the two people in a conversation. Sensitive operations (task deletion, expense editing,
              member removal) are validated at the database level — not just the UI. Data is encrypted in transit.
            </p>
            <p className="text-muted-foreground leading-relaxed mt-2">
              If we become aware of a personal data breach that affects you, we will inform you and the Data
              Protection Board of India as required by the DPDP Rules.
            </p>
            <p className="text-muted-foreground leading-relaxed mt-2">
              No security system is perfect. If you discover a vulnerability, please report it to{' '}
              <a href="mailto:hello@habitiq.app" className="text-violet-500 hover:underline">hello@habitiq.app</a>{' '}
              before disclosing it publicly.
            </p>
          </section>

          {/* Children */}
          <section>
            <h2 className="text-lg font-semibold mb-3">9. Children</h2>
            <p className="text-muted-foreground">
              Habitiq is not intended for use by anyone under 18. We do not knowingly collect data from minors.
              If you believe a minor has created an account, contact us and we will delete it.
            </p>
          </section>

          {/* Changes */}
          <section>
            <h2 className="text-lg font-semibold mb-3">10. Changes to This Policy</h2>
            <p className="text-muted-foreground">
              If we make material changes to this policy, we will notify active users within the app and update the
              date above. If a change needs new consent (for example a new purpose), we will ask for it rather than
              assume it.
            </p>
          </section>

          {/* Contact */}
          <section>
            <h2 className="text-lg font-semibold mb-3">11. Grievance Officer</h2>
            <p className="text-muted-foreground">
              As required under the DPDP Act, our designated grievance contact is:
            </p>
            <div className="mt-2 text-muted-foreground">
              <p><strong className="text-foreground">Name:</strong> Venkata Sai Jaswanth E</p>
              <p><strong className="text-foreground">Email:</strong>{' '}
                <a href="mailto:hello@habitiq.app" className="text-violet-500 hover:underline">hello@habitiq.app</a>
              </p>
              <p><strong className="text-foreground">Response time:</strong> Within 30 days of receiving a complaint</p>
            </div>
            <p className="text-muted-foreground text-sm mt-3">
              Safety concerns on Discover can go to the same address — see our{' '}
              <Link href="/safety" className="text-violet-500 hover:underline">Safety Tips</Link>.
            </p>
          </section>

          <hr className="border-border" />

          <p className="text-xs text-muted-foreground">
            Habitiq · habitiq.app · India · Document version 0.5.0 ·{' '}
            <Link href="/terms" className="text-violet-500 hover:underline">Terms of Service</Link> ·{' '}
            <Link href="/safety" className="text-violet-500 hover:underline">Safety Tips</Link>
          </p>
        </div>
      </div>
    </main>
  )
}
