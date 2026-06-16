'use client'

import { useEffect, useState, ReactNode } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { Navbar, AuthForm } from '@/components/ui/navbar'
import { Check, ArrowRight, Star } from 'lucide-react'
import TestimonialSlider, { type Testimonial } from '@/components/ui/testimonial-slider'

// ── Design tokens ─────────────────────────────────────────────────────────────
const BG      = '#08080C'
const SURFACE = '#0F0F15'
const BORDER  = 'rgba(255,255,255,0.07)'
const BLUE    = '#2563EB'
const BLUE_L  = '#60A5FA'
const GREEN_L = '#34D399'
const PURPLE  = '#7C3AED'
const PURPLE_L= '#A78BFA'

// ── Easing ────────────────────────────────────────────────────────────────────
const EASE: [number, number, number, number] = [0.22, 1, 0.36, 1]

// ── Phone frame ────────────────────────────────────────────────────────────────
// W scales with viewport: ~220px on small phones, 264px on desktop
function PhoneFrame({ children, glow = 'rgba(37,99,235,0.22)' }: { children: ReactNode; glow?: string }) {
  const W = 'min(264px, 58vw)'
  return (
    <div className="relative shrink-0" style={{ width: W }}>
      {/* Ambient glow under phone */}
      <div className="absolute pointer-events-none" style={{
        bottom: '-28px', left: '50%', transform: 'translateX(-50%)',
        width: '85%', height: '56px',
        background: `radial-gradient(ellipse, ${glow} 0%, transparent 70%)`,
        filter: 'blur(16px)',
      }} />
      {/* Outer ring */}
      <div style={{
        borderRadius: '46px',
        padding: '10px',
        background: 'linear-gradient(160deg, #222228 0%, #0e0e12 100%)',
        boxShadow: [
          '0 0 0 1px rgba(255,255,255,0.1)',
          '0 0 0 9px #0c0c10',
          '0 0 0 10px rgba(255,255,255,0.035)',
          '0 52px 100px rgba(0,0,0,0.9)',
          'inset 0 1px 0 rgba(255,255,255,0.12)',
          'inset 0 -1px 0 rgba(0,0,0,0.5)',
        ].join(','),
      }}>
        {/* Dynamic island */}
        <div className="absolute z-30" style={{
          top: '18px', left: '50%', transform: 'translateX(-50%)',
          width: '88px', height: '26px',
          background: '#000', borderRadius: '14px',
        }} />
        {/* Side buttons */}
        <div className="absolute" style={{ left: '-11px', top: '100px', width: '3px', height: '34px', background: '#1a1a1f', borderRadius: '2px' }} />
        <div className="absolute" style={{ left: '-11px', top: '146px', width: '3px', height: '56px', background: '#1a1a1f', borderRadius: '2px' }} />
        <div className="absolute" style={{ right: '-11px', top: '120px', width: '3px', height: '68px', background: '#1a1a1f', borderRadius: '2px' }} />
        {/* Screen */}
        <div style={{ borderRadius: '36px', overflow: 'hidden', background: '#09090D' }}>
          {children}
        </div>
      </div>
    </div>
  )
}

// ── Shared screen components ──────────────────────────────────────────────────
function StatusBar({ time = '9:41' }: { time?: string }) {
  return (
    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', paddingLeft: '22px', paddingRight: '18px', paddingTop: '44px', paddingBottom: '4px' }}>
      <span style={{ fontSize: '11px', fontWeight: '700', color: 'white' }}>{time}</span>
      <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
        {[3,5,7,9].map(h => <div key={h} style={{ width: '3px', height: `${h}px`, background: 'rgba(255,255,255,0.72)', borderRadius: '1px' }} />)}
        <svg style={{ margin: '0 2px' }} width="13" height="9" viewBox="0 0 13 9" fill="none">
          <path d="M6.5 7.5a1 1 0 1 0 0-2 1 1 0 0 0 0 2Z" fill="rgba(255,255,255,0.72)"/>
          <path d="M4 5.5C4.7 4.8 5.6 4.4 6.5 4.4s1.8.4 2.5 1.1" stroke="rgba(255,255,255,0.72)" strokeWidth="1.1" strokeLinecap="round" fill="none"/>
          <path d="M1.5 3.5C2.8 2.2 4.6 1.4 6.5 1.4s3.7.8 5 2.1" stroke="rgba(255,255,255,0.5)" strokeWidth="1.1" strokeLinecap="round" fill="none"/>
        </svg>
        <div style={{ display: 'flex', alignItems: 'center', gap: '1px' }}>
          <div style={{ width: '21px', height: '10px', border: '1px solid rgba(255,255,255,0.45)', borderRadius: '2px', padding: '1.5px' }}>
            <div style={{ width: '14px', height: '100%', background: 'rgba(255,255,255,0.72)', borderRadius: '1px' }} />
          </div>
          <div style={{ width: '2px', height: '5px', background: 'rgba(255,255,255,0.4)', borderRadius: '1px' }} />
        </div>
      </div>
    </div>
  )
}

// 5-tab bottom nav — SVG icons matching the real Habitiq mobile app
function BottomNav5({ active }: { active: 'dashboard' | 'expenses' | 'tasks' | 'profile' }) {
  const ic = (a: boolean) => a ? BLUE_L : 'rgba(255,255,255,0.28)'
  const dot = <div style={{ width:'14px', height:'2px', borderRadius:'1px', background:BLUE, marginTop:'1px' }} />
  return (
    <div style={{ display:'flex', alignItems:'center', justifyContent:'space-around', padding:'6px 4px 14px', borderTop:`1px solid ${BORDER}`, background:'rgba(8,8,12,0.97)' }}>

      {/* Dashboard — 4-square grid */}
      <div style={{ display:'flex', flexDirection:'column', alignItems:'center', gap:'1px' }}>
        <svg width="19" height="19" viewBox="0 0 24 24" fill={ic(active==='dashboard')}>
          <rect x="3" y="3" width="8" height="8" rx="1.5"/>
          <rect x="13" y="3" width="8" height="8" rx="1.5"/>
          <rect x="3" y="13" width="8" height="8" rx="1.5"/>
          <rect x="13" y="13" width="8" height="8" rx="1.5"/>
        </svg>
        {active==='dashboard' && dot}
      </div>

      {/* Expenses — receipt */}
      <div style={{ display:'flex', flexDirection:'column', alignItems:'center', gap:'1px' }}>
        <svg width="19" height="19" viewBox="0 0 24 24" fill="none" stroke={ic(active==='expenses')} strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
          <rect x="2" y="4" width="20" height="16" rx="2"/>
          <path d="M2 9h20M7 14h4M7 17h3"/>
        </svg>
        {active==='expenses' && dot}
      </div>

      {/* Quick Add — large purple circle */}
      <div style={{ width:'42px', height:'42px', borderRadius:'50%', background:'linear-gradient(145deg,#7C3AED,#4F46E5)', display:'flex', alignItems:'center', justifyContent:'center', boxShadow:'0 0 0 3px rgba(79,70,229,0.22),0 8px 20px rgba(124,58,237,0.6)', marginTop:'-12px', flexShrink:0 }}>
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="2.2" strokeLinecap="round">
          <path d="M12 5v14M5 12h14"/>
        </svg>
      </div>

      {/* Tasks & Rotation — clipboard check */}
      <div style={{ display:'flex', flexDirection:'column', alignItems:'center', gap:'1px' }}>
        <svg width="19" height="19" viewBox="0 0 24 24" fill="none" stroke={ic(active==='tasks')} strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
          <rect x="3" y="3" width="18" height="18" rx="2"/>
          <path d="M8 12l3 3 5-5"/>
        </svg>
        {active==='tasks' && dot}
      </div>

      {/* Profile — avatar initial */}
      <div style={{ display:'flex', flexDirection:'column', alignItems:'center', gap:'1px' }}>
        <div style={{ width:'22px', height:'22px', borderRadius:'50%', background:active==='profile'?PURPLE:'rgba(255,255,255,0.12)', display:'flex', alignItems:'center', justifyContent:'center', fontSize:'9px', fontWeight:'800', color:'white', border:active==='profile'?`1.5px solid ${PURPLE_L}30`:'none' }}>J</div>
        {active==='profile' && dot}
      </div>

    </div>
  )
}

// ── Dashboard Screen ──────────────────────────────────────────────────────────
// Recreates the actual app: warm orange→rose→indigo gradient header, rotation order, 5-tab nav
function DashboardScreen() {
  const rota = [
    { i:'K', c:'#DC2626', name:'Kiran N..', badge:'',     date:'14 Jun' },
    { i:'M', c:'#7C3AED', name:'MADKING',   badge:'NOW',  date:'21 Jun' },
    { i:'J', c:'#2563EB', name:'Jaswant..', badge:'NEXT', date:'28 Jun' },
    { i:'B', c:'#047857', name:'Bhanu K..', badge:'',     date:'5 Jul'  },
  ]
  return (
    <div style={{ background: '#09090D', display: 'flex', flexDirection: 'column', minHeight: '518px' }}>
      {/* Gradient header — faithfully matches the real app's warm sunset gradient */}
      <div style={{ position: 'relative', overflow: 'hidden' }}>
        <StatusBar />
        <div style={{ position: 'absolute', inset: 0, zIndex: 0, background: 'linear-gradient(150deg,#C2410C 0%,#9D174D 38%,#4C1D95 65%,transparent 88%)', opacity: 0.82 }} />
        <div style={{ position: 'absolute', right: '-12px', bottom: '-18px', zIndex: 0, width: '110px', height: '110px', background: 'radial-gradient(circle,rgba(251,146,60,0.55) 0%,transparent 65%)' }} />
        <div style={{ position: 'relative', zIndex: 1, padding: '6px 14px 16px' }}>
          <div style={{ display:'flex', justifyContent:'space-between', alignItems:'flex-start' }}>
            <div>
              <p style={{ fontSize: '7px', fontWeight: '700', color: 'rgba(255,255,255,0.52)', letterSpacing: '0.14em', marginBottom: '3px' }}>EVENING</p>
              <p style={{ fontSize: '16px', fontWeight: '800', color: 'white', lineHeight: 1.1 }}>Good evening, Sai</p>
              <p style={{ fontSize: '8.5px', color: 'rgba(255,255,255,0.44)', marginTop: '2px' }}>Your personal duties</p>
            </div>
            {/* Going Away button — visible in real app screenshots */}
            <div style={{ display:'flex', alignItems:'center', gap:'4px', padding:'4px 8px', borderRadius:'20px', border:'1px solid rgba(251,146,60,0.38)', background:'rgba(251,146,60,0.1)', marginTop:'2px' }}>
              <svg width="8" height="8" viewBox="0 0 24 24" fill="none" stroke="#FB923C" strokeWidth="2.5" strokeLinecap="round"><path d="M5 12h14M12 5l7 7-7 7"/></svg>
              <span style={{ fontSize:'7px', fontWeight:'700', color:'#FB923C' }}>Going Away</span>
            </div>
          </div>
          <div style={{ display: 'flex', gap: '5px', marginTop: '10px' }}>
            {/* My Tasks — active: white pill */}
            <div style={{ fontSize: '8px', fontWeight: '700', color: 'white', background: 'rgba(255,255,255,0.22)', padding: '4px 11px', borderRadius: '20px', backdropFilter:'blur(8px)' }}>My Tasks</div>
            <div style={{ fontSize: '8px', fontWeight: '500', color: 'rgba(255,255,255,0.42)', padding: '4px 10px', borderRadius: '20px' }}>Org View</div>
          </div>
        </div>
      </div>

      {/* Your Duties row */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '10px 14px 6px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '5px' }}>
          <span style={{ fontSize: '11px', fontWeight: '700', color: 'white' }}>Your Duties</span>
          <div style={{ width: '16px', height: '16px', borderRadius: '50%', background: BLUE, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <span style={{ fontSize: '8px', color: 'white', fontWeight: '700' }}>2</span>
          </div>
        </div>
        {/* Available badge — matching real app */}
        <div style={{ display:'flex', alignItems:'center', gap:'4px', padding:'3px 8px', borderRadius:'20px', background:'rgba(34,197,94,0.1)', border:'1px solid rgba(34,197,94,0.22)' }}>
          <div style={{ width: '5px', height: '5px', borderRadius: '50%', background: '#22C55E' }} />
          <span style={{ fontSize: '7.5px', color: '#22C55E', fontWeight: '700' }}>Available</span>
        </div>
      </div>

      {/* Task cards */}
      <div style={{ padding: '0 14px', display: 'flex', flexDirection: 'column', gap: '6px' }}>
        {[
          { label:'Garbage', sub:'Weekly · Overdue',   left:'#DC2626', bg:'rgba(220,38,38,0.09)',  badge:'Overdue', bc:'#F87171' },
          { label:'Mopping', sub:'Weekly · Due today', left:'#D97706', bg:'rgba(217,119,6,0.09)',  badge:'Today',   bc:'#FCD34D' },
        ].map(t => (
          <div key={t.label} style={{ display:'flex', alignItems:'center', gap:'8px', padding:'7px 10px', borderRadius:'10px', background:t.bg, borderLeft:`2.5px solid ${t.left}` }}>
            <div style={{ flex:1 }}>
              <p style={{ fontSize:'10px', fontWeight:'700', color:'white' }}>{t.label}</p>
              <p style={{ fontSize:'8px', color:'rgba(255,255,255,0.4)' }}>{t.sub}</p>
            </div>
            <span style={{ fontSize:'7.5px', fontWeight:'700', color:t.bc, background:`${t.left}22`, padding:'2px 6px', borderRadius:'20px', whiteSpace:'nowrap' }}>{t.badge}</span>
          </div>
        ))}
      </div>

      {/* Stats row */}
      <div style={{ display:'grid', gridTemplateColumns:'1fr 1fr 1fr', gap:'5px', padding:'8px 14px' }}>
        {[
          { label:'FLAT HEALTH', value:'72%',  color:'#FCD34D' },
          { label:'TOTAL TASKS', value:'5',    color:'white'   },
          { label:'SWAPS',       value:'1',    color:PURPLE_L  },
        ].map(s => (
          <div key={s.label} style={{ padding:'6px 8px', borderRadius:'10px', background:'rgba(255,255,255,0.03)' }}>
            <p style={{ fontSize:'6px', fontWeight:'700', color:'rgba(255,255,255,0.3)', letterSpacing:'0.08em', marginBottom:'2px' }}>{s.label}</p>
            <p style={{ fontSize:'13px', fontWeight:'800', color:s.color, lineHeight:1 }}>{s.value}</p>
          </div>
        ))}
      </div>

      {/* Rotation Order card */}
      <div style={{ margin:'0 14px', padding:'10px', borderRadius:'12px', background:'rgba(255,255,255,0.03)', border:`1px solid ${BORDER}` }}>
        <div style={{ display:'flex', alignItems:'center', gap:'5px', marginBottom:'10px' }}>
          <div style={{ width:'16px', height:'16px', borderRadius:'4px', background:BLUE, display:'flex', alignItems:'center', justifyContent:'center', fontSize:'9px', color:'white' }}>↻</div>
          <p style={{ fontSize:'9px', fontWeight:'700', color:'white' }}>Rotation Order</p>
          <p style={{ fontSize:'7px', color:'rgba(255,255,255,0.3)', marginLeft:'auto' }}>#2 in queue</p>
        </div>
        <div style={{ display:'flex', gap:'8px' }}>
          {rota.map(m => (
            <div key={m.name} style={{ display:'flex', flexDirection:'column', alignItems:'center', gap:'2px', flex:1 }}>
              <div style={{ position:'relative' }}>
                <div style={{ width:'28px', height:'28px', borderRadius:'50%', background:m.c, display:'flex', alignItems:'center', justifyContent:'center', fontSize:'10px', fontWeight:'800', color:'white', border: m.badge ? '1.5px solid rgba(255,255,255,0.35)' : 'none' }}>{m.i}</div>
                {m.badge && (
                  <div style={{ position:'absolute', bottom:'-5px', left:'50%', transform:'translateX(-50%)', background:m.badge==='NOW'?PURPLE:BLUE, fontSize:'5.5px', fontWeight:'800', color:'white', padding:'1px 4px', borderRadius:'4px', whiteSpace:'nowrap' }}>{m.badge}</div>
                )}
              </div>
              <p style={{ fontSize:'7px', color:'rgba(255,255,255,0.5)', marginTop:'5px', textAlign:'center' }}>{m.name}</p>
              <p style={{ fontSize:'6px', color:'rgba(255,255,255,0.25)', textAlign:'center' }}>{m.date}</p>
            </div>
          ))}
        </div>
      </div>

      <div style={{ flex:1 }} />
      <BottomNav5 active="dashboard" />
    </div>
  )
}

// ── Expenses Screen ───────────────────────────────────────────────────────────
// Faithfully recreates the Expenses Hub: cycle status card + 3 metrics + green balance card
function ExpensesScreen() {
  const list = [
    { i:'S', c:'#059669', name:'Sri Ram Murthy',    amt:'₹625' },
    { i:'M', c:PURPLE,    name:'MADKING',            amt:'₹625' },
    { i:'R', c:BLUE,      name:'rohin bellamkonda', amt:'₹625' },
    { i:'S', c:'#DB2777', name:'Sai Bellamkonda',   amt:'₹625' },
  ]
  return (
    <div style={{ background:'#09090D', display:'flex', flexDirection:'column', minHeight:'518px' }}>
      <StatusBar />
      <div style={{ padding:'4px 14px 10px', display:'flex', justifyContent:'space-between', alignItems:'flex-start' }}>
        <div>
          <p style={{ fontSize:'7.5px', color:'rgba(255,255,255,0.35)' }}>Ayyapa Nilayam · 1 Jun – 30 Jun</p>
          <p style={{ fontSize:'18px', fontWeight:'800', color:'white', lineHeight:1.1, marginTop:'2px' }}>Expenses Hub</p>
        </div>
        {/* Close Cycle button — matching real app */}
        <div style={{ display:'flex', alignItems:'center', gap:'4px', padding:'4px 8px', borderRadius:'8px', background:'rgba(255,255,255,0.06)', border:`1px solid ${BORDER}`, marginTop:'4px' }}>
          <svg width="8" height="8" viewBox="0 0 24 24" fill="none" stroke="rgba(255,255,255,0.4)" strokeWidth="2.2" strokeLinecap="round"><rect x="3" y="4" width="18" height="18" rx="2"/><path d="M16 2v4M8 2v4M3 10h18"/></svg>
          <span style={{ fontSize:'7px', fontWeight:'600', color:'rgba(255,255,255,0.35)' }}>Close Cycle</span>
        </div>
      </div>

      {/* Cycle status card */}
      <div style={{ margin:'0 14px 8px', padding:'11px', borderRadius:'14px', background:'rgba(255,255,255,0.035)', border:`1px solid ${BORDER}` }}>
        <div style={{ display:'flex', justifyContent:'space-between', alignItems:'flex-start', marginBottom:'9px' }}>
          <div>
            <p style={{ fontSize:'6.5px', fontWeight:'700', color:'rgba(255,255,255,0.3)', letterSpacing:'0.1em' }}>CURRENT CYCLE STATUS</p>
            <div style={{ display:'flex', alignItems:'center', gap:'5px', marginTop:'4px' }}>
              <div style={{ width:'15px', height:'15px', borderRadius:'50%', background:'rgba(251,146,60,0.18)', display:'flex', alignItems:'center', justifyContent:'center', fontSize:'8px' }}>⏱</div>
              <p style={{ fontSize:'10px', fontWeight:'700', color:'white' }}>Pending balances</p>
            </div>
            <p style={{ fontSize:'7px', color:'rgba(255,255,255,0.35)', marginTop:'1px' }}>7 unsettled</p>
          </div>
          <div style={{ textAlign:'right' }}>
            <p style={{ fontSize:'6.5px', color:'rgba(255,255,255,0.28)', marginBottom:'3px' }}>CYCLE PROGRESS</p>
            <p style={{ fontSize:'10px', fontWeight:'700', color:'white' }}>14 / 30 days</p>
            <div style={{ width:'60px', height:'3px', borderRadius:'2px', background:'rgba(255,255,255,0.1)', marginTop:'4px' }}>
              <div style={{ width:'47%', height:'100%', borderRadius:'2px', background:BLUE }} />
            </div>
          </div>
        </div>
        {/* 3 metric boxes — exactly as in the real app screenshot */}
        <div style={{ display:'grid', gridTemplateColumns:'1fr 1fr 1fr', gap:'4px' }}>
          {[
            { label:'YOU WILL RECEIVE', value:'₹4,375', sub:'From 7 people', tc:'#22C55E', bg:'rgba(5,150,105,0.12)'  },
            { label:'YOU NEED TO PAY',  value:'₹0',     sub:'To 0 people',   tc:'#F87171', bg:'rgba(220,38,38,0.1)'   },
            { label:'NET POSITION',     value:'+₹4,375', sub:'In your favor', tc:'#22C55E', bg:'rgba(37,99,235,0.08)' },
          ].map(box => (
            <div key={box.label} style={{ padding:'5px', borderRadius:'8px', background:box.bg }}>
              <p style={{ fontSize:'5.5px', fontWeight:'700', color:box.tc, letterSpacing:'0.05em', marginBottom:'2px', lineHeight:1.2 }}>{box.label}</p>
              <p style={{ fontSize:'11px', fontWeight:'900', color:box.tc, lineHeight:1 }}>{box.value}</p>
              <p style={{ fontSize:'6px', color:`${box.tc}80`, marginTop:'1px' }}>{box.sub}</p>
            </div>
          ))}
        </div>
      </div>

      {/* Tab switcher */}
      <div style={{ margin:'0 14px 8px', display:'flex', background:'rgba(255,255,255,0.04)', borderRadius:'10px', padding:'2px' }}>
        <div style={{ flex:1, padding:'5px', borderRadius:'8px', background:BLUE, textAlign:'center', fontSize:'8px', fontWeight:'700', color:'white' }}>Daily Splits</div>
        <div style={{ flex:1, padding:'5px', textAlign:'center', fontSize:'8px', fontWeight:'500', color:'rgba(255,255,255,0.32)' }}>Fixed Bills</div>
      </div>

      {/* Green balance card */}
      <div style={{ margin:'0 14px 8px', padding:'12px 14px', borderRadius:'14px', background:'linear-gradient(135deg,#064E3B 0%,#047857 100%)', display:'flex', justifyContent:'space-between', alignItems:'center' }}>
        <div>
          <p style={{ fontSize:'8px', color:'rgba(255,255,255,0.55)' }}>You will receive</p>
          <p style={{ fontSize:'22px', fontWeight:'900', color:'white', lineHeight:1.1 }}>₹4,375</p>
          <p style={{ fontSize:'7px', color:'rgba(255,255,255,0.45)', marginTop:'2px' }}>From 7 people</p>
        </div>
        <div style={{ width:'28px', height:'28px', borderRadius:'50%', background:'rgba(255,255,255,0.15)', display:'flex', alignItems:'center', justifyContent:'center', fontSize:'12px', color:'white' }}>↙</div>
      </div>

      {/* Balance list */}
      <p style={{ fontSize:'6.5px', fontWeight:'700', color:'rgba(255,255,255,0.28)', letterSpacing:'0.1em', padding:'0 14px 5px' }}>BALANCES IN DAILY SPLITS</p>
      <div style={{ flex:1, overflow:'hidden', padding:'0 14px', display:'flex', flexDirection:'column', gap:'4px' }}>
        {list.map(p => (
          <div key={p.name} style={{ display:'flex', alignItems:'center', gap:'8px', padding:'6px 8px', borderRadius:'10px', background:'rgba(255,255,255,0.025)' }}>
            <div style={{ width:'22px', height:'22px', borderRadius:'50%', background:p.c, display:'flex', alignItems:'center', justifyContent:'center', fontSize:'8px', fontWeight:'700', color:'white', flexShrink:0 }}>{p.i}</div>
            <div style={{ flex:1, minWidth:0 }}>
              <p style={{ fontSize:'9px', fontWeight:'600', color:'white', overflow:'hidden', whiteSpace:'nowrap', textOverflow:'ellipsis' }}>{p.name}</p>
              <p style={{ fontSize:'7.5px', color:'#22C55E' }}>Owes you</p>
            </div>
            <p style={{ fontSize:'10px', fontWeight:'700', color:'#22C55E', flexShrink:0 }}>{p.amt}</p>
          </div>
        ))}
      </div>
      <BottomNav5 active="expenses" />
    </div>
  )
}

// ── Swaps Screen ──────────────────────────────────────────────────────────────
function SwapsScreen() {
  return (
    <div style={{ background:'#09090D', display:'flex', flexDirection:'column', minHeight:'518px' }}>
      <StatusBar />
      <div style={{ padding:'4px 14px 12px', display:'flex', justifyContent:'space-between', alignItems:'flex-start' }}>
        <div>
          <p style={{ fontSize:'19px', fontWeight:'800', color:'white', lineHeight:1.1 }}>Swap Requests</p>
          <p style={{ fontSize:'8px', color:'rgba(255,255,255,0.35)', marginTop:'2px' }}>Task coverage requests</p>
        </div>
        {/* Invite button — matching real app header */}
        <div style={{ display:'flex', alignItems:'center', gap:'4px', padding:'4px 9px', borderRadius:'8px', background:'rgba(255,255,255,0.06)', border:`1px solid ${BORDER}`, marginTop:'6px' }}>
          <svg width="8" height="8" viewBox="0 0 24 24" fill="none" stroke="rgba(255,255,255,0.45)" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round"><path d="M18 8a3 3 0 1 0 0-6 3 3 0 0 0 0 6ZM6 15a3 3 0 1 0 0-6 3 3 0 0 0 0 6ZM18 20a3 3 0 1 0 0-6 3 3 0 0 0 0 6Z"/><path d="M8.59 13.51l6.83 3.98M15.41 6.51l-6.82 3.98"/></svg>
          <span style={{ fontSize:'7px', fontWeight:'600', color:'rgba(255,255,255,0.4)' }}>Invite</span>
        </div>
      </div>

      {/* Incoming request */}
      <div style={{ margin:'0 14px 12px', padding:'12px', borderRadius:'14px', background:'rgba(37,99,235,0.1)', border:'1px solid rgba(37,99,235,0.28)' }}>
        <div style={{ display:'flex', alignItems:'center', gap:'4px', marginBottom:'9px' }}>
          <div style={{ width:'6px', height:'6px', borderRadius:'50%', background:BLUE }} />
          <p style={{ fontSize:'7.5px', fontWeight:'700', color:BLUE_L, letterSpacing:'0.08em' }}>INCOMING REQUEST</p>
        </div>
        <div style={{ display:'flex', alignItems:'center', gap:'10px', marginBottom:'10px' }}>
          <div style={{ width:'34px', height:'34px', borderRadius:'50%', background:PURPLE, display:'flex', alignItems:'center', justifyContent:'center', fontSize:'13px', fontWeight:'800', color:'white', flexShrink:0 }}>M</div>
          <div>
            <p style={{ fontSize:'11px', fontWeight:'700', color:'white' }}>MADKING → you</p>
            <p style={{ fontSize:'8px', color:'rgba(255,255,255,0.45)', marginTop:'1px' }}>Swap <span style={{ color:'white', fontWeight:'600' }}>Clean Bathrooms</span></p>
            <p style={{ fontSize:'8px', color:'rgba(255,255,255,0.35)' }}>Jun 21 ↔ Jun 28</p>
          </div>
        </div>
        <p style={{ fontSize:'8px', color:'rgba(255,255,255,0.4)', marginBottom:'10px', fontStyle:'italic' }}>&ldquo;I have a family event that weekend&rdquo;</p>
        <div style={{ display:'flex', gap:'6px' }}>
          <button style={{ flex:1, padding:'8px', borderRadius:'10px', background:BLUE, fontSize:'10px', fontWeight:'700', color:'white' }}>Accept</button>
          <button style={{ padding:'8px 12px', borderRadius:'10px', fontSize:'10px', fontWeight:'600', color:'rgba(255,255,255,0.4)', background:'rgba(255,255,255,0.05)', border:`1px solid ${BORDER}` }}>Decline</button>
        </div>
      </div>

      {/* History */}
      <p style={{ fontSize:'6.5px', fontWeight:'700', color:'rgba(255,255,255,0.28)', letterSpacing:'0.1em', padding:'0 14px 6px' }}>RECENT HISTORY</p>
      <div style={{ flex:1, overflow:'hidden', padding:'0 14px', display:'flex', flexDirection:'column', gap:'5px' }}>
        {[
          { i:'P', c:'#7C3AED', who:'Priya',  task:'Kitchen → Grocery',   date:'Jun 10', ok:true  },
          { i:'A', c:'#DB2777', who:'Ankit',  task:'Trash → Cook Dinner', date:'Jun 6',  ok:false },
          { i:'K', c:'#DC2626', who:'Kiran',  task:'Mopping → Bathroom',  date:'Jun 3',  ok:true  },
        ].map(h => (
          <div key={h.task} style={{ display:'flex', alignItems:'center', gap:'8px', padding:'6px 8px', borderRadius:'10px', background:'rgba(255,255,255,0.025)' }}>
            <div style={{ width:'22px', height:'22px', borderRadius:'50%', background:h.c, display:'flex', alignItems:'center', justifyContent:'center', fontSize:'8px', fontWeight:'700', color:'white', flexShrink:0 }}>{h.i}</div>
            <div style={{ flex:1, minWidth:0 }}>
              <p style={{ fontSize:'9px', fontWeight:'600', color:'white' }}>{h.who}</p>
              <p style={{ fontSize:'7.5px', color:'rgba(255,255,255,0.35)', overflow:'hidden', whiteSpace:'nowrap', textOverflow:'ellipsis' }}>{h.task}</p>
            </div>
            <div style={{ textAlign:'right', flexShrink:0 }}>
              <p style={{ fontSize:'8px', fontWeight:'700', color:h.ok?'#22C55E':'#F87171' }}>{h.ok?'Accepted':'Declined'}</p>
              <p style={{ fontSize:'7px', color:'rgba(255,255,255,0.25)' }}>{h.date}</p>
            </div>
          </div>
        ))}
      </div>
      <BottomNav5 active="tasks" />
    </div>
  )
}

// ── macOS Browser Frame ────────────────────────────────────────────────────────
function BrowserFrame({ children, url = 'habitiq.app/dashboard' }: { children: ReactNode; url?: string }) {
  return (
    <div style={{ borderRadius:'14px', overflow:'hidden', background:'#09090D', border:'1px solid rgba(255,255,255,0.1)', boxShadow:'0 0 0 1px rgba(255,255,255,0.04),0 80px 180px rgba(0,0,0,0.92),inset 0 1px 0 rgba(255,255,255,0.07)' }}>
      <div style={{ display:'flex', alignItems:'center', padding:'10px 16px', background:'#111116', borderBottom:'1px solid rgba(255,255,255,0.07)', minHeight:'38px' }}>
        <div style={{ display:'flex', gap:'6px', flexShrink:0 }}>
          {['#FF5F57','#FFBD2E','#28CA41'].map((c,i) => <div key={i} style={{ width:'11px', height:'11px', borderRadius:'50%', background:c }} />)}
        </div>
        <div style={{ flex:1, display:'flex', justifyContent:'center' }}>
          <div style={{ display:'flex', alignItems:'center', gap:'6px', padding:'4px 12px', borderRadius:'7px', background:'rgba(255,255,255,0.05)', minWidth:'180px', maxWidth:'280px' }}>
            <svg width="9" height="9" viewBox="0 0 24 24" fill="none" stroke="rgba(255,255,255,0.25)" strokeWidth="2" strokeLinecap="round"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>
            <span style={{ fontSize:'10px', color:'rgba(255,255,255,0.28)' }}>{url}</span>
          </div>
        </div>
        <div style={{ width:'50px', flexShrink:0 }} />
      </div>
      {children}
    </div>
  )
}

// ── Desktop App Sidebar ────────────────────────────────────────────────────────
function DesktopSidebar({ active }: { active: 'dashboard' | 'expenses' | 'members' }) {
  const mainNav  = [{ id:'dashboard', label:'Dashboard' },{ id:'insights', label:'Insights' },{ id:'expenses', label:'Expenses' },{ id:'swaps', label:'Swaps' }]
  const genNav   = [{ id:'members',  label:'Members' },{ id:'about', label:'About' }]
  return (
    <div style={{ width:'168px', flexShrink:0, background:'#08080C', borderRight:'1px solid rgba(255,255,255,0.06)', display:'flex', flexDirection:'column', minHeight:'460px' }}>
      <div style={{ padding:'14px 14px 10px', borderBottom:'1px solid rgba(255,255,255,0.05)' }}>
        <img src="/habitiq-logo.svg" alt="Habitiq" style={{ height:'18px', width:'auto', filter:'brightness(0) invert(1)', display:'block', marginBottom:'4px' }} />
        <p style={{ fontSize:'8.5px', color:'rgba(255,255,255,0.28)', marginBottom:'8px' }}>Ayyapa Nilayam</p>
        <div style={{ padding:'4px 8px', borderRadius:'6px', background:'rgba(220,38,38,0.13)', border:'1px solid rgba(220,38,38,0.28)', display:'flex', alignItems:'center', gap:'5px' }}>
          <div style={{ width:'5px', height:'5px', borderRadius:'50%', background:'#EF4444', flexShrink:0 }} />
          <span style={{ fontSize:'8px', fontWeight:'600', color:'#FCA5A5' }}>1 overdue task</span>
        </div>
      </div>
      <div style={{ padding:'10px 8px 0' }}>
        <p style={{ fontSize:'7.5px', fontWeight:'700', color:'rgba(255,255,255,0.2)', letterSpacing:'0.1em', padding:'0 8px', marginBottom:'4px' }}>MAIN</p>
        {mainNav.map(n => {
          const on = n.id === active
          return (
            <div key={n.id} style={{ display:'flex', alignItems:'center', gap:'9px', padding:'6px 8px', borderRadius:'8px', marginBottom:'2px', background:on?`${BLUE}1A`:'transparent' }}>
              <div style={{ width:'7px', height:'7px', borderRadius:'1.5px', background:on?BLUE:'rgba(255,255,255,0.2)', flexShrink:0 }} />
              <span style={{ fontSize:'10.5px', fontWeight:on?'700':'500', color:on?'white':'rgba(255,255,255,0.42)' }}>{n.label}</span>
              {on && <svg style={{ marginLeft:'auto' }} width="10" height="10" viewBox="0 0 24 24" fill="none" stroke={BLUE_L} strokeWidth="2.5" strokeLinecap="round"><path d="M9 18l6-6-6-6"/></svg>}
            </div>
          )
        })}
      </div>
      <div style={{ padding:'8px 8px 0' }}>
        <p style={{ fontSize:'7.5px', fontWeight:'700', color:'rgba(255,255,255,0.2)', letterSpacing:'0.1em', padding:'0 8px', marginBottom:'4px' }}>GENERAL</p>
        {genNav.map(n => {
          const on = n.id === active
          return (
            <div key={n.id} style={{ display:'flex', alignItems:'center', gap:'9px', padding:'6px 8px', borderRadius:'8px', marginBottom:'2px', background:on?`${BLUE}1A`:'transparent' }}>
              <div style={{ width:'7px', height:'7px', borderRadius:'1.5px', background:on?BLUE:'rgba(255,255,255,0.2)', flexShrink:0 }} />
              <span style={{ fontSize:'10.5px', fontWeight:on?'700':'500', color:on?'white':'rgba(255,255,255,0.42)' }}>{n.label}</span>
            </div>
          )
        })}
      </div>
      <div style={{ flex:1 }} />
      <div style={{ borderTop:'1px solid rgba(255,255,255,0.05)', padding:'10px 12px' }}>
        <div style={{ display:'flex', alignItems:'center', gap:'6px', padding:'5px 6px', borderRadius:'8px', background:'rgba(255,255,255,0.03)', marginBottom:'8px' }}>
          <div style={{ width:'22px', height:'22px', borderRadius:'5px', background:BLUE, display:'flex', alignItems:'center', justifyContent:'center', fontSize:'9px', fontWeight:'800', color:'white', flexShrink:0 }}>A</div>
          <p style={{ fontSize:'8.5px', fontWeight:'600', color:'rgba(255,255,255,0.5)' }}>Ayyapa Nilayam</p>
        </div>
        <div style={{ display:'flex', alignItems:'center', gap:'6px', padding:'4px 6px' }}>
          <div style={{ width:'22px', height:'22px', borderRadius:'50%', background:PURPLE, display:'flex', alignItems:'center', justifyContent:'center', fontSize:'9px', fontWeight:'800', color:'white', flexShrink:0 }}>J</div>
          <div>
            <p style={{ fontSize:'9px', fontWeight:'600', color:'rgba(255,255,255,0.5)' }}>jaswanth evs</p>
            <p style={{ fontSize:'7.5px', color:'rgba(255,255,255,0.22)' }}>Member</p>
          </div>
        </div>
      </div>
    </div>
  )
}

// ── Desktop: Dashboard content ─────────────────────────────────────────────────
function DashboardDesktopContent() {
  return (
    <div style={{ flex:1, background:'#09090D', display:'flex', flexDirection:'column', minHeight:'460px' }}>
      <div style={{ position:'relative', overflow:'hidden', minHeight:'120px' }}>
        <div style={{ position:'absolute', inset:0, background:'linear-gradient(150deg,#C2410C 0%,#9D174D 38%,#4C1D95 65%,transparent 88%)', opacity:0.82 }} />
        <div style={{ position:'absolute', right:'-20px', bottom:'-20px', width:'160px', height:'160px', background:'radial-gradient(circle,rgba(251,146,60,0.45) 0%,transparent 65%)' }} />
        <div style={{ position:'relative', zIndex:1, padding:'20px 24px' }}>
          <p style={{ fontSize:'9px', fontWeight:'700', color:'rgba(255,255,255,0.5)', letterSpacing:'0.14em', marginBottom:'4px' }}>EVENING</p>
          <p style={{ fontSize:'22px', fontWeight:'800', color:'white', lineHeight:1 }}>Good evening, jaswanth</p>
          <p style={{ fontSize:'11px', color:'rgba(255,255,255,0.42)', marginTop:'4px' }}>Your duties for today</p>
        </div>
      </div>
      <div style={{ padding:'12px 16px 10px', borderBottom:'1px solid rgba(255,255,255,0.05)' }}>
        <div style={{ display:'flex', alignItems:'center', justifyContent:'space-between', marginBottom:'10px' }}>
          <div style={{ display:'flex', alignItems:'center', gap:'6px' }}>
            <div style={{ width:'18px', height:'18px', borderRadius:'4px', background:'rgba(255,255,255,0.07)', display:'flex', alignItems:'center', justifyContent:'center', fontSize:'11px', color:'rgba(255,255,255,0.5)' }}>$</div>
            <span style={{ fontSize:'12px', fontWeight:'700', color:'white' }}>Bills & Expenses</span>
          </div>
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="rgba(255,255,255,0.25)" strokeWidth="2" strokeLinecap="round"><path d="M9 18l6-6-6-6"/></svg>
        </div>
        <div style={{ display:'grid', gridTemplateColumns:'1fr 1fr 1fr', gap:'12px' }}>
          {[{ label:'SPLITS', value:'₹9,370', color:'white' },{ label:'YOU OWE', value:'—', color:'rgba(255,255,255,0.35)' },{ label:'OWED TO YOU', value:'₹4,375', color:'#22C55E' }].map(s => (
            <div key={s.label}>
              <p style={{ fontSize:'8px', fontWeight:'700', color:'rgba(255,255,255,0.28)', letterSpacing:'0.1em', marginBottom:'3px' }}>{s.label}</p>
              <p style={{ fontSize:'15px', fontWeight:'800', color:s.color }}>{s.value}</p>
            </div>
          ))}
        </div>
      </div>
      <div style={{ padding:'12px 16px', flex:1 }}>
        <div style={{ display:'flex', alignItems:'center', justifyContent:'space-between', marginBottom:'10px' }}>
          <span style={{ fontSize:'12px', fontWeight:'700', color:'white' }}>Your Duties</span>
          <div style={{ display:'flex', alignItems:'center', gap:'5px', padding:'4px 10px', borderRadius:'20px', background:'rgba(34,197,94,0.1)', border:'1px solid rgba(34,197,94,0.2)' }}>
            <div style={{ width:'5px', height:'5px', borderRadius:'50%', background:'#22C55E' }} />
            <span style={{ fontSize:'9px', fontWeight:'600', color:'#22C55E' }}>Available</span>
          </div>
        </div>
        <div style={{ display:'flex', flexDirection:'column', alignItems:'center', justifyContent:'center', padding:'20px', borderRadius:'10px', background:'rgba(255,255,255,0.02)', border:'1px solid rgba(255,255,255,0.05)' }}>
          <div style={{ width:'30px', height:'30px', borderRadius:'50%', background:'rgba(34,197,94,0.15)', border:'1px solid rgba(34,197,94,0.3)', display:'flex', alignItems:'center', justifyContent:'center', marginBottom:'7px' }}>
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#22C55E" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><path d="M20 6L9 17l-5-5"/></svg>
          </div>
          <p style={{ fontSize:'12px', fontWeight:'700', color:'rgba(255,255,255,0.7)', marginBottom:'2px' }}>All clear!</p>
          <p style={{ fontSize:'10px', color:'rgba(255,255,255,0.3)' }}>No pending tasks right now.</p>
        </div>
      </div>
      <div style={{ borderTop:'1px solid rgba(255,255,255,0.05)', display:'grid', gridTemplateColumns:'1fr 1fr 1fr', background:'rgba(255,255,255,0.015)' }}>
        {[{ label:'FLAT HEALTH', value:'0%', color:'#FCD34D' },{ label:'TOTAL TASKS', value:'1', color:'white' },{ label:'SWAPS', value:'0', color:PURPLE_L }].map((s,i) => (
          <div key={s.label} style={{ padding:'10px 20px', borderRight:i<2?'1px solid rgba(255,255,255,0.05)':'none' }}>
            <p style={{ fontSize:'8px', fontWeight:'700', color:'rgba(255,255,255,0.28)', letterSpacing:'0.1em', marginBottom:'2px' }}>{s.label}</p>
            <p style={{ fontSize:'16px', fontWeight:'800', color:s.color, lineHeight:1 }}>{s.value}</p>
          </div>
        ))}
      </div>
    </div>
  )
}

// ── Desktop: Expenses content ──────────────────────────────────────────────────
function ExpensesDesktopContent() {
  const people = [
    { i:'S', c:'#059669', name:'Sri Ram Murthy',    amt:'₹625' },
    { i:'M', c:PURPLE,    name:'MADKING',            amt:'₹625' },
    { i:'R', c:BLUE,      name:'rohin bellamkonda',  amt:'₹625' },
    { i:'S', c:'#DB2777', name:'Sai Bellamkonda',    amt:'₹625' },
    { i:'K', c:'#DC2626', name:'Kiran Naidu',        amt:'₹625' },
    { i:'B', c:'#047857', name:'Bhanu Kalyan',       amt:'₹625' },
  ]
  return (
    <div style={{ flex:1, background:'#09090D', display:'flex', flexDirection:'column', minHeight:'460px' }}>
      <div style={{ padding:'14px 16px 10px', borderBottom:'1px solid rgba(255,255,255,0.06)' }}>
        <p style={{ fontSize:'9px', color:'rgba(255,255,255,0.3)', marginBottom:'3px' }}>Ayyapa Nilayam · 1 Jun – 30 Jun</p>
        <p style={{ fontSize:'20px', fontWeight:'800', color:'white', lineHeight:1 }}>Expenses Hub</p>
      </div>
      <div style={{ margin:'10px 16px', padding:'12px', borderRadius:'12px', background:'rgba(255,255,255,0.03)', border:`1px solid ${BORDER}` }}>
        <div style={{ display:'flex', justifyContent:'space-between', marginBottom:'12px' }}>
          <div>
            <p style={{ fontSize:'8px', fontWeight:'700', color:'rgba(255,255,255,0.28)', letterSpacing:'0.1em', marginBottom:'5px' }}>CURRENT CYCLE STATUS</p>
            <div style={{ display:'flex', alignItems:'center', gap:'7px', marginBottom:'2px' }}>
              <div style={{ width:'18px', height:'18px', borderRadius:'50%', background:'rgba(251,146,60,0.15)', display:'flex', alignItems:'center', justifyContent:'center', fontSize:'10px' }}>⏱</div>
              <p style={{ fontSize:'13px', fontWeight:'700', color:'white' }}>Pending balances</p>
            </div>
            <p style={{ fontSize:'9px', color:'rgba(255,255,255,0.3)' }}>7 unsettled</p>
          </div>
          <div style={{ textAlign:'right' }}>
            <p style={{ fontSize:'8px', color:'rgba(255,255,255,0.28)', marginBottom:'4px' }}>CYCLE PROGRESS</p>
            <p style={{ fontSize:'14px', fontWeight:'700', color:'white' }}>14 / 30 days</p>
            <div style={{ width:'80px', height:'3px', borderRadius:'2px', background:'rgba(255,255,255,0.1)', marginTop:'6px', marginLeft:'auto' }}>
              <div style={{ width:'47%', height:'100%', borderRadius:'2px', background:BLUE }} />
            </div>
          </div>
        </div>
        <div style={{ display:'grid', gridTemplateColumns:'1fr 1fr 1fr', gap:'8px' }}>
          {[{ label:'YOU WILL RECEIVE', value:'₹4,375', sub:'From 7 people', color:'#22C55E', bg:'rgba(5,150,105,0.1)' },{ label:'YOU NEED TO PAY', value:'₹0', sub:'To 0 people', color:'#F87171', bg:'rgba(220,38,38,0.1)' },{ label:'NET POSITION', value:'+₹4,375', sub:'In your favor', color:'#22C55E', bg:'rgba(37,99,235,0.07)' }].map(b => (
            <div key={b.label} style={{ padding:'8px 10px', borderRadius:'8px', background:b.bg }}>
              <p style={{ fontSize:'7px', fontWeight:'700', color:b.color, letterSpacing:'0.05em', marginBottom:'3px', lineHeight:1.2 }}>{b.label}</p>
              <p style={{ fontSize:'15px', fontWeight:'900', color:b.color, lineHeight:1 }}>{b.value}</p>
              <p style={{ fontSize:'8px', color:`${b.color}80`, marginTop:'2px' }}>{b.sub}</p>
            </div>
          ))}
        </div>
      </div>
      <div style={{ padding:'0 16px', flex:1 }}>
        <div style={{ display:'flex', background:'rgba(255,255,255,0.04)', borderRadius:'10px', padding:'2px', marginBottom:'10px' }}>
          <div style={{ flex:1, padding:'6px', borderRadius:'8px', background:BLUE, textAlign:'center', fontSize:'11px', fontWeight:'700', color:'white' }}>Daily Splits</div>
          <div style={{ flex:1, padding:'6px', textAlign:'center', fontSize:'11px', color:'rgba(255,255,255,0.3)' }}>Fixed Bills</div>
        </div>
        <div style={{ padding:'12px 16px', borderRadius:'12px', background:'linear-gradient(135deg,#064E3B,#047857)', display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:'10px' }}>
          <div>
            <p style={{ fontSize:'9px', color:'rgba(255,255,255,0.55)' }}>You will receive</p>
            <p style={{ fontSize:'20px', fontWeight:'900', color:'white', lineHeight:1.1 }}>₹4,375</p>
            <p style={{ fontSize:'8px', color:'rgba(255,255,255,0.45)', marginTop:'2px' }}>From 7 people</p>
          </div>
          <div style={{ width:'28px', height:'28px', borderRadius:'50%', background:'rgba(255,255,255,0.15)', display:'flex', alignItems:'center', justifyContent:'center', fontSize:'13px', color:'white' }}>↙</div>
        </div>
        <div style={{ display:'flex', flexDirection:'column', gap:'4px' }}>
          {people.map(p => (
            <div key={p.name} style={{ display:'flex', alignItems:'center', gap:'10px', padding:'7px 10px', borderRadius:'8px', background:'rgba(255,255,255,0.025)' }}>
              <div style={{ width:'26px', height:'26px', borderRadius:'50%', background:p.c, display:'flex', alignItems:'center', justifyContent:'center', fontSize:'10px', fontWeight:'700', color:'white', flexShrink:0 }}>{p.i}</div>
              <div style={{ flex:1, minWidth:0 }}>
                <p style={{ fontSize:'11px', fontWeight:'600', color:'white', overflow:'hidden', whiteSpace:'nowrap', textOverflow:'ellipsis' }}>{p.name}</p>
                <p style={{ fontSize:'8.5px', color:'#22C55E' }}>Owes you</p>
              </div>
              <p style={{ fontSize:'12px', fontWeight:'700', color:'#22C55E', flexShrink:0 }}>{p.amt}</p>
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}

// ── Desktop: Members content ───────────────────────────────────────────────────
function MembersDesktopContent() {
  const members = [
    { i:'S', c:'#059669', name:'Sri Ram Murthy' },
    { i:'M', c:PURPLE,    name:'MADKING' },
    { i:'r', c:BLUE,      name:'rohin bellamkonda' },
    { i:'J', c:'#D97706', name:'jaswanth evs' },
    { i:'S', c:'#DB2777', name:'Sai Bellamkonda' },
    { i:'K', c:'#DC2626', name:'Kiran Naidu Bellamkonda' },
    { i:'m', c:'#6B7280', name:'mani atava' },
    { i:'B', c:'#047857', name:'Bhanu Kalyan Upputuri' },
  ]
  return (
    <div style={{ flex:1, background:'#09090D', display:'flex', flexDirection:'column', minHeight:'460px' }}>
      <div style={{ padding:'14px 16px 10px', borderBottom:'1px solid rgba(255,255,255,0.06)' }}>
        <p style={{ fontSize:'20px', fontWeight:'800', color:'white', lineHeight:1 }}>Members</p>
        <p style={{ fontSize:'10px', color:'rgba(255,255,255,0.3)', marginTop:'3px' }}>Your flatmates and their current status.</p>
      </div>
      <div style={{ display:'grid', gridTemplateColumns:'repeat(2,1fr)', gap:'8px', padding:'12px 16px 10px' }}>
        {[{ label:'TOTAL', value:'8', sub:'flatmates', color:'white' },{ label:'AVAILABLE', value:'8', sub:'0 absent', color:GREEN_L },{ label:'AVG SCORE', value:'100', sub:'reliability', color:'#FCD34D' },{ label:'TOP', value:'Sri Ram', sub:'100 score', color:PURPLE_L }].map(s => (
          <div key={s.label} style={{ padding:'10px 12px', borderRadius:'10px', background:'rgba(255,255,255,0.03)', border:`1px solid ${BORDER}` }}>
            <p style={{ fontSize:'7.5px', fontWeight:'700', color:'rgba(255,255,255,0.25)', letterSpacing:'0.1em', marginBottom:'4px' }}>{s.label}</p>
            <p style={{ fontSize:'16px', fontWeight:'800', color:s.color, lineHeight:1, marginBottom:'2px' }}>{s.value}</p>
            <p style={{ fontSize:'8px', color:'rgba(255,255,255,0.25)' }}>{s.sub}</p>
          </div>
        ))}
      </div>
      <div style={{ padding:'0 16px', flex:1, overflow:'hidden' }}>
        <p style={{ fontSize:'10px', color:'rgba(255,255,255,0.28)', marginBottom:'8px' }}>8 of 8 flatmates</p>
        <div style={{ display:'flex', flexDirection:'column', gap:'3px' }}>
          {members.map(m => (
            <div key={m.name} style={{ display:'flex', alignItems:'center', gap:'10px', padding:'7px 10px', borderRadius:'8px', background:'rgba(255,255,255,0.02)', borderBottom:'1px solid rgba(255,255,255,0.04)' }}>
              <div style={{ width:'28px', height:'28px', borderRadius:'50%', background:m.c, display:'flex', alignItems:'center', justifyContent:'center', fontSize:'11px', fontWeight:'700', color:'white', flexShrink:0 }}>{m.i}</div>
              <div style={{ flex:1 }}>
                <p style={{ fontSize:'11px', fontWeight:'600', color:'white' }}>{m.name}</p>
                <div style={{ display:'flex', alignItems:'center', gap:'5px' }}>
                  <div style={{ width:'5px', height:'5px', borderRadius:'50%', background:'#22C55E' }} />
                  <span style={{ fontSize:'8px', color:'rgba(255,255,255,0.35)' }}>Available · 100</span>
                </div>
              </div>
              <span style={{ fontSize:'9px', color:'rgba(255,255,255,0.25)' }}>1 in queue</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}

// ── Desktop showcase section ───────────────────────────────────────────────────
function DesktopShowcase() {
  const [tab, setTab] = useState<'dashboard' | 'expenses' | 'members'>('dashboard')
  const tabs = [
    { id:'dashboard' as const, label:'Dashboard',    accent:'#C2410C', accentL:'#FB923C' },
    { id:'expenses'  as const, label:'Expenses Hub', accent:'#059669', accentL:GREEN_L   },
    { id:'members'   as const, label:'Members',      accent:PURPLE,    accentL:PURPLE_L  },
  ]
  const urlMap: Record<'dashboard'|'expenses'|'members',string> = {
    dashboard:'habitiq.app/dashboard',
    expenses: 'habitiq.app/expenses',
    members:  'habitiq.app/members',
  }
  const cur = tabs.find(t => t.id === tab)!

  const TabSwitcher = () => (
    <div style={{ display:'flex', gap:'6px', justifyContent:'center', marginBottom:'24px', flexWrap:'wrap' }}>
      {tabs.map(t => (
        <button key={t.id} onClick={() => setTab(t.id)}
          style={{ padding:'7px 18px', borderRadius:'10px', fontSize:'12px', fontWeight:'600', cursor:'pointer', border:'none', transition:'all 0.22s',
            background: tab===t.id ? `${t.accent}22` : 'rgba(255,255,255,0.04)',
            color: tab===t.id ? 'white' : 'rgba(255,255,255,0.35)',
            outline: tab===t.id ? `1px solid ${t.accent}44` : '1px solid rgba(255,255,255,0.06)',
          }}>
          {t.label}
        </button>
      ))}
    </div>
  )

  return (
    <section className="py-16 lg:py-28 px-5 lg:px-8" style={{ background:BG, position:'relative', overflow:'hidden' }}>
      <div style={{ position:'absolute', pointerEvents:'none', top:'50%', left:'50%', transform:'translate(-50%,-50%)', width:'70vw', height:'70vw', background:`radial-gradient(circle,${cur.accent}12 0%,transparent 65%)`, filter:'blur(80px)', transition:'background 0.6s' }} />
      <div style={{ position:'relative', zIndex:1, maxWidth:'1280px', margin:'0 auto' }}>

        <motion.div style={{ textAlign:'center', marginBottom:'32px' }}
          initial={{ opacity:0, y:24 }} whileInView={{ opacity:1, y:0 }}
          viewport={{ once:true, margin:'-80px' }} transition={{ duration:0.8, ease:EASE }}>
          <span style={{ display:'inline-block', marginBottom:'16px', fontSize:'11px', fontWeight:'700', letterSpacing:'0.14em', textTransform:'uppercase', padding:'6px 14px', borderRadius:'9999px', color:PURPLE_L, background:`${PURPLE}12`, border:`1px solid ${PURPLE}28` }}>Desktop App</span>
          <h2 className="font-black text-white" style={{ fontSize:'clamp(1.8rem,3.8vw,3rem)', letterSpacing:'-0.035em', fontFamily:'var(--font-inter)' }}>
            Powerful on every screen.
          </h2>
          <p className="mx-auto" style={{ marginTop:'12px', fontSize:'clamp(13px,1.6vw,15px)', color:'rgba(255,255,255,0.32)', maxWidth:'440px' }}>
            Full dashboard, every metric — in your browser. No install, no friction.
          </p>
        </motion.div>

        <motion.div initial={{ opacity:0, y:10 }} whileInView={{ opacity:1, y:0 }}
          viewport={{ once:true }} transition={{ duration:0.5, ease:EASE, delay:0.1 }}>
          <TabSwitcher />
        </motion.div>

        {/* ── MOBILE: no sidebar, compact card fits within viewport ────────── */}
        <motion.div className="lg:hidden"
          initial={{ opacity:0, y:20 }} whileInView={{ opacity:1, y:0 }}
          viewport={{ once:true }} transition={{ duration:0.7, ease:EASE, delay:0.05 }}>
          <div style={{ borderRadius:'16px', overflow:'hidden', border:'1px solid rgba(255,255,255,0.1)', boxShadow:`0 40px 100px rgba(0,0,0,0.8),0 0 0 1px ${cur.accent}18` }}>
            {/* Mini browser chrome */}
            <div style={{ display:'flex', alignItems:'center', gap:'8px', padding:'8px 12px', background:'#111116', borderBottom:'1px solid rgba(255,255,255,0.07)' }}>
              <div style={{ display:'flex', gap:'4px', flexShrink:0 }}>
                {['#FF5F57','#FFBD2E','#28CA41'].map((c,i) => <div key={i} style={{ width:'9px', height:'9px', borderRadius:'50%', background:c }} />)}
              </div>
              <div style={{ flex:1, display:'flex', alignItems:'center', gap:'5px', padding:'3px 8px', borderRadius:'5px', background:'rgba(255,255,255,0.05)' }}>
                <svg width="8" height="8" viewBox="0 0 24 24" fill="none" stroke="rgba(255,255,255,0.25)" strokeWidth="2" strokeLinecap="round"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>
                <span style={{ fontSize:'9px', color:'rgba(255,255,255,0.25)' }}>{urlMap[tab]}</span>
              </div>
            </div>
            {/* Content — full width, no sidebar. Clean and readable on mobile */}
            <AnimatePresence mode="wait">
              <motion.div key={`mob-${tab}`}
                initial={{ opacity:0 }} animate={{ opacity:1 }} exit={{ opacity:0 }}
                transition={{ duration:0.22 }}>
                {tab==='dashboard' && <DashboardDesktopContent />}
                {tab==='expenses'  && <ExpensesDesktopContent />}
                {tab==='members'   && <MembersDesktopContent />}
              </motion.div>
            </AnimatePresence>
          </div>
        </motion.div>

        {/* ── DESKTOP: full browser frame with sidebar ─────────────────────── */}
        <motion.div className="hidden lg:block"
          initial={{ opacity:0, y:32, scale:0.97 }} whileInView={{ opacity:1, y:0, scale:1 }}
          viewport={{ once:true, margin:'-60px' }} transition={{ duration:1, ease:EASE, delay:0.08 }}>
          <BrowserFrame url={urlMap[tab]}>
            <div style={{ display:'flex' }}>
              <DesktopSidebar active={tab} />
              <AnimatePresence mode="wait">
                <motion.div key={`dsk-${tab}`} style={{ flex:1, minWidth:0 }}
                  initial={{ opacity:0, x:14 }} animate={{ opacity:1, x:0 }} exit={{ opacity:0, x:-14 }}
                  transition={{ duration:0.28, ease:EASE }}>
                  {tab==='dashboard' && <DashboardDesktopContent />}
                  {tab==='expenses'  && <ExpensesDesktopContent />}
                  {tab==='members'   && <MembersDesktopContent />}
                </motion.div>
              </AnimatePresence>
            </div>
          </BrowserFrame>
        </motion.div>

      </div>
    </section>
  )
}

// ── Cycling phone ─────────────────────────────────────────────────────────────
const SCREEN_IDS = ['dashboard', 'expenses', 'swaps'] as const
type ScreenId = (typeof SCREEN_IDS)[number]
const SCREEN_META: Record<ScreenId, { label: string; glow: string }> = {
  dashboard: { label:'Duty Dashboard', glow:'rgba(194,65,12,0.3)'  },
  expenses:  { label:'Expenses Hub',   glow:'rgba(5,150,105,0.25)' },
  swaps:     { label:'Swap Requests',  glow:'rgba(79,70,229,0.25)' },
}

function ScreenContent({ id }: { id: ScreenId }) {
  if (id === 'dashboard') return <DashboardScreen />
  if (id === 'expenses')  return <ExpensesScreen />
  return <SwapsScreen />
}

function CyclingPhone() {
  const [idx, setIdx] = useState(0)
  useEffect(() => {
    const t = setInterval(() => setIdx(i => (i + 1) % 3), 3400)
    return () => clearInterval(t)
  }, [])
  const id = SCREEN_IDS[idx]
  return (
    <div style={{ display:'flex', flexDirection:'column', alignItems:'center', gap:'24px' }}>
      <PhoneFrame glow={SCREEN_META[id].glow}>
        <AnimatePresence mode="wait">
          <motion.div key={idx} initial={{ opacity:0, y:14, scale:0.97 }} animate={{ opacity:1, y:0, scale:1 }} exit={{ opacity:0, y:-14, scale:0.97 }} transition={{ duration:0.4, ease:EASE }}>
            <ScreenContent id={id} />
          </motion.div>
        </AnimatePresence>
      </PhoneFrame>
      <div style={{ display:'flex', flexDirection:'column', alignItems:'center', gap:'8px' }}>
        <AnimatePresence mode="wait">
          <motion.p key={id} initial={{ opacity:0, y:4 }} animate={{ opacity:1, y:0 }} exit={{ opacity:0, y:-4 }} transition={{ duration:0.22 }}
            style={{ fontSize:'11px', fontWeight:'600', color:'rgba(255,255,255,0.3)', letterSpacing:'0.1em', textTransform:'uppercase' }}>
            {SCREEN_META[id].label}
          </motion.p>
        </AnimatePresence>
        <div style={{ display:'flex', gap:'6px' }}>
          {SCREEN_IDS.map((_, i) => (
            <button key={i} onClick={() => setIdx(i)}
              style={{ height:'4px', width:i===idx?'22px':'4px', borderRadius:'2px', background:i===idx?BLUE:'rgba(255,255,255,0.18)', transition:'all 0.3s', cursor:'pointer', border:'none', padding:0 }} />
          ))}
        </div>
      </div>
    </div>
  )
}

// ── Hero ──────────────────────────────────────────────────────────────────────
function Hero() {
  return (
    <section style={{ position:'relative', overflow:'hidden', background:BG, minHeight:'100svh' }}>
      {/* Dot grid */}
      <div style={{ position:'absolute', inset:0, pointerEvents:'none', backgroundImage:'radial-gradient(rgba(255,255,255,0.04) 1px,transparent 1px)', backgroundSize:'28px 28px', maskImage:'radial-gradient(ellipse 80% 80% at 50% 40%,black,transparent)' }} />
      {/* Warm orb — echoes the app's dashboard gradient */}
      <motion.div animate={{ x:[0,20,-8,0], y:[0,-16,10,0], scale:[1,1.1,0.94,1] }} transition={{ duration:24, repeat:Infinity, ease:'easeInOut' }}
        style={{ position:'absolute', pointerEvents:'none', top:'-18%', left:'-6%', width:'65vw', height:'65vw', background:'radial-gradient(circle,rgba(194,65,12,0.18) 0%,rgba(157,23,77,0.12) 40%,transparent 65%)', filter:'blur(90px)' }} />
      <motion.div animate={{ x:[0,-16,14,0], y:[0,18,-8,0], scale:[1,0.92,1.08,1] }} transition={{ duration:30, repeat:Infinity, ease:'easeInOut', delay:6 }}
        style={{ position:'absolute', pointerEvents:'none', bottom:'-16%', right:'-4%', width:'50vw', height:'50vw', background:'radial-gradient(circle,rgba(79,70,229,0.15) 0%,transparent 60%)', filter:'blur(80px)' }} />

      <div className="relative z-10 max-w-[1300px] mx-auto px-5 lg:px-8 flex flex-col lg:flex-row items-center justify-between gap-10 lg:gap-14"
        style={{ minHeight:'100svh', paddingTop:'96px', paddingBottom:'60px' }}>

        {/* Left copy — text first in DOM (shows above phone on mobile) */}
        <div className="flex-1 max-w-[600px] text-center lg:text-left">
          <motion.div initial={{ opacity:0, y:8 }} animate={{ opacity:1, y:0 }} transition={{ duration:0.4 }}
            className="inline-flex items-center gap-2 mb-8 px-3.5 py-1.5 rounded-full"
            style={{ border:`1px solid ${BLUE}40`, background:`${BLUE}10` }}>
            <motion.span animate={{ opacity:[1,0.35,1] }} transition={{ duration:2, repeat:Infinity }}
              style={{ width:'6px', height:'6px', borderRadius:'50%', background:GREEN_L, display:'inline-block' }} />
            <span style={{ fontSize:'11px', fontWeight:'700', color:BLUE_L, letterSpacing:'0.12em', textTransform:'uppercase' }}>
              Free · No app download · Works worldwide
            </span>
          </motion.div>

          <motion.h1 initial={{ opacity:0, y:32 }} animate={{ opacity:1, y:0 }} transition={{ duration:0.85, delay:0.1, ease:EASE }}
            className="text-white font-black leading-none mb-6"
            style={{ fontSize:'clamp(3rem,6.5vw,5.75rem)', letterSpacing:'-0.04em', fontFamily:'var(--font-inter)' }}>
            Run your flat.<br />
            <span style={{ background:'linear-gradient(120deg,#FB923C 0%,#EC4899 45%,#60A5FA 100%)', WebkitBackgroundClip:'text', WebkitTextFillColor:'transparent', backgroundClip:'text' }}>
              Without the drama.
            </span>
          </motion.h1>

          <motion.p initial={{ opacity:0, y:20 }} animate={{ opacity:1, y:0 }} transition={{ duration:0.75, delay:0.22, ease:EASE }}
            className="leading-relaxed mb-10 max-w-[480px] mx-auto lg:mx-0"
            style={{ fontSize:'1.125rem', color:'rgba(255,255,255,0.42)', fontFamily:'var(--font-inter)' }}>
            Duties rotate automatically. Bills split fairly. Swaps handled in-app.
            Your flat manages itself — you just live in it.
          </motion.p>

          <motion.div initial={{ opacity:0, y:12 }} animate={{ opacity:1, y:0 }} transition={{ duration:0.65, delay:0.3, ease:EASE }}
            className="flex flex-row gap-2 sm:gap-3 justify-center lg:justify-start mb-10">
            <motion.a href="#get-started" whileHover={{ scale:1.03 }} whileTap={{ scale:0.97 }}
              className="flex items-center justify-center gap-2.5 font-bold text-sm text-white h-12 px-7 rounded-2xl cursor-pointer"
              style={{ background:BLUE, boxShadow:`0 0 0 1px rgba(37,99,235,0.55),0 12px 32px rgba(37,99,235,0.42)` }}>
              Get Started Free <ArrowRight size={14} />
            </motion.a>
            <motion.a href="#features" whileHover={{ scale:1.02 }} whileTap={{ scale:0.97 }}
              className="flex items-center justify-center gap-2 font-medium text-sm h-12 px-6 rounded-2xl border cursor-pointer"
              style={{ borderColor:BORDER, color:'rgba(255,255,255,0.45)' }}>
              See how it works
            </motion.a>
          </motion.div>

          <motion.div initial={{ opacity:0 }} animate={{ opacity:1 }} transition={{ duration:0.6, delay:0.48 }}
            className="flex items-center gap-4 justify-center lg:justify-start">
            <div style={{ display:'flex' }}>
              {['#C2410C','#7C3AED','#059669','#DB2777','#1D4ED8'].map((c,i) => (
                <div key={i} style={{ width:'34px', height:'34px', borderRadius:'50%', background:c, border:`2px solid ${BG}`, display:'flex', alignItems:'center', justifyContent:'center', fontSize:'11px', fontWeight:'700', color:'white', marginLeft: i>0 ? '-8px' : '0' }}>
                  {['S','M','P','A','R'][i]}
                </div>
              ))}
            </div>
            <div>
              <div style={{ display:'flex', gap:'2px', marginBottom:'4px' }}>
                {[...Array(5)].map((_,j) => <Star key={j} size={11} fill={BLUE} style={{ color:BLUE }} />)}
              </div>
              <p style={{ fontSize:'11px', color:'rgba(255,255,255,0.28)' }}>Trusted by 20+ flats worldwide</p>
            </div>
          </motion.div>
        </div>

        {/* Phone — second in DOM (below copy on mobile, right on desktop) */}
        <motion.div initial={{ opacity:0, y:48, scale:0.94 }} animate={{ opacity:1, y:0, scale:1 }}
          transition={{ duration:1.1, delay:0.28, ease:EASE }}
          className="w-full lg:flex-1 flex justify-center lg:justify-end">
          <CyclingPhone />
        </motion.div>
      </div>

      {/* Scroll cue */}
      <motion.div animate={{ opacity:[0.25,0.7,0.25], y:[0,7,0] }} transition={{ duration:2.4, repeat:Infinity }}
        style={{ position:'absolute', bottom:'32px', left:'50%', transform:'translateX(-50%)', display:'flex', flexDirection:'column', alignItems:'center', gap:'4px', pointerEvents:'none' }}>
        <div style={{ width:'1px', height:'40px', background:`linear-gradient(to bottom,${BLUE}90,transparent)` }} />
        <div style={{ width:'4px', height:'4px', borderRadius:'50%', background:BLUE_L }} />
      </motion.div>
    </section>
  )
}

// ── Marquee ───────────────────────────────────────────────────────────────────
const MARQUEE_ITEMS = ['Duty Rotation','Expense Splitting','Real-Time Sync','Swap Requests','Fair Rotation','Audit Trail','Bill Splitting','Settlement Tracking','Auto-Assignment','Reliability Scores']

function Marquee() {
  const all = [...MARQUEE_ITEMS, ...MARQUEE_ITEMS]
  return (
    <div style={{ overflow:'hidden', padding:'14px 0', borderTop:'1px solid rgba(255,255,255,0.05)', borderBottom:'1px solid rgba(255,255,255,0.05)', background:'rgba(255,255,255,0.008)' }}>
      <div style={{ display:'flex', width:'max-content', animation:'mq 38s linear infinite' }}>
        {all.map((t,i) => (
          <div key={i} style={{ display:'flex', alignItems:'center', gap:'16px', flexShrink:0, padding:'0 24px' }}>
            <div style={{ width:'3px', height:'3px', borderRadius:'50%', background:BLUE, opacity:0.55 }} />
            <span style={{ fontSize:'11px', fontWeight:'600', letterSpacing:'0.14em', textTransform:'uppercase', color:'rgba(255,255,255,0.18)', whiteSpace:'nowrap' }}>{t}</span>
          </div>
        ))}
      </div>
    </div>
  )
}

// ── Feature chapters ──────────────────────────────────────────────────────────
interface ChapterProps {
  badge: string; badgeColor: string
  headline: string; body: string; proof: string
  glow: string; screen: ReactNode; flip?: boolean
}

function Chapter({ badge, badgeColor, headline, body, proof, glow, screen, flip=false }: ChapterProps) {
  return (
    <section className="py-16 lg:py-32 px-5 lg:px-8" style={{ position:'relative', overflow:'hidden', background:SURFACE }}>
      {/* Ambient glow */}
      <div style={{ position:'absolute', pointerEvents:'none', [flip?'right':'left']:'-8%', top:'50%', transform:'translateY(-50%)', width:'60vw', height:'60vw', maxWidth:'680px', maxHeight:'680px', background:`radial-gradient(circle,${glow} 0%,transparent 62%)`, filter:'blur(90px)' }} />

      <div style={{ position:'relative', zIndex:1, maxWidth:'1280px', margin:'0 auto' }}>
        {/* Mobile: phone above text. Desktop: side-by-side (with flip) */}
        <div className={`flex flex-col ${flip?'lg:flex-row-reverse':'lg:flex-row'} items-center gap-10 lg:gap-28`}>

          {/* Phone — comes first in DOM so it renders on TOP on mobile */}
          <motion.div
            className={`flex justify-center w-full lg:flex-1 order-first lg:order-none`}
            initial={{ opacity:0, y:24, scale:0.96 }} whileInView={{ opacity:1, y:0, scale:1 }}
            viewport={{ once:true, margin:'-80px' }} transition={{ duration:0.9, ease:EASE }}>
            <PhoneFrame glow={glow}>{screen}</PhoneFrame>
          </motion.div>

          {/* Copy */}
          <motion.div
            className="flex-1 max-w-[540px] text-center lg:text-left w-full"
            initial={{ opacity:0, x:flip?40:-40 }} whileInView={{ opacity:1, x:0 }}
            viewport={{ once:true, margin:'-80px' }} transition={{ duration:0.85, ease:EASE, delay:0.08 }}>
            <span style={{ display:'inline-block', marginBottom:'16px', fontSize:'11px', fontWeight:'700', letterSpacing:'0.14em', textTransform:'uppercase', padding:'5px 13px', borderRadius:'9999px', color:badgeColor, background:`${badgeColor}15`, border:`1px solid ${badgeColor}30` }}>{badge}</span>
            <h2 className="text-white font-black leading-[1.02] mb-4"
              style={{ fontSize:'clamp(1.75rem,4vw,3.4rem)', letterSpacing:'-0.035em', fontFamily:'var(--font-inter)' }}>{headline}</h2>
            <p className="leading-relaxed mb-6" style={{ fontSize:'clamp(0.9rem,1.8vw,1.05rem)', color:'rgba(255,255,255,0.4)', fontFamily:'var(--font-inter)' }}>{body}</p>
            <div style={{ display:'flex', alignItems:'flex-start', gap:'10px', borderRadius:'14px', padding:'13px', background:`${badgeColor}0a`, border:`1px solid ${badgeColor}1c`, textAlign:'left' }}>
              <div style={{ width:'20px', height:'20px', borderRadius:'50%', background:`${badgeColor}22`, display:'flex', alignItems:'center', justifyContent:'center', flexShrink:0, marginTop:'1px' }}>
                <Star size={10} fill={badgeColor} style={{ color:badgeColor }} />
              </div>
              <p style={{ fontSize:'clamp(12px,1.5vw,13px)', color:'rgba(255,255,255,0.52)', lineHeight:1.65 }}>{proof}</p>
            </div>
          </motion.div>
        </div>
      </div>
    </section>
  )
}

function Features() {
  return (
    <div id="features">
      <Chapter badge="Duty Rotation" badgeColor={BLUE}
        headline="The rota that runs itself."
        body="Define every task once. Set the queue. Habitiq assigns the next person the moment one cycle ends — no calendar, no group chat, no nagging required."
        proof='"Set up Garbage rotation in 2 minutes. MADKING was auto-assigned. Done. — Ayyapa Nilayam flat, Bengaluru"'
        glow="rgba(37,99,235,0.2)" screen={<DashboardScreen />} flip={false} />
      <Chapter badge="Expense Splitting" badgeColor="#059669"
        headline="Every rupee, accounted for."
        body="Log any shared expense. Habitiq splits it, tracks who paid, who owes, and how much. The balance updates in real time — visible to everyone in the flat."
        proof='"Sundy food ₹5,000 split 8 ways. No debate. Everyone could see it instantly. — Jaswanth EVS, Hyderabad"'
        glow="rgba(5,150,105,0.2)" screen={<ExpensesScreen />} flip={true} />
      <Chapter badge="Swap Requests" badgeColor={PURPLE}
        headline="Life happens. Cover yourself in two taps."
        body="Can't do your task? Send a swap request. Your flatmate gets notified, accepts or declines in-app. The system logs it officially. No WhatsApp thread needed."
        proof='"I had a family event. Sent a swap, MADKING accepted. No awkwardness. Recorded. Done. — Edupuganti Venkata, Pune"'
        glow="rgba(124,58,237,0.2)" screen={<SwapsScreen />} flip={false} />
    </div>
  )
}

// ── "The problem" callout — narrative, not a compare table ───────────────────
function ProblemCallout() {
  return (
    <section className="py-16 lg:py-24 px-5 lg:px-8" style={{ background:BG }}>
      <div style={{ maxWidth:'860px', margin:'0 auto' }}>
        <motion.div initial={{ opacity:0, y:24 }} whileInView={{ opacity:1, y:0 }}
          viewport={{ once:true, margin:'-80px' }} transition={{ duration:0.85, ease:EASE }}>
          <p style={{ marginBottom:'32px', fontWeight:'700', fontSize:'11px', color:'rgba(255,255,255,0.2)', letterSpacing:'0.14em', textTransform:'uppercase' }}>Why not just use WhatsApp?</p>
          <div style={{ display:'flex', flexDirection:'column', gap:'20px', marginBottom:'48px' }}>
            {[
              { text:'WhatsApp has 20 people in the chat. Nobody actually does the dishes.', strike:true  },
              { text:'Splitwise tracks the bill. Nobody actually pays it.',                  strike:true  },
              { text:'Habitiq assigns, rotates, tracks, and settles — automatically.',       strike:false },
            ].map((line,i) => (
              <p key={i} className="font-black leading-tight"
                style={{ fontSize:'clamp(1.1rem,2.8vw,2.2rem)', letterSpacing:'-0.025em', fontFamily:'var(--font-inter)', color:line.strike?'rgba(255,255,255,0.15)':'white', textDecoration:line.strike?'line-through':'none' }}>
                {line.text}
              </p>
            ))}
          </div>
        </motion.div>
        <motion.div
          className="grid grid-cols-1 sm:grid-cols-3 gap-8 sm:gap-6"
          initial={{ opacity:0, y:20 }} whileInView={{ opacity:1, y:0 }}
          viewport={{ once:true, margin:'-60px' }} transition={{ duration:0.75, ease:EASE, delay:0.1 }}>
          {[
            { n:'200M+',   label:'People living in shared accommodation worldwide' },
            { n:'< 2 min', label:'To set up your flat and invite flatmates'  },
            { n:'₹0',      label:'Cost during trial — no card required'        },
          ].map(({ n, label }) => (
            <div key={n} className="flex flex-col sm:block items-start sm:text-center border-l-2 sm:border-l-0 sm:border-t-2 pl-5 pt-0 sm:pl-0 sm:pt-5"
              style={{ borderColor:'rgba(37,99,235,0.3)' }}>
              <p style={{ fontWeight:'900', color:'white', lineHeight:1, marginBottom:'6px', fontSize:'clamp(2rem,4vw,2.75rem)', letterSpacing:'-0.04em' }}>{n}</p>
              <p style={{ fontSize:'13px', color:'rgba(255,255,255,0.3)', lineHeight:1.55 }}>{label}</p>
            </div>
          ))}
        </motion.div>
      </div>
    </section>
  )
}

// ── Testimonials ───────────────────────────────────────────────────────────────
const TESTIMONIALS: Testimonial[] = [
  { id:1, quote:"Nobody argues about chores anymore. The rotation just works. Whoever is next, does it — everyone can see it. No WhatsApp fights.", name:'Ayyapa Nilayam', username:'8 members · Koramangala, Bengaluru', avatar:'', avatarColor:'#1D4ED8', avatarInitial:'A' },
  { id:2, quote:"The expense splitting saved our friendships. ₹4,375 tracked to the rupee. Everyone paid on time for the first time ever.", name:'Jaswanth EVS', username:'Flat of 8 · Hyderabad', avatar:'', avatarColor:'#059669', avatarInitial:'J' },
  { id:3, quote:"I sent a swap request, my flatmate accepted in 2 minutes. No follow-up, no phone calls. The system just records it and moves on.", name:'Edupuganti Venkata', username:'Admin · sai room, Pune', avatar:'', avatarColor:PURPLE, avatarInitial:'E' },
  { id:4, quote:"I've used Splitwise for 3 years. Habitiq does in one screen what Splitwise needs 5 steps for. And the duty rotation is something Splitwise can't do at all.", name:'Bhanu Kalyan', username:'Admin · 4BHK · Bengaluru', avatar:'', avatarColor:'#DB2777', avatarInitial:'B' },
  { id:5, quote:"We set up the flat in under 3 minutes. All 8 members joined from their phone without downloading an app. Just a link.", name:'Sri Ram Murthy', username:'Flat of 8 · Hyderabad', avatar:'', avatarColor:'#D97706', avatarInitial:'S' },
]

function Testimonials() {
  return (
    <section className="py-14 lg:py-20" style={{ position:'relative', overflow:'hidden', background:SURFACE }}>
      <div style={{ position:'absolute', inset:0, pointerEvents:'none', background:'radial-gradient(ellipse 60% 60% at 50% 50%,rgba(124,58,237,0.12) 0%,transparent 75%)' }} />
      <div style={{ position:'relative', zIndex:1 }}>
        <motion.div className="px-5 lg:px-8" style={{ textAlign:'center', marginBottom:'40px' }}
          initial={{ opacity:0, y:20 }} whileInView={{ opacity:1, y:0 }}
          viewport={{ once:true, margin:'-80px' }} transition={{ duration:0.7, ease:EASE }}>
          <span style={{ display:'inline-block', marginBottom:'16px', fontSize:'11px', fontWeight:'700', letterSpacing:'0.14em', textTransform:'uppercase', padding:'6px 14px', borderRadius:'9999px', color:'rgba(255,255,255,0.25)', background:'rgba(255,255,255,0.04)', border:`1px solid ${BORDER}` }}>Real users</span>
          <h2 className="font-black text-white" style={{ fontSize:'clamp(1.8rem,3.5vw,2.5rem)', letterSpacing:'-0.035em', fontFamily:'var(--font-inter)' }}>Real flats. Real results.</h2>
          <p style={{ marginTop:'12px', fontSize:'14px', color:'rgba(255,255,255,0.3)' }}>From Bengaluru to London to Sydney — people who stopped arguing.</p>
        </motion.div>
        <TestimonialSlider testimonials={TESTIMONIALS} className="bg-transparent" />
      </div>
    </section>
  )
}

// ── FAQ ────────────────────────────────────────────────────────────────────────
const FAQS = [
  { q:'Is Habitiq really free?', a:"Yes — completely. No credit card required. All features: duty rotation, expense splitting, bill tracking, swap requests, audit log — all free during our trial phase. We'll give you plenty of notice before anything changes." },
  { q:'How is Habitiq different from Splitwise?', a:"Splitwise only tracks money. Habitiq manages the flat. Duty rotation, automated assignment, swap request system, monthly fixed bills, and settlement tracking — Splitwise does one of these. Habitiq does all five, in one place." },
  { q:'How does the rotation actually work?', a:"You define tasks (cleaning, cooking, trash), add flatmates to the queue, and set the frequency. Habitiq auto-assigns the next person when a task is completed or the cycle resets. Members can send swap requests if they're unavailable — all logged officially." },
  { q:"Does my flatmate need to install an app?", a:"No. Habitiq is a PWA — it works in the browser. Flatmates open a link, sign in with Google, and they're in. They can add it to their home screen for a native feel. No App Store. No Play Store. Just a link." },
  { q:'Which cities is Habitiq used in?', a:"Habitiq is used worldwide — Bengaluru, Hyderabad, Pune, Mumbai, Delhi, and Chennai in India; London and Manchester in the UK; Sydney and Melbourne in Australia; Singapore, Toronto, and beyond. Any shared living setup works anywhere — flats, PGs, house shares, co-living, and student accommodation." },
  { q:'Does Habitiq work outside India?', a:"Yes — Habitiq works for any shared flat, house share, or co-living space in any country. The chore rotation, bill splitting, and swap request system work the same whether you're in London, Sydney, New York, Singapore, or Bengaluru. No country restrictions, no local setup needed." },
]

function FAQ() {
  const [open, setOpen] = useState<number | null>(null)
  return (
    <section id="faq" className="py-16 lg:py-24 px-5 lg:px-8" style={{ background:BG }}>
      <div style={{ maxWidth:'720px', margin:'0 auto' }}>
        <motion.div style={{ marginBottom:'44px', textAlign:'center' }}
          initial={{ opacity:0, y:20 }} whileInView={{ opacity:1, y:0 }}
          viewport={{ once:true, margin:'-80px' }} transition={{ duration:0.7, ease:EASE }}>
          <span style={{ display:'inline-block', marginBottom:'16px', fontSize:'11px', fontWeight:'700', letterSpacing:'0.14em', textTransform:'uppercase', padding:'6px 14px', borderRadius:'9999px', color:BLUE_L, background:`${BLUE}12`, border:`1px solid ${BLUE}28` }}>Questions</span>
          <h2 className="font-black text-white" style={{ fontSize:'clamp(1.8rem,3.5vw,2.75rem)', letterSpacing:'-0.035em', fontFamily:'var(--font-inter)' }}>Answers, upfront.</h2>
        </motion.div>
        <div style={{ display:'flex', flexDirection:'column', gap:'8px' }}>
          {FAQS.map((faq, i) => (
            <motion.div key={i} initial={{ opacity:0, y:14 }} whileInView={{ opacity:1, y:0 }}
              viewport={{ once:true, margin:'-40px' }} transition={{ duration:0.55, ease:EASE, delay:i*0.05 }}
              style={{ borderRadius:'16px', overflow:'hidden', border:`1px solid ${BORDER}`, background:'rgba(255,255,255,0.018)' }}>
              <button onClick={() => setOpen(open===i?null:i)}
                style={{ width:'100%', display:'flex', alignItems:'center', justifyContent:'space-between', gap:'16px', padding:'20px 24px', textAlign:'left', cursor:'pointer', background:'transparent', border:'none' }}
                className="hover:bg-white/[0.025] transition-colors">
                <span style={{ fontSize:'14px', fontWeight:'600', lineHeight:1.4, color:'rgba(255,255,255,0.82)' }}>{faq.q}</span>
                <motion.div animate={{ rotate:open===i?45:0 }} transition={{ duration:0.2 }}
                  style={{ width:'20px', height:'20px', borderRadius:'50%', border:`1px solid ${BORDER}`, display:'flex', alignItems:'center', justifyContent:'center', flexShrink:0 }}>
                  <svg width="9" height="9" viewBox="0 0 9 9" fill="none">
                    <path d="M4.5 1v7M1 4.5h7" stroke="rgba(255,255,255,0.38)" strokeWidth="1.4" strokeLinecap="round"/>
                  </svg>
                </motion.div>
              </button>
              <AnimatePresence>
                {open===i && (
                  <motion.div initial={{ height:0, opacity:0 }} animate={{ height:'auto', opacity:1 }} exit={{ height:0, opacity:0 }}
                    transition={{ duration:0.24, ease:EASE }} style={{ overflow:'hidden' }}>
                    <p style={{ padding:'0 24px 20px', fontSize:'14px', lineHeight:1.7, color:'rgba(255,255,255,0.42)' }}>{faq.a}</p>
                  </motion.div>
                )}
              </AnimatePresence>
            </motion.div>
          ))}
        </div>
      </div>
    </section>
  )
}

// ── CTA ────────────────────────────────────────────────────────────────────────
function GetStarted() {
  return (
    <section id="get-started" className="py-16 lg:py-24 px-5 lg:px-8" style={{ position:'relative', overflow:'hidden', background:SURFACE }}>
      <motion.div animate={{ scale:[1,1.1,1], opacity:[0.35,0.6,0.35] }} transition={{ duration:8, repeat:Infinity, ease:'easeInOut' }}
        style={{ position:'absolute', pointerEvents:'none', top:'-40%', left:'50%', transform:'translateX(-50%)', width:'700px', height:'700px', background:`radial-gradient(circle,rgba(37,99,235,0.18) 0%,transparent 60%)`, filter:'blur(60px)' }} />
      <div style={{ position:'relative', zIndex:1, maxWidth:'1120px', margin:'0 auto' }} className="grid lg:grid-cols-2 gap-10 lg:gap-16 items-start">
        <motion.div initial={{ opacity:0, x:-40 }} whileInView={{ opacity:1, x:0 }}
          viewport={{ once:true, margin:'-80px' }} transition={{ duration:0.85, ease:EASE }}>
          <span style={{ display:'inline-block', marginBottom:'20px', fontSize:'11px', fontWeight:'700', letterSpacing:'0.14em', textTransform:'uppercase', padding:'6px 14px', borderRadius:'9999px', color:BLUE_L, background:`${BLUE}12`, border:`1px solid ${BLUE}28` }}>Start free</span>
          <h2 className="font-black text-white leading-[1.02] mb-5"
            style={{ fontSize:'clamp(2rem,4vw,3.25rem)', letterSpacing:'-0.035em', fontFamily:'var(--font-inter)' }}>
            Your flat deserves<br />a proper system.
          </h2>
          <p style={{ fontSize:'16px', marginBottom:'40px', lineHeight:1.7, maxWidth:'360px', color:'rgba(255,255,255,0.36)', fontFamily:'var(--font-inter)' }}>
            Create your flat or join your flatmates. No payment, no install, no friction — under 2 minutes.
          </p>
          <ul style={{ display:'flex', flexDirection:'column', gap:'16px' }}>
            {[
              'Duty rotation runs automatically — no daily management',
              'Expenses split to the rupee across all flatmates',
              'Swap requests handled officially, in-app',
              'Works on any phone — no app download needed',
            ].map(item => (
              <li key={item} style={{ display:'flex', alignItems:'center', gap:'12px' }}>
                <div style={{ width:'20px', height:'20px', borderRadius:'50%', background:`${BLUE}20`, border:`1px solid ${BLUE}38`, display:'flex', alignItems:'center', justifyContent:'center', flexShrink:0 }}>
                  <Check size={9} style={{ color:BLUE }} />
                </div>
                <span style={{ fontSize:'14px', color:'rgba(255,255,255,0.44)' }}>{item}</span>
              </li>
            ))}
          </ul>
        </motion.div>
        <motion.div initial={{ opacity:0, x:40 }} whileInView={{ opacity:1, x:0 }}
          viewport={{ once:true, margin:'-80px' }} transition={{ duration:0.85, ease:EASE, delay:0.1 }}>
          <div style={{ borderRadius:'16px', overflow:'hidden', border:`1px solid ${BORDER}`, background:'rgba(255,255,255,0.02)', boxShadow:`0 0 0 1px ${BLUE}18,0 40px 100px rgba(0,0,0,0.6)` }}>
            <div style={{ padding:'24px 28px 12px', borderBottom:`1px solid ${BORDER}` }}>
              <p style={{ fontSize:'16px', fontWeight:'700', color:'white' }}>Create your account</p>
              <p style={{ fontSize:'14px', marginTop:'2px', color:'rgba(255,255,255,0.28)' }}>Your flat is 2 minutes away.</p>
            </div>
            <AuthForm inline />
          </div>
        </motion.div>
      </div>
    </section>
  )
}

// ── Footer ─────────────────────────────────────────────────────────────────────
function Footer() {
  return (
    <footer className="py-12 lg:py-14 px-5 lg:px-8" style={{ borderTop:'1px solid rgba(255,255,255,0.05)', background:BG }}>
      <div style={{ maxWidth:'1120px', margin:'0 auto' }}>
        <div className="grid sm:grid-cols-2 lg:grid-cols-4 gap-10 mb-14">
          <div className="lg:col-span-2">
            <img src="/habitiq-logo.svg" alt="Habitiq" className="h-7 w-auto brightness-0 invert mb-4" />
            <p style={{ fontSize:'14px', lineHeight:1.6, maxWidth:'280px', color:'rgba(255,255,255,0.22)' }}>
              The operating system for shared flats. Duties rotate. Bills split. Flatmates stop arguing.
            </p>
            <div style={{ display:'flex', alignItems:'center', gap:'6px', marginTop:'16px' }}>
              <div style={{ width:'6px', height:'6px', borderRadius:'50%', background:GREEN_L }} />
              <p style={{ fontSize:'11px', color:'rgba(255,255,255,0.2)' }}>All systems live · habitiq.app</p>
            </div>
          </div>
          {[
            { head:'Product', links:[['#features','Features'],['#faq','FAQ'],['#get-started','Get Started']] },
            { head:'Company', links:[['#','About'],['/privacy','Privacy'],['/terms','Terms'],['mailto:hello@habitiq.app','Contact']] },
          ].map(col => (
            <div key={col.head}>
              <p style={{ fontSize:'11px', fontWeight:'700', letterSpacing:'0.12em', textTransform:'uppercase', marginBottom:'16px', color:'rgba(255,255,255,0.28)' }}>{col.head}</p>
              <div style={{ display:'flex', flexDirection:'column', gap:'12px' }}>
                {col.links.map(([href,label]) => (
                  <a key={label} href={href} style={{ fontSize:'14px', color:'rgba(255,255,255,0.22)', textDecoration:'none', transition:'color 150ms' }}
                    onMouseEnter={e=>(e.currentTarget.style.color='rgba(255,255,255,0.55)')}
                    onMouseLeave={e=>(e.currentTarget.style.color='rgba(255,255,255,0.22)')}>
                    {label}
                  </a>
                ))}
              </div>
            </div>
          ))}
        </div>
        <div style={{ borderTop:'1px solid rgba(255,255,255,0.04)', paddingTop:'24px', display:'flex', flexWrap:'wrap', alignItems:'center', justifyContent:'space-between', gap:'12px' }}>
          <p style={{ fontSize:'12px', color:'rgba(255,255,255,0.12)' }}>© 2026 Habitiq. All rights reserved.</p>
          <p style={{ fontSize:'12px', color:'rgba(255,255,255,0.12)' }}>Shared living, managed.</p>
        </div>
      </div>
    </footer>
  )
}

// ── Page ───────────────────────────────────────────────────────────────────────
export default function LandingPage() {
  return (
    <div style={{ minHeight:'100svh', background:BG, fontFamily:'var(--font-inter)', WebkitFontSmoothing:'antialiased' }}>
      <style>{`@keyframes mq { from { transform:translateX(0) } to { transform:translateX(-50%) } }`}</style>
      <Navbar />
      <main>
        <Hero />
        <Marquee />
        <Features />
        <DesktopShowcase />
        <ProblemCallout />
        <Testimonials />
        <FAQ />
        <GetStarted />
      </main>
      <Footer />
    </div>
  )
}
