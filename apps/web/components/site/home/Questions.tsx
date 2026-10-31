import Link from 'next/link'
import s from '../site.module.css'
import q from './Questions.module.css'

// Server-rendered answers are visible to readers and crawlers without opening a widget.
export default function Questions() {
  return (
    <section id="questions" className={`${s.section} ${s.toneWhite}`} aria-labelledby="questions-title">
      <div className={s.inner}>
        <h2 id="questions-title" className={s.title}>Questions about Habitiq</h2>
        <div className={`${s.prose} ${q.answers}`}>
          <h3>What is Habitiq?</h3>
          <p>Habitiq is an Android app for finding rooms and flatmates and managing a shared household. It brings room discovery, rotating chores, shared expenses and monthly bills together. <Link href="/about">About Habitiq</Link>.</p>
          <h3>Can I look for a room without creating a household?</h3>
          <p>Yes. Choose a search city and up to three preferred areas to browse relevant rooms and people searching nearby. You do not need to publish a looking post to browse. <Link href="/discover">Explore room and flatmate discovery</Link>.</p>
          <h3>Who can post a vacant room?</h3>
          <p>A room listing belongs to a household in Habitiq. Create your household first, or use a household you are authorized to manage. A member’s proposed listing can require the household admin’s approval before it goes live.</p>
          <h3>Can two people find a place together?</h3>
          <p>People searching in overlapping areas can send a connection request and chat after it is accepted. Connecting does not automatically add either person to a household.</p>
          <h3>Does Habitiq transfer rent or settlement payments?</h3>
          <p>No. Habitiq records shared expenses, bills and settlements so household members can track what they owe. Recording a settlement does not transfer money. <Link href="/flat-manager">See household management features</Link>.</p>
          <h3>How should I check a room before committing?</h3>
          <p>Review the listing, talk with the household and view the room before paying. A listing is not a guarantee of the property or person. Discovery uses approximate locations; report or block concerning interactions. <Link href="/safety">Read the safety guidance</Link>.</p>
          <h3>How do I delete my Habitiq account?</h3>
          <p>Request deletion in the app’s privacy and security settings or through our <Link href="/delete-account">account deletion page</Link>. Shared household accounting history remains available to remaining members. The <Link href="/privacy">privacy policy</Link> explains what is removed and retained.</p>
        </div>
      </div>
    </section>
  )
}
