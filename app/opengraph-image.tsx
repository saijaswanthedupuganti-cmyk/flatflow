import { ImageResponse } from 'next/og'

export const runtime = 'edge'
export const alt = 'Habitiq — Smart Living Management'
export const size = { width: 1200, height: 630 }
export const contentType = 'image/png'

export default function OgImage() {
  return new ImageResponse(
    (
      <div
        style={{
          width: '100%',
          height: '100%',
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          background: '#0a0a0a',
          fontFamily: 'sans-serif',
          position: 'relative',
        }}
      >
        {/* Subtle grid overlay */}
        <div
          style={{
            position: 'absolute',
            inset: 0,
            backgroundImage:
              'radial-gradient(circle at 20% 50%, rgba(124,58,237,0.12) 0%, transparent 60%), radial-gradient(circle at 80% 20%, rgba(99,102,241,0.10) 0%, transparent 50%)',
          }}
        />

        {/* Logo icon */}
        <div
          style={{
            width: 100,
            height: 100,
            borderRadius: 24,
            background: 'linear-gradient(135deg, #7c3aed, #4338ca)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            boxShadow: '0 20px 60px rgba(124,58,237,0.50)',
            marginBottom: 36,
          }}
        >
          <span style={{ fontSize: 52, fontWeight: 900, color: 'white' }}>H</span>
        </div>

        {/* Brand name */}
        <div
          style={{
            fontSize: 80,
            fontWeight: 900,
            color: 'white',
            letterSpacing: '-3px',
            lineHeight: 1,
            marginBottom: 20,
          }}
        >
          Habitiq
        </div>

        {/* Tagline */}
        <div
          style={{
            fontSize: 28,
            color: 'rgba(255,255,255,0.55)',
            fontWeight: 500,
            letterSpacing: '-0.5px',
          }}
        >
          Smart living, managed.
        </div>

        {/* Bottom pill badges */}
        <div
          style={{
            display: 'flex',
            gap: 16,
            marginTop: 48,
          }}
        >
          {['Duty Rotation', 'Real-time Sync', 'Fair & Transparent'].map((label) => (
            <div
              key={label}
              style={{
                background: 'rgba(124,58,237,0.18)',
                border: '1px solid rgba(124,58,237,0.35)',
                borderRadius: 100,
                padding: '10px 22px',
                fontSize: 18,
                color: 'rgba(196,181,253,0.90)',
                fontWeight: 600,
              }}
            >
              {label}
            </div>
          ))}
        </div>

        {/* URL watermark */}
        <div
          style={{
            position: 'absolute',
            bottom: 36,
            fontSize: 18,
            color: 'rgba(255,255,255,0.25)',
            fontWeight: 500,
            letterSpacing: '0.5px',
          }}
        >
          habitiq.app
        </div>
      </div>
    ),
    size,
  )
}
