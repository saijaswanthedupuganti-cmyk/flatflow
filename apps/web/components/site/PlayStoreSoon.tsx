import s from './site.module.css'

/** Placeholder for the Play Store listing. Not a link: the app is not on Google Play yet. */
export default function PlayStoreSoon({ onDark = false }: { onDark?: boolean }) {
  return (
    <span className={`${s.playSoon} ${onDark ? s.playSoonOnDark : ''}`} aria-disabled="true">
      <svg width="22" height="24" viewBox="0 0 22 24" aria-hidden focusable="false">
        <path d="M1.2 1.1 12.3 12 1.2 22.9c-.4-.2-.7-.7-.7-1.3V2.4c0-.6.3-1.1.7-1.3Z" fill="currentColor" opacity=".55" />
        <path d="M15.8 8.5 12.3 12l3.5 3.5 4.1-2.3c1.2-.7 1.2-1.7 0-2.4l-4.1-2.3Z" fill="currentColor" opacity=".8" />
        <path d="M1.2 1.1c.4-.2.9-.2 1.5.1l13.1 7.3L12.3 12 1.2 1.1Z" fill="currentColor" opacity=".7" />
        <path d="M12.3 12l3.5 3.5-13.1 7.3c-.6.3-1.1.3-1.5.1L12.3 12Z" fill="currentColor" opacity=".9" />
      </svg>
      <span><small>Coming soon on</small><b>Google Play</b></span>
    </span>
  )
}
