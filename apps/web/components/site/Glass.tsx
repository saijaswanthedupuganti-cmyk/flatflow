'use client'
import { useSyncExternalStore } from 'react'
import s from './site.module.css'

type Tier = 'photo' | 'canvas' | 'dark'

// Blur support never changes during a session, so there is nothing to subscribe to.
const subscribe = () => () => {}
const getNoBlur = () => !(CSS.supports('backdrop-filter', 'blur(1px)') || CSS.supports('-webkit-backdrop-filter', 'blur(1px)'))
// Server render and hydration assume blur is supported, as before.
const getServerNoBlur = () => false

export default function Glass({ tier, className = '', as: Tag = 'div', children, ...rest }:
  { tier: Tier; className?: string; as?: 'div' | 'section' | 'article' | 'header' | 'nav' } & React.HTMLAttributes<HTMLElement>) {
  const noBlur = useSyncExternalStore(subscribe, getNoBlur, getServerNoBlur)
  return (
    <Tag data-glass={tier} className={`${s.glass} ${noBlur ? s.noBlur : ''} ${className}`} {...rest}>
      {children}
    </Tag>
  )
}
