import { ImageResponse } from 'next/og'

export async function GET(req: Request, { params }: { params: Promise<{ size: string }> }) {
  const { size: sizeStr } = await params
  const size = Math.min(Math.max(parseInt(sizeStr) || 192, 16), 1024)

  const url = new URL(req.url)
  const baseUrl = `${url.protocol}//${url.host}`
  const iconUrl = `${baseUrl}/habitiq-icon.png`

  return new ImageResponse(
    (
      // eslint-disable-next-line @next/next/no-img-element
      <img
        src={iconUrl}
        width={size}
        height={size}
        style={{ objectFit: 'cover' }}
        alt=""
      />
    ),
    { width: size, height: size }
  )
}
