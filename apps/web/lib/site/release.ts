import data from '@/public/downloads/version.json'

export type Release = {
  version: string
  versionCode: number
  sizeMb: number
  releasedOn: string
  minAndroid: string
  url: string
}

export const release: Release = data

export function releaseMeta(r: Release = release): string {
  return `Android ${r.minAndroid}+ · ${r.sizeMb} MB · free · v${r.version}`
}
