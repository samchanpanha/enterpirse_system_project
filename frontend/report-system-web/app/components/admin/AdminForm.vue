<template>
  <form class="space-y-6" @submit.prevent="onSubmit">
    <div v-for="(group, gi) in groups" :key="gi">
      <div v-if="group.title" class="mb-3">
        <h3 class="text-sm font-medium text-text-primary m-0">
          {{ group.title }}
        </h3>
        <p v-if="group.description" class="text-xs text-text-secondary mt-0.5 mb-0">
          {{ group.description }}
        </p>
      </div>

      <div class="grid grid-cols-12 gap-4">
        <div
          v-for="field in visibleFields(group.fields)"
          :key="field.key"
          :class="[`col-span-${field.span || 12}`]"
        >
          <label
            v-if="field.type !== 'checkbox' && field.type !== 'switch'"
            :for="`f-${field.key}`"
            class="block text-sm font-medium text-text-primary mb-1"
          >
            {{ field.label }}
            <span v-if="field.required" class="text-danger">*</span>
          </label>

          <!-- text-like -->
          <input
            v-if="field.type === 'text' || field.type === 'email' || field.type === 'password' ||
                  field.type === 'tel' || field.type === 'url'"
            :id="`f-${field.key}`"
            v-model="form[field.key]"
            :type="field.type"
            :placeholder="field.placeholder"
            :required="field.required"
            :disabled="field.disabled || readOnly"
            :readonly="field.readonly"
            :maxlength="field.maxlength"
            :minlength="field.minlength"
            class="w-full px-3 py-1.5 text-sm border border-border rounded text-text-primary placeholder:text-text-disabled focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary/30 disabled:bg-gray-50 disabled:text-text-disabled transition-colors"
            @input="onFieldChange(field)"
          >

          <textarea
            v-else-if="field.type === 'textarea'"
            :id="`f-${field.key}`"
            v-model="form[field.key]"
            :placeholder="field.placeholder"
            :required="field.required"
            :disabled="field.disabled || readOnly"
            :readonly="field.readonly"
            :maxlength="field.maxlength"
            rows="3"
            class="w-full px-3 py-1.5 text-sm border border-border rounded text-text-primary placeholder:text-text-disabled focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary/30 disabled:bg-gray-50 transition-colors"
            @input="onFieldChange(field)"
          />

          <input
            v-else-if="field.type === 'number'"
            :id="`f-${field.key}`"
            v-model.number="form[field.key]"
            type="number"
            :placeholder="field.placeholder"
            :required="field.required"
            :disabled="field.disabled || readOnly"
            :readonly="field.readonly"
            :min="field.min"
            :max="field.max"
            :step="field.step"
            class="w-full px-3 py-1.5 text-sm border border-border rounded text-text-primary placeholder:text-text-disabled focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary/30 disabled:bg-gray-50 transition-colors"
            @input="onFieldChange(field)"
          >

          <!-- date/time -->
          <input
            v-else-if="field.type === 'date' || field.type === 'datetime' || field.type === 'time'"
            :id="`f-${field.key}`"
            v-model="form[field.key]"
            :type="field.type === 'datetime' ? 'datetime-local' : field.type"
            :required="field.required"
            :disabled="field.disabled || readOnly"
            class="w-full px-3 py-1.5 text-sm border border-border rounded text-text-primary focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary/30 disabled:bg-gray-50 transition-colors"
            @input="onFieldChange(field)"
          >

          <!-- select -->
          <select
            v-else-if="field.type === 'select'"
            :id="`f-${field.key}`"
            v-model="form[field.key]"
            :required="field.required"
            :disabled="field.disabled || readOnly"
            class="w-full px-3 py-1.5 text-sm border border-border rounded text-text-primary focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary/30 disabled:bg-gray-50 transition-colors"
            @change="onFieldChange(field)"
          >
            <option v-if="field.placeholder" value="">
              {{ field.placeholder }}
            </option>
            <option
              v-for="opt in field.options || []"
              :key="String(opt.value)"
              :value="opt.value"
              :disabled="opt.disabled"
            >
              {{ opt.label }}
            </option>
          </select>

          <!-- checkbox -->
          <label
            v-else-if="field.type === 'checkbox'"
            class="inline-flex items-center gap-2 cursor-pointer text-sm"
          >
            <input
              :id="`f-${field.key}`"
              v-model="form[field.key]"
              type="checkbox"
              :disabled="field.disabled || readOnly"
              class="rounded border-border text-primary focus:ring-primary/30"
              @change="onFieldChange(field)"
            >
            <span class="text-text-primary">{{ field.label }}</span>
            <span v-if="field.required" class="text-danger">*</span>
          </label>

          <!-- switch -->
          <label
            v-else-if="field.type === 'switch'"
            class="inline-flex items-center gap-2 cursor-pointer"
          >
            <button
              type="button"
              :disabled="field.disabled || readOnly"
              :class="[
                'relative w-9 h-5 rounded-full transition-colors',
                form[field.key] ? 'bg-primary' : 'bg-gray-300',
                (field.disabled || readOnly) ? 'opacity-50 cursor-not-allowed' : ''
              ]"
              role="switch"
              :aria-checked="!!form[field.key]"
              @click="toggleSwitch(field)"
            >
              <span
                :class="[
                  'absolute top-0.5 left-0.5 w-4 h-4 bg-white rounded-full transition-transform shadow',
                  form[field.key] ? 'translate-x-4' : ''
                ]"
              />
            </button>
            <span class="text-sm text-text-primary">
              {{ field.label }}
              <span v-if="field.required" class="text-danger">*</span>
            </span>
          </label>

          <p v-if="field.help" class="text-xs text-text-disabled mt-1 mb-0">
            {{ field.help }}
          </p>
          <p
            v-if="errors[field.key]"
            class="text-xs text-danger mt-1 mb-0 flex items-center gap-1"
          >
            <Icon icon="ant-design:exclamation-circle-outlined" />
            {{ errors[field.key] }}
          </p>
        </div>
      </div>
    </div>

    <slot name="actions" />
  </form>
</template>

<script setup lang="ts" generic="T extends Record<string, any>">
import type { FormGroup, FormField } from '~/shared/types/form'

interface Props {
  groups: FormGroup[]
  modelValue: T
  readOnly?: boolean
}

const props = defineProps<Props>()

const emit = defineEmits<{
  'update:modelValue': [value: T]
  submit: [value: T]
}>()

const form = reactive<T>({ ...props.modelValue })
const errors = reactive<Record<string, string>>({})

watch(
  () => props.modelValue,
  (val) => {
    Object.keys(form).forEach(k => { delete (form as any)[k] })
    Object.assign(form, val)
  },
  { deep: true }
)

function visibleFields (fields: FormField[]) {
  return fields.filter(f => !f.hidden || !f.hidden(form as any))
}

function onFieldChange (field: FormField) {
  // re-validate
  if (field.validator) {
    const err = field.validator(form[field.key])
    if (err) {
      errors[field.key] = err
    } else {
      delete errors[field.key]
    }
  }
  emit('update:modelValue', { ...form } as T)
}

function toggleSwitch (field: FormField) {
  if (props.readOnly || field.disabled) { return }
  ;(form as any)[field.key] = !form[field.key]
  onFieldChange(field)
}

function validate (): boolean {
  let valid = true
  for (const group of props.groups) {
    for (const field of group.fields) {
      if (field.hidden?.(form as any)) { continue }
      if (field.required) {
        const v = form[field.key]
        if (v === null || v === undefined || v === '' ||
            (Array.isArray(v) && v.length === 0)) {
          errors[field.key] = `${field.label} is required`
          valid = false
        }
      }
      if (field.validator) {
        const err = field.validator(form[field.key])
        if (err) {
          errors[field.key] = err
          valid = false
        } else {
          delete errors[field.key]
        }
      }
    }
  }
  return valid
}

function onSubmit () {
  if (!validate()) { return }
  emit('submit', { ...form } as T)
}

defineExpose({ validate, form })
</script>
