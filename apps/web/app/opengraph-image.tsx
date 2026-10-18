import { readFile } from 'node:fs/promises'
import { join } from 'node:path'
import { ImageResponse } from 'next/og'

export const alt = 'Oddroof — Your flat, sorted'
export const size = { width: 1200, height: 630 }
export const contentType = 'image/png'

const NAVY = '#1C2A40'
const BLUE = '#2563EB'
const TEAL = '#14B8A6'

export default async function OgImage() {
  const mark = await readFile(join(process.cwd(), 'public', 'brand', 'oddroof-mark.png'))
  const markSrc = `data:image/png;base64,${mark.toString('base64')}`

  return new ImageResponse(
    (
      <div
        style={{
          width: '100%',
          height: '100%',
          display: 'flex',
          flexDirection: 'column',
          justifyContent: 'center',
          padding: '0 96px',
          background: NAVY,
          backgroundImage: `radial-gradient(circle at 85% 15%, rgba(37,99,235,0.45) 0%, transparent 55%), radial-gradient(circle at 10% 95%, rgba(20,184,166,0.22) 0%, transparent 50%)`,
          fontFamily: 'sans-serif',
          position: 'relative',
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: 32 }}>
          <div
            style={{
              width: 128,
              height: 128,
              borderRadius: 32,
              background: '#FFFFFF',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              boxShadow: '0 24px 60px rgba(0,0,0,0.35)',
            }}
          >
            {/* eslint-disable-next-line @next/next/no-img-element */}
            <img src={markSrc} width={96} height={73} alt="" />
          </div>
          <div style={{ fontSize: 112, fontWeight: 800, color: '#FFFFFF', letterSpacing: '-4px', lineHeight: 1 }}>
            Oddroof
          </div>
        </div>

        <div style={{ fontSize: 52, fontWeight: 700, color: '#FFFFFF', marginTop: 48, letterSpacing: '-1px' }}>
          Your flat, sorted.
        </div>
        <div style={{ fontSize: 28, color: '#C9D4E8', marginTop: 16, maxWidth: 900, lineHeight: 1.35 }}>
          Fair tasks, shared expenses and bills, and safe flat and flatmate discovery.
        </div>

        <div style={{ position: 'absolute', left: 96, right: 96, bottom: 56, display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <div style={{ display: 'flex', gap: 12 }}>
            <div style={{ width: 48, height: 8, borderRadius: 8, background: BLUE }} />
            <div style={{ width: 24, height: 8, borderRadius: 8, background: TEAL }} />
          </div>
          <div style={{ fontSize: 26, color: '#C9D4E8', fontWeight: 600 }}>habitiq.app · Free on Android</div>
        </div>
      </div>
    ),
    size,
  )
}
