"use client"
import { getMemberColor } from '@/lib/memberColors'

interface MemberAvatarProps {
  uid: string
  nickname: string
  memberUids: string[]
  photoUrl?: string | null
  size?: 'xs' | 'sm' | 'md' | 'lg'
  showRing?: boolean
  className?: string
}

const SIZE_MAP = {
  xs: { outer: 24, inner: 22, text: '9px', ring: 1 },
  sm: { outer: 32, inner: 30, text: '11px', ring: 1.5 },
  md: { outer: 40, inner: 37, text: '14px', ring: 2 },
  lg: { outer: 52, inner: 48, text: '18px', ring: 2.5 },
}

export default function MemberAvatar({
  uid,
  nickname,
  memberUids,
  photoUrl,
  size = 'md',
  showRing = true,
  className = '',
}: MemberAvatarProps) {
  const color = getMemberColor(uid, memberUids)
  const s = SIZE_MAP[size]
  const initials = nickname.slice(0, 2).toUpperCase()

  return (
    <div
      className={`relative inline-flex shrink-0 items-center justify-center rounded-full ${className}`}
      style={{
        width: s.outer,
        height: s.outer,
        boxShadow: showRing ? `0 0 0 ${s.ring}px ${color.hex}, 0 0 0 ${s.ring + 2}px rgba(0,0,0,0.4)` : undefined,
      }}
      title={nickname}
    >
      {photoUrl ? (
        <img
          src={photoUrl}
          alt={nickname}
          className="rounded-full object-cover"
          style={{ width: s.inner, height: s.inner }}
        />
      ) : (
        <div
          className="rounded-full flex items-center justify-center font-bold select-none"
          style={{
            width: s.inner,
            height: s.inner,
            backgroundColor: color.bg,
            color: color.hex,
            fontSize: s.text,
            border: `1px solid ${color.border}`,
          }}
        >
          {initials}
        </div>
      )}
    </div>
  )
}

interface MemberChipProps {
  uid: string
  nickname: string
  memberUids: string[]
  className?: string
}

export function MemberChip({ uid, nickname, memberUids, className = '' }: MemberChipProps) {
  const color = getMemberColor(uid, memberUids)
  return (
    <span
      className={`inline-flex items-center gap-1.5 px-2 py-0.5 rounded-full text-[11px] font-semibold ${className}`}
      style={{ backgroundColor: color.bg, color: color.hex, border: `1px solid ${color.border}` }}
    >
      <span
        className="inline-block rounded-full font-bold text-[9px]"
        style={{ width: 16, height: 16, lineHeight: '16px', textAlign: 'center', backgroundColor: `${color.hex}22`, color: color.hex }}
      >
        {nickname.slice(0, 1).toUpperCase()}
      </span>
      {nickname}
    </span>
  )
}
