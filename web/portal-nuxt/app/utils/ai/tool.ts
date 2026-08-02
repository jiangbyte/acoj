export interface Source {
  url: string
  title?: string
}

export interface WeatherData {
  location: string
  temperature: number
  condition: 'sunny' | 'cloudy' | 'rainy' | 'snowy'
  humidity: number
  windSpeed: number
}

export interface ChartData {
  title: string
  labels: string[]
  values: number[]
}

export function getSearchQuery(part: { input?: Record<string, unknown> }): string {
  return (part.input?.query as string) ?? (part.input?.q as string) ?? ''
}

export function getSources(part: { output?: Record<string, unknown> }): Source[] {
  return (part.output?.sources as Source[]) ?? []
}
