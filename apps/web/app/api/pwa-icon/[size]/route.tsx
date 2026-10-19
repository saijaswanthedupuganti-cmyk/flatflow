import { ImageResponse } from 'next/og'
import { BRAND } from '@/components/site/brand'

// Oddroof mark (401x305, transparent) centred on white. 60% width keeps it inside the maskable safe zone.
const MARK_RATIO = 305 / 401

export async function GET(req: Request, { params }: { params: Promise<{ size: string }> }) {
  const { size: sizeStr } = await params
  const size = Math.min(Math.max(parseInt(sizeStr) || 192, 16), 1024)

  const url = new URL(req.url)
  const markUrl = `${url.protocol}//${url.host}/brand/oddroof-mark.png`
  const markWidth = Math.round(size * (size <= 48 ? 0.86 : 0.6))

  return new ImageResponse(
    (
      <div style={{ width: '100%', height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center', background: BRAND.surface }}>
        {/* eslint-disable-next-line @next/next/no-img-element */}
        <img src={markUrl} width={markWidth} height={Math.round(markWidth * MARK_RATIO)} alt="" />
      </div>
    ),
    { width: size, height: size }
  )
}
