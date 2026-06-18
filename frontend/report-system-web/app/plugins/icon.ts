import { Icon, addCollection } from '@iconify/vue'
import antDesignIcons from '@iconify-json/ant-design/icons.json'

// Register the ant-design icon set for offline use.
// This bundles ~700KB of icon data so all ant-design:* icons render
// during SSR and on the client without any network calls.
addCollection(antDesignIcons as any)

export default defineNuxtPlugin((nuxtApp) => {
  nuxtApp.vueApp.component('Icon', Icon)
})
