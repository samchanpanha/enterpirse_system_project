import type { FormField, FormGroup, FormFieldOption } from '~/shared/types/form'

/**
 * useFormSchema — declarative helpers for building form schemas from
 * common patterns. Saves boilerplate when defining form groups.
 *
 * Examples:
 *
 *   const { text, number, select, switch: switchField, group, opt } = useFormSchema()
 *
 *   const formGroups = [
 *     group('Identity', [
 *       text('code', 'Code', { required: true, placeholder: 'BR01' }),
 *       select('type', 'Type', [
 *         opt('HQ', 'HQ'),
 *         opt('STORE', 'Store')
 *       ], { required: true })
 *     ])
 *   ]
 */
export const useFormSchema = () => {
  function text (key: string, label: string, opts: Partial<FormField> = {}): FormField {
    return { key, label, type: 'text', ...opts }
  }

  function number (key: string, label: string, opts: Partial<FormField> = {}): FormField {
    return { key, label, type: 'number', ...opts }
  }

  function textarea (key: string, label: string, opts: Partial<FormField> = {}): FormField {
    return { key, label, type: 'textarea', ...opts }
  }

  function email (key: string, label: string, opts: Partial<FormField> = {}): FormField {
    return { key, label, type: 'email', ...opts }
  }

  function tel (key: string, label: string, opts: Partial<FormField> = {}): FormField {
    return { key, label, type: 'tel', ...opts }
  }

  function url (key: string, label: string, opts: Partial<FormField> = {}): FormField {
    return { key, label, type: 'url', ...opts }
  }

  function password (key: string, label: string, opts: Partial<FormField> = {}): FormField {
    return { key, label, type: 'password', ...opts }
  }

  function date (key: string, label: string, opts: Partial<FormField> = {}): FormField {
    return { key, label, type: 'date', ...opts }
  }

  function datetime (key: string, label: string, opts: Partial<FormField> = {}): FormField {
    return { key, label, type: 'datetime', ...opts }
  }

  function select (key: string, label: string, options: FormFieldOption[], opts: Partial<FormField> = {}): FormField {
    return { key, label, type: 'select', options, ...opts }
  }

  function multiselect (key: string, label: string, options: FormFieldOption[], opts: Partial<FormField> = {}): FormField {
    return { key, label, type: 'multiselect', options, ...opts }
  }

  function checkbox (key: string, label: string, opts: Partial<FormField> = {}): FormField {
    return { key, label, type: 'checkbox', ...opts }
  }

  function switchField (key: string, label: string, opts: Partial<FormField> = {}): FormField {
    return { key, label, type: 'switch', ...opts }
  }

  function opt (value: string | number | boolean, label: string, opts: Partial<FormFieldOption> = {}): FormFieldOption {
    return { value, label, ...opts }
  }

  function group (title: string, fields: FormField[], description?: string): FormGroup {
    return { title, fields, description }
  }

  /**
   * Build a quick schema for a simple flat object.
   * Each entry is a tuple of [key, label, type, options?]
   */
  function quick (entries: Array<[string, string, FormField['type']]>): FormGroup {
    return {
      fields: entries.map(([key, label, type]) => ({ key, label, type }))
    }
  }

  return {
    text,
    number,
    textarea,
    email,
    tel,
    url,
    password,
    date,
    datetime,
    select,
    multiselect,
    checkbox,
    switchField,
    opt,
    group,
    quick
  }
}
