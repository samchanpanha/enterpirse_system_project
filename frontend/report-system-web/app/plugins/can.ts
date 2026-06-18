import type { Directive, DirectiveBinding } from 'vue'

/**
 * v-can="'inventory:transfer:create'"
 *
 * Hides the element if the current user lacks the permission code.
 *
 * Usage:
 *   <button v-can="'branches.create'">New Branch</button>
 *   <a v-can="['users.read', 'users.update']" href="/users">Users</a>
 *
 * The element is removed from the DOM (v-if-style). For a display:none
 * variant, use `v-can-show` which keeps the node in the tree.
 */
type CanValue = string | string[]

function check (binding: DirectiveBinding<CanValue>): boolean {
  const { can, canAll } = usePermission()
  const v = binding.value
  if (!v) { return true }
  if (Array.isArray(v)) { return canAll(v) }
  return can(v)
}

function apply (el: HTMLElement, binding: DirectiveBinding<CanValue>) {
  const ok = check(binding)
  if (binding.arg === 'show') {
    el.style.display = ok ? '' : 'none'
  } else {
    el.style.display = ok ? '' : 'none'
    if (!ok) {
      el.setAttribute('data-v-can-hidden', 'true')
    } else {
      el.removeAttribute('data-v-can-hidden')
    }
  }
}

const vCan: Directive<HTMLElement, CanValue> = {
  mounted: apply,
  updated: apply
}

const vCanShow: Directive<HTMLElement, CanValue> = {
  mounted: apply,
  updated: apply
}

export default defineNuxtPlugin((nuxtApp) => {
  nuxtApp.vueApp.directive('can', vCan)
  nuxtApp.vueApp.directive('can-show', vCanShow)
})
