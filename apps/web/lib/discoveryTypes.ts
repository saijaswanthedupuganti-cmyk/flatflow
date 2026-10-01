export interface VacancyListing {
  active: boolean
  bedsAvailable: number
  rentPerHead: number | null
  currency: string
  city: string
  area: string
  existingMembersGender: string | null
  preferredGender: 'any' | 'women' | 'men' | string
  lifestyle: string[]
  customTags: string[]
  about: string
  updatedAt: string
}
