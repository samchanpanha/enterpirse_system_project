import type { Directive, DirectiveBinding } from 'vue'

/**
 * v-feature="'inventory.stock_transfers'"
 *
 * Hides the element if the current tenant lacks the feature code.
 * Aliases `v-feature-show` to use display:none instead of removing the node.
 */
type FeatureValue = string | string[]

function check (binding: DirectiveBinding<FeatureValue>): boolean {
  const { has, hasAll } = useFeature()
  const v = binding.value
  if (!v) { return true }
  if (Array.isArray(v)) { return hasAll(v) }
  return has(v)
}

function apply (el: HTMLElement, binding: DirectiveBinding<FeatureValue>) {
  const ok = check(binding)
  if (binding.arg === 'show') {
    el.style.display = ok ? '' : 'none'
  } else {
    el.style.display = ok ? '' : 'none'
  }
}

const vFeature: Directive<HTMLElement, FeatureValue> = {
  mounted: apply,
  updated: apply
}

export default defineNuxtPlugin((nuxtApp) => {
  nuxtApp.vueApp.directive('feature', vFeature)
})
