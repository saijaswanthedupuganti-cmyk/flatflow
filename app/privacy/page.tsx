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
            Effective date: 13 June 2026 · Last updated: 13 June 2026
          </p>
        </div>

        <div className="prose prose-sm dark:prose-invert max-w-none space-y-8 text-foreground">

          {/* Intro */}
          <section>
            <p className="text-muted-foreground leading-relaxed">
              Habitiq is a shared living management platform built for flats, PGs, and co-living spaces in India.
              This policy explains what personal data we collect, why we collect it, how we use it, and what rights
              you have over it. We try to say this plainly — not in a way designed to confuse you.
            </p>
            <p className="text-muted-foreground leading-relaxed mt-3">
              This policy applies to Habitiq users worldwide and complies with India's{' '}
              <strong className="text-foreground">Digital Personal Data Protection Act, 2023 (DPDP Act)</strong>.
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
              We do not collect your phone number, date of birth, government ID, or any payment information.
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
              We do not process any actual payments. Habitiq records that a settlement happened (e.g., "Rahul paid
              ₹2,000 via UPI") but no money moves through our platform.
            </p>

            <h3 className="font-medium mb-2 mt-5">Usage data</h3>
            <p className="text-muted-foreground">
              We collect basic usage analytics through Vercel Analytics (page views, general usage patterns).
              This data is aggregated and not linked to your individual account.
            </p>
          </section>

          {/* Why we collect it */}
          <section>
            <h2 className="text-lg font-semibold mb-3">3. Why We Collect It</h2>
            <table className="w-full text-sm text-muted-foreground border-collapse">
              <thead>
                <tr className="border-b border-border">
                  <th className="text-left py-2 pr-4 font-medium text-foreground w-1/2">Purpose</th>
                  <th className="text-left py-2 font-medium text-foreground">Lawful basis (DPDP)</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border">
                <tr>
                  <td className="py-2 pr-4">Create and manage your account</td>
                  <td className="py-2">Consent (at sign-up)</td>
                </tr>
                <tr>
                  <td className="py-2 pr-4">Run the duty rotation engine and sync flat data in real time</td>
                  <td className="py-2">Performance of service</td>
                </tr>
                <tr>
                  <td className="py-2 pr-4">Show your flatmates who completed a task, who owes what</td>
                  <td className="py-2">Performance of service</td>
                </tr>
                <tr>
                  <td className="py-2 pr-4">Send task reminders (push/WhatsApp, when you enable them)</td>
                  <td className="py-2">Consent (opt-in per channel)</td>
                </tr>
                <tr>
                  <td className="py-2 pr-4">Improve the product (aggregated analytics)</td>
                  <td className="py-2">Legitimate interest</td>
                </tr>
                <tr>
                  <td className="py-2 pr-4">Respond to support requests and complaints</td>
                  <td className="py-2">Legal obligation</td>
                </tr>
              </tbody>
            </table>
            <p className="text-muted-foreground text-sm mt-3">
              We do not sell your data. We do not use your data for advertising or profiling.
            </p>
          </section>

          {/* Third parties */}
          <section>
            <h2 className="text-lg font-semibold mb-3">4. Third-Party Services We Use</h2>
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
                  <td className="py-2 pr-4">Google Firebase (Auth + Firestore)</td>
                  <td className="py-2 pr-4">Authentication and database</td>
                  <td className="py-2">Account and flat data</td>
                </tr>
                <tr>
                  <td className="py-2 pr-4">Vercel</td>
                  <td className="py-2 pr-4">Hosting and delivery</td>
                  <td className="py-2">Aggregated usage data</td>
                </tr>
                <tr>
                  <td className="py-2 pr-4">WhatsApp Cloud API (planned)</td>
                  <td className="py-2 pr-4">Task reminders (opt-in only)</td>
                  <td className="py-2">Name + reminder message</td>
                </tr>
                <tr>
                  <td className="py-2 pr-4">Firebase Cloud Messaging (planned)</td>
                  <td className="py-2 pr-4">Push notifications (opt-in only)</td>
                  <td className="py-2">Device token</td>
                </tr>
              </tbody>
            </table>
            <p className="text-muted-foreground text-sm mt-3">
              Google Firebase servers are located outside India. By using Habitiq, you consent to your data
              being transferred to Google's infrastructure in accordance with Google's privacy commitments.
            </p>
          </section>

          {/* Data retention */}
          <section>
            <h2 className="text-lg font-semibold mb-3">5. How Long We Keep Your Data</h2>
            <ul className="text-muted-foreground space-y-2 list-disc list-inside">
              <li>
                <strong className="text-foreground">Active account:</strong> Data is retained as long as your account exists and you are a member of at least one flat.
              </li>
              <li>
                <strong className="text-foreground">After leaving all flats:</strong> Your account data (email, name) is retained until you explicitly delete your account. Flat activity records (task logs, expenses) remain as part of the flat's history for other members.
              </li>
              <li>
                <strong className="text-foreground">After account deletion:</strong> Your personal data is removed within 30 days. Anonymised references in flat logs (e.g., "a member completed Kitchen") may be retained for data integrity.
              </li>
            </ul>
          </section>

          {/* Your rights */}
          <section>
            <h2 className="text-lg font-semibold mb-3">6. Your Rights Under the DPDP Act, 2023</h2>
            <p className="text-muted-foreground mb-3">As a data principal under the DPDP Act, you have the following rights:</p>
            <ul className="text-muted-foreground space-y-2 list-disc list-inside">
              <li><strong className="text-foreground">Right to access:</strong> Know what personal data we hold about you.</li>
              <li><strong className="text-foreground">Right to correction:</strong> Request that inaccurate or incomplete data be corrected.</li>
              <li><strong className="text-foreground">Right to erasure:</strong> Request deletion of your personal data, subject to legal obligations.</li>
              <li><strong className="text-foreground">Right to grievance redressal:</strong> Raise a complaint and receive a response within a reasonable time.</li>
              <li><strong className="text-foreground">Right to nominate:</strong> Nominate someone to exercise your rights in the event of your incapacity or death.</li>
            </ul>
            <p className="text-muted-foreground mt-3">
              To exercise any of these rights, email us at{' '}
              <a href="mailto:hello@habitiq.app" className="text-violet-500 hover:underline">hello@habitiq.app</a>{' '}
              with the subject "Data Request". We will respond within 30 days.
            </p>
          </section>

          {/* Security */}
          <section>
            <h2 className="text-lg font-semibold mb-3">7. Security</h2>
            <p className="text-muted-foreground leading-relaxed">
              We take security seriously. The app has undergone three internal security audits. We use Firebase's
              role-based Firestore security rules to ensure members can only access data from flats they belong to.
              Sensitive operations (task deletion, expense editing, member removal) are validated at the database
              level — not just the UI.
            </p>
            <p className="text-muted-foreground leading-relaxed mt-2">
              No security system is perfect. If you discover a vulnerability, please report it to{' '}
              <a href="mailto:hello@habitiq.app" className="text-violet-500 hover:underline">hello@habitiq.app</a>{' '}
              before disclosing it publicly.
            </p>
          </section>

          {/* Children */}
          <section>
            <h2 className="text-lg font-semibold mb-3">8. Children</h2>
            <p className="text-muted-foreground">
              Habitiq is not intended for use by anyone under 18. We do not knowingly collect data from minors.
              If you believe a minor has created an account, contact us and we will delete it.
            </p>
          </section>

          {/* Changes */}
          <section>
            <h2 className="text-lg font-semibold mb-3">9. Changes to This Policy</h2>
            <p className="text-muted-foreground">
              If we make material changes to this policy, we will notify active users within the app and update the
              effective date above. Continued use after the notice period means you accept the updated policy.
            </p>
          </section>

          {/* Contact */}
          <section>
            <h2 className="text-lg font-semibold mb-3">10. Grievance Officer</h2>
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
          </section>

          <hr className="border-border" />

          <p className="text-xs text-muted-foreground">
            Habitiq · habitiq.app · India · Document version 0.4.0 ·{' '}
            <Link href="/terms" className="text-violet-500 hover:underline">Terms of Service</Link>
          </p>
        </div>
      </div>
    </main>
  )
}
