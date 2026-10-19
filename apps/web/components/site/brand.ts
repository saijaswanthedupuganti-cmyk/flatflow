/**
 * Palette "C — Navy and teal" for renderers that cannot read CSS variables
 * (next/og ImageResponse, the QR code generator). Mirrors components/site/tokens.css;
 * change both together. Everything rendered in the browser uses the CSS tokens instead.
 */
export const BRAND = {
  action: '#0F766E',
  actionBright: '#14B8A6',
  navy: '#1C2A40',
  navyDeep: '#0B1220',
  onNavy: '#FFFFFF',
  onNavyMuted: '#C7D0DE',
  surface: '#FFFFFF',
  canvas: '#F5F7FA',
} as const
