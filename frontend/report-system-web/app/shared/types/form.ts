export type FormFieldType =
  | 'text'
  | 'textarea'
  | 'number'
  | 'email'
  | 'password'
  | 'tel'
  | 'url'
  | 'select'
  | 'multiselect'
  | 'checkbox'
  | 'switch'
  | 'date'
  | 'datetime'
  | 'time'

export interface FormFieldOption {
  label: string
  value: string | number | boolean
  disabled?: boolean
}

export interface FormField {
  key: string
  label: string
  type: FormFieldType
  placeholder?: string
  help?: string
  required?: boolean
  disabled?: boolean
  readonly?: boolean
  /** For select/multiselect */
  options?: FormFieldOption[]
  /** For select/multiselect — async options fetcher */
  loadOptions?: () => Promise<FormFieldOption[]>
  /** For text-like fields */
  maxlength?: number
  minlength?: number
  /** For number */
  min?: number
  max?: number
  step?: number
  /** Layout */
  span?: 1 | 2 | 3 | 4 | 6 | 12
  /** Custom validation rule */
  validator?: (val: any) => string | null
  /** Prefix/suffix content (e.g. currency symbol) */
  prefix?: string
  suffix?: string
  /** Hidden if returns true */
  hidden?: (form: Record<string, any>) => boolean
}

export interface FormGroup {
  title?: string
  description?: string
  fields: FormField[]
}
