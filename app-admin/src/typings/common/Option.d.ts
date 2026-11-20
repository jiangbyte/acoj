/**
 * OptionLong
 */
interface OptionLong {
  children?: OptionLong[]
  label?: string
  value?: number
  [property: string]: any
}

/**
 * OptionString
 */
interface OptionString {
  children?: OptionString[]
  label?: string
  value?: string
  [property: string]: any
}
