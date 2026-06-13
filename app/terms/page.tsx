import Link from 'next/link'
import type { Metadata } from 'next'

export const metadata: Metadata = {
  title: 'Terms of Service — Habitiq',
  description: 'The rules for using Habitiq — what you can expect from us and what we expect from you.',
}

export default function TermsPage() {
  return (
    <main className="min-h-screen bg-background text-foreground">
      <div className="max-w-2xl mx-auto px-6 py-16">

        {/* Header */}
        <div className="mb-10">
          <Link href="/" className="text-sm text-muted-foreground hover:text-foreground mb-6 inline-block">
            ← Back to Habitiq
          </Link>
          <h1 className="text-3xl font-bold tracking-tight mb-2">Terms of Service</h1>
          <p className="text-muted-foreground text-sm">
            Effective date: 13 June 2026 · Last updated: 13 June 2026
          </p>
        </div>

        <div className="prose prose-sm dark:prose-invert max-w-none space-y-8 text-foreground">

          {/* Intro */}
          <section>
            <p className="text-muted-foreground leading-relaxed">
              These Terms of Service ("Terms") govern your use of Habitiq — a shared living management platform
              for flats, PGs, and co-living spaces. By creating an account, you agree to these Terms.
              If you don't agree, don't use the service.
            </p>
            <p className="text-muted-foreground leading-relaxed mt-3">
              Habitiq is operated by its founders, Venkata Sai Jaswanth E and Upputuri Bhanu Kalyan, based in India
              ("we", "us", "Habitiq"). Our{' '}
              <Link href="/privacy" className="text-violet-500 hover:underline">Privacy Policy</Link>{' '}
              is part of these Terms by reference.
            </p>
          </section>

          <hr className="border-border" />

          {/* Eligibility */}
          <section>
            <h2 className="text-lg font-semibold mb-3">1. Who Can Use Habitiq</h2>
            <ul className="text-muted-foreground space-y-2 list-disc list-inside">
              <li>You must be 18 years of age or older.</li>
              <li>You must provide accurate information when creating your account.</li>
              <li>You are responsible for maintaining the security of your account credentials.</li>
              <li>One person may have one account. Do not create accounts on behalf of others.</li>
            </ul>
          </section>

          {/* What Habitiq is */}
          <section>
            <h2 className="text-lg font-semibold mb-3">2. What Habitiq Does</h2>
            <p className="text-muted-foreground leading-relaxed">
              Habitiq helps flatmates manage shared household duties, track expenses, and split bills.
              Specifically, we provide:
            </p>
            <ul className="text-muted-foreground space-y-1 list-disc list-inside mt-2">
              <li>Automated duty rotation among flat members</li>
              <li>Task tracking with overdue detection and reliability scores</li>
              <li>Swap request management between members</li>
              <li>Expense logging and pairwise balance calculation</li>
              <li>Recurring bill management with payer rotation</li>
              <li>Real-time sync so all members see the same state</li>
            </ul>
            <p className="text-muted-foreground mt-3">
              <strong className="text-foreground">Habitiq does not process payments.</strong> When a balance is
              recorded as "settled", we log the record — but no money moves through our platform. All actual
              transfers happen between members directly (UPI, cash, bank transfer, etc.).
            </p>
          </section>

          {/* Free service */}
          <section>
            <h2 className="text-lg font-semibold mb-3">3. Free Service</h2>
            <p className="text-muted-foreground leading-relaxed">
              Habitiq is currently free to use. We may introduce paid plans in the future. If we do, we will
              notify existing users at least 30 days in advance, and free features will remain free or be
              grandfathered at our discretion. We will not start charging without explicit notice and your
              opt-in for paid features.
            </p>
          </section>

          {/* User responsibilities */}
          <section>
            <h2 className="text-lg font-semibold mb-3">4. Your Responsibilities</h2>

            <h3 className="font-medium mb-2">You agree to use Habitiq only to:</h3>
            <ul className="text-muted-foreground space-y-1 list-disc list-inside">
              <li>Manage genuine shared-living arrangements</li>
              <li>Record real expenses and settlements between real flatmates</li>
              <li>Communicate fairly and honestly with your flat members through the platform</li>
            </ul>

            <h3 className="font-medium mb-2 mt-4">You agree not to:</h3>
            <ul className="text-muted-foreground space-y-1 list-disc list-inside">
              <li>Use Habitiq to harass, threaten, or defraud other members</li>
              <li>Record false expenses or manipulate balances to exploit flatmates</li>
              <li>Attempt to access, modify, or delete another user's data outside the app's intended flows</li>
              <li>Try to reverse-engineer, scrape, or automate interaction with the platform</li>
              <li>Circumvent the 8-member flat cap or other platform limits</li>
              <li>Use the service for any purpose that is illegal under Indian law</li>
            </ul>

            <h3 className="font-medium mb-2 mt-4">Flat admin responsibilities</h3>
            <p className="text-muted-foreground">
              If you create a flat, you are the admin. Admins control task creation, member removal, bill management,
              and month-end close. Use these powers fairly. Admin actions are logged and visible to all members.
              Admins are responsible for the data entered into their flat.
            </p>
          </section>

          {/* Content you create */}
          <section>
            <h2 className="text-lg font-semibold mb-3">5. Content You Create</h2>
            <p className="text-muted-foreground leading-relaxed">
              You own the data you enter into Habitiq (task names, expense descriptions, notes). You grant us
              a limited licence to store, display, and sync that data to members of your flat — which is
              necessary to provide the service.
            </p>
            <p className="text-muted-foreground leading-relaxed mt-2">
              We do not claim ownership of your data. We do not sell it. We do not use it for advertising.
            </p>
          </section>

          {/* Flat membership */}
          <section>
            <h2 className="text-lg font-semibold mb-3">6. Flat Membership and Removal</h2>
            <ul className="text-muted-foreground space-y-2 list-disc list-inside">
              <li>
                Members can leave a flat at any time from Settings. If you are the admin and other members are
                present, you must transfer admin role before leaving.
              </li>
              <li>
                Admins can remove members from a flat. Removed members lose access immediately and are
                redirected to onboarding.
              </li>
              <li>
                If the last member leaves a flat, all flat data (tasks, expenses, logs) is permanently deleted.
                There is no recovery.
              </li>
              <li>
                We may suspend or delete accounts that violate these Terms, with notice where practical.
              </li>
            </ul>
          </section>

          {/* Disputes between members */}
          <section>
            <h2 className="text-lg font-semibold mb-3">7. Disputes Between Members</h2>
            <p className="text-muted-foreground leading-relaxed">
              Habitiq records what members log. We cannot verify whether a task was actually completed, whether
              money was actually paid, or whether a settlement is accurate. Disputes about chores or money between
              flatmates are between those flatmates — Habitiq is not a party to those disputes and cannot
              mediate, arbitrate, or enforce outcomes.
            </p>
            <p className="text-muted-foreground leading-relaxed mt-2">
              If a flatmate misuses the platform (falsifying records, manipulating balances), your recourse is
              to report it to us and, where appropriate, to relevant legal authorities.
            </p>
          </section>

          {/* Service availability */}
          <section>
            <h2 className="text-lg font-semibold mb-3">8. Service Availability</h2>
            <p className="text-muted-foreground leading-relaxed">
              We aim to keep Habitiq available and working. However, we do not guarantee 100% uptime.
              The service may be interrupted for maintenance, infrastructure issues, or events outside our
              control. We are not liable for losses caused by downtime.
            </p>
            <p className="text-muted-foreground leading-relaxed mt-2">
              We may discontinue Habitiq by giving users at least 60 days' notice. During that period, you
              will be able to export your flat's expense and task data.
            </p>
          </section>

          {/* Limitation of liability */}
          <section>
            <h2 className="text-lg font-semibold mb-3">9. Limitation of Liability</h2>
            <p className="text-muted-foreground leading-relaxed">
              Habitiq is provided "as is". To the fullest extent permitted by law, we are not liable for:
            </p>
            <ul className="text-muted-foreground space-y-1 list-disc list-inside mt-2">
              <li>Financial disputes or losses between flat members</li>
              <li>Data loss resulting from user error, deletion by flat members, or platform outages</li>
              <li>Errors in balance calculations caused by incorrect data entered by users</li>
              <li>Indirect, incidental, or consequential damages of any kind</li>
            </ul>
            <p className="text-muted-foreground mt-3">
              Our total liability to you for any claim arising out of your use of Habitiq is limited to ₹0,
              since the service is currently free.
            </p>
          </section>

          {/* Intellectual property */}
          <section>
            <h2 className="text-lg font-semibold mb-3">10. Intellectual Property</h2>
            <p className="text-muted-foreground leading-relaxed">
              The Habitiq name, logo, rotation engine, and all platform code are owned by the founders.
              You may not copy, reproduce, or distribute any part of the Habitiq platform without written
              permission.
            </p>
          </section>

          {/* Governing law */}
          <section>
            <h2 className="text-lg font-semibold mb-3">11. Governing Law and Disputes</h2>
            <p className="text-muted-foreground leading-relaxed">
              These Terms are governed by the laws of India. Any dispute arising from your use of Habitiq
              will be subject to the exclusive jurisdiction of the courts in Hyderabad, Telangana, India.
            </p>
            <p className="text-muted-foreground leading-relaxed mt-2">
              Before taking any formal action, please contact us at{' '}
              <a href="mailto:hello@habitiq.in" className="text-violet-500 hover:underline">hello@habitiq.in</a>{' '}
              — we will try to resolve any concern directly.
            </p>
          </section>

          {/* Changes */}
          <section>
            <h2 className="text-lg font-semibold mb-3">12. Changes to These Terms</h2>
            <p className="text-muted-foreground leading-relaxed">
              We may update these Terms as the product evolves. If changes materially affect your rights, we
              will notify you in-app with at least 14 days' notice before the new Terms take effect.
              Continued use after the effective date means you accept the updated Terms.
            </p>
          </section>

          {/* Contact */}
          <section>
            <h2 className="text-lg font-semibold mb-3">13. Contact</h2>
            <p className="text-muted-foreground">
              Questions about these Terms? Email us at{' '}
              <a href="mailto:hello@habitiq.in" className="text-violet-500 hover:underline">hello@habitiq.in</a>.
              We read everything.
            </p>
          </section>

          <hr className="border-border" />

          <p className="text-xs text-muted-foreground">
            Habitiq · habitiq.in · India · Document version 0.3.0 ·{' '}
            <Link href="/privacy" className="text-violet-500 hover:underline">Privacy Policy</Link>
          </p>
        </div>
      </div>
    </main>
  )
}
