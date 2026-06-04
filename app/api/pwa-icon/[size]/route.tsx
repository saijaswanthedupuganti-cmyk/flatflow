import { ImageResponse } from 'next/og'

export async function GET(_req: Request, { params }: { params: Promise<{ size: string }> }) {
  const { size: sizeStr } = await params
  const size = Math.min(Math.max(parseInt(sizeStr) || 192, 16), 1024)

  return new ImageResponse(
    (
      <div
        style={{
          width: '100%',
          height: '100%',
          background: 'linear-gradient(135deg, #7c3aed 0%, #4338ca 100%)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
        }}
      >
        <span
          style={{
            color: 'white',
            fontWeight: 800,
            fontSize: `${Math.round(size * 0.5)}px`,
            fontFamily: 'sans-serif',
            letterSpacing: '-0.02em',
          }}
        >
          H
        </span>
      </div>
    ),
    { width: size, height: size }
  )
}
