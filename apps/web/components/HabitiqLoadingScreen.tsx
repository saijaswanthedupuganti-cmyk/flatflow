import styles from './HabitiqLoadingScreen.module.css'
import Image from 'next/image'

export default function HabitiqLoadingScreen() {
  return (
    <div className={styles.screen} role="status" aria-live="polite" aria-atomic="true">
      <div className={styles.content}>
        <div className={styles.mark} aria-hidden="true">
          <Image src="/habitiq-app-mark.png" width={72} height={71} alt="" priority />
        </div>
        <div className={styles.copy}>
          <p className={styles.name}>Habitiq</p>
          <p className={styles.message}>Getting your flat ready…</p>
        </div>
        <div className={styles.track} aria-hidden="true">
          <span className={styles.indicator} />
        </div>
      </div>
    </div>
  )
}
