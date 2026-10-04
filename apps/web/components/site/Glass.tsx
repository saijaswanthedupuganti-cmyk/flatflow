'use client'
import { useEffect, useState } from 'react'
import s from './site.module.css'

type Tier = 'photo' | 'canvas' | 'dark'

export default function Glass({ tier, className = '', as: Tag = 'div', children, ...rest }:
  { tier: Tier; className?: string; as?: 'div' | 'section' | 'article' | 'header' | 'nav' } & React.HTMLAttributes<HTMLElement>) {
  const [noBlur, setNoBlur] = useState(false)
  useEffect(() => {
    setNoBlur(!(CSS.supports('backdrop-filter', 'blur(1px)') || CSS.supports('-webkit-backdrop-filter', 'blur(1px)')))
  }, [])
  return (
    <Tag data-glass={tier} className={`${s.glass} ${noBlur ? s.noBlur : ''} ${className}`} {...rest}>
      {children}
    </Tag>
  )
}
