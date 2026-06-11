import { ImageResponse } from 'next/og'

export async function GET(_req: Request, { params }: { params: Promise<{ size: string }> }) {
  const { size: sizeStr } = await params
  const size = Math.min(Math.max(parseInt(sizeStr) || 192, 16), 1024)

  const r = Math.round(size * 0.22)
  return new ImageResponse(
    (
      <div
        style={{
          width: '100%',
          height: '100%',
          background: 'linear-gradient(135deg, #7c3aed 0%, #4338ca 60%, #1d4ed8 100%)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          borderRadius: r,
        }}
      >
        <span
          style={{
            color: 'white',
            fontWeight: 900,
            fontSize: `${Math.round(size * 0.52)}px`,
            fontFamily: 'sans-serif',
            letterSpacing: '-0.04em',
            lineHeight: 1,
          }}
        >
          H
        </span>
      </div>
    ),
    { width: size, height: size }
  )
}
