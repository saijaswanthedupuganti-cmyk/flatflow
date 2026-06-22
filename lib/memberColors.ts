/* Member DNA — 8-color system. Each flatmate gets one permanently. */

export const MEMBER_COLORS = [
  { id: 0, name: 'amber',  hex: '#f59e0b', bg: 'rgba(245,158,11,0.12)',  border: 'rgba(245,158,11,0.50)',  text: '#f59e0b', tailwindBg: 'bg-amber-500',  tailwindText: 'text-amber-500' },
  { id: 1, name: 'teal',   hex: '#14b8a6', bg: 'rgba(20,184,166,0.12)',  border: 'rgba(20,184,166,0.50)',  text: '#14b8a6', tailwindBg: 'bg-teal-500',   tailwindText: 'text-teal-500' },
  { id: 2, name: 'rose',   hex: '#f43f5e', bg: 'rgba(244,63,94,0.12)',   border: 'rgba(244,63,94,0.50)',   text: '#f43f5e', tailwindBg: 'bg-rose-500',   tailwindText: 'text-rose-500' },
  { id: 3, name: 'sky',    hex: '#0ea5e9', bg: 'rgba(14,165,233,0.12)',  border: 'rgba(14,165,233,0.50)',  text: '#0ea5e9', tailwindBg: 'bg-sky-500',    tailwindText: 'text-sky-500' },
  { id: 4, name: 'violet', hex: '#8b5cf6', bg: 'rgba(139,92,246,0.12)', border: 'rgba(139,92,246,0.50)', text: '#8b5cf6', tailwindBg: 'bg-violet-500', tailwindText: 'text-violet-500' },
  { id: 5, name: 'lime',   hex: '#84cc16', bg: 'rgba(132,204,22,0.12)', border: 'rgba(132,204,22,0.50)', text: '#84cc16', tailwindBg: 'bg-lime-500',   tailwindText: 'text-lime-500' },
  { id: 6, name: 'orange', hex: '#f97316', bg: 'rgba(249,115,22,0.12)', border: 'rgba(249,115,22,0.50)', text: '#f97316', tailwindBg: 'bg-orange-500', tailwindText: 'text-orange-500' },
  { id: 7, name: 'cyan',   hex: '#06b6d4', bg: 'rgba(6,182,212,0.12)',  border: 'rgba(6,182,212,0.50)',  text: '#06b6d4', tailwindBg: 'bg-cyan-500',   tailwindText: 'text-cyan-500' },
] as const

export type MemberColor = typeof MEMBER_COLORS[number]

/**
 * Given an ordered list of member UIDs and a target UID,
 * returns the member color for that UID (stable across renders).
 */
export function getMemberColor(uid: string, memberUids: string[]): MemberColor {
  const idx = memberUids.indexOf(uid)
  if (idx === -1) return MEMBER_COLORS[0]
  return MEMBER_COLORS[idx % MEMBER_COLORS.length]
}

/**
 * Given just an index (0–7), returns the color token.
 */
export function getMemberColorByIndex(idx: number): MemberColor {
  return MEMBER_COLORS[Math.abs(idx) % MEMBER_COLORS.length]
}
