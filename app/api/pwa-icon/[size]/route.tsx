import { ImageResponse } from 'next/og'

export async function GET(req: Request, { params }: { params: Promise<{ size: string }> }) {
  const { size: sizeStr } = await params
  const size = Math.min(Math.max(parseInt(sizeStr) || 192, 16), 1024)

  // Build absolute URL — required by Satori to fetch raster images
  const url = new URL(req.url)
  const baseUrl = `${url.protocol}//${url.host}`
  const iconUrl = `${baseUrl}/habitiq-icon-mark.png`

  const iconSize = Math.round(size * 0.62)

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
        {/* eslint-disable-next-line @next/next/no-img-element */}
        <img
          src={iconUrl}
          width={iconSize}
          height={iconSize}
          style={{ objectFit: 'contain' }}
        />
      </div>
    ),
    { width: size, height: size }
  )
}
