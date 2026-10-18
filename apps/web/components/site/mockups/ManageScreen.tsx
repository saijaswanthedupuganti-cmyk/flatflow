import { ChevronRight } from 'lucide-react'
import { AppNav, ClipboardIllustration, ReceiptIllustration } from './AppParts'
import m from './mockups.module.css'

/** The app's Manage tab: tasks and expenses at a glance, what needs you, and recent activity. */
export default function ManageScreen() {
  return (
    <div className={m.page}>
      <div className={m.h1}>Manage flat</div>
      <div className={m.sub}>Sunrise Heights 4B · 4 members</div>
      <div className={m.mCards}>
        <div className={`${m.mCard} ${m.outline}`}>
          <div style={{ display: 'flex', justifyContent: 'space-between' }}><ClipboardIllustration /><ChevronRight size="1em" aria-hidden /></div>
          <b>Tasks</b>
          <span className={m.muted}>3 assigned</span>
          <span className={`${m.muted} ${m.red}`}>1 overdue</span>
        </div>
        <div className={`${m.mCard} ${m.outline}`}>
          <div style={{ display: 'flex', justifyContent: 'space-between' }}><ReceiptIllustration /><ChevronRight size="1em" aria-hidden /></div>
          <b>Expenses</b>
          <span className={m.muted}>You owe ₹850</span>
          <span className={m.muted}>You&apos;re owed ₹1,240.50</span>
        </div>
      </div>
      <div className={m.secTitle}>Needs your attention</div>
      <div className={`${m.attn} ${m.outline}`}>
        <div><span>Overdue task</span><span className={m.muted}>Kitchen cleanup · was due yesterday</span></div>
        <div><span>Balance to settle</span><span className={m.muted}>You owe ₹850 to 1 person</span></div>
      </div>
      <div className={m.secTitle}>Recent activity<span className={m.linkTeal}>View all</span></div>
      <div className={m.act}>
        <div>Anita completed Kitchen cleanup<div className={m.muted}>2 hours ago</div></div>
        <div>You added an expense: Groceries<div className={m.muted}>Yesterday</div></div>
      </div>
      <AppNav active="manage" />
    </div>
  )
}
