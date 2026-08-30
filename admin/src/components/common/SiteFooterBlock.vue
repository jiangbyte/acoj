<!-- Author: Charlie -->

<script setup lang="ts">
import { computed } from 'vue'
import {
  type SiteFooterInfo,
  emptySiteFooter,
  externalHref,
  hasSiteFooterContent,
} from '@/composables/siteFooter'

const props = withDefaults(
  defineProps<{
    footer?: SiteFooterInfo | null
    compact?: boolean
  }>(),
  {
    footer: () => emptySiteFooter(),
    compact: false,
  },
)

const visible = computed(() => hasSiteFooterContent(props.footer ?? emptySiteFooter()))
const copyrightHref = computed(() => externalHref(props.footer?.copyrightUrl ?? ''))
const icpHref = computed(() => externalHref(props.footer?.icpUrl ?? ''))
const psbHref = computed(() => externalHref(props.footer?.psbUrl ?? ''))
</script>

<template>
  <div
    v-if="visible"
    class="site-footer-block"
    :class="{ 'site-footer-block--compact': compact }"
  >
    <template v-if="footer?.copyrightText">
      <a
        v-if="copyrightHref"
        class="site-footer-block__link"
        :href="copyrightHref"
        target="_blank"
        rel="noopener noreferrer"
      >{{ footer.copyrightText }}</a>
      <span
        v-else
        class="site-footer-block__text"
      >{{ footer.copyrightText }}</span>
    </template>

    <template v-if="footer?.icpNumber">
      <span
        v-if="footer?.copyrightText"
        class="site-footer-block__sep"
      >·</span>
      <a
        v-if="icpHref"
        class="site-footer-block__link"
        :href="icpHref"
        target="_blank"
        rel="noopener noreferrer"
      >{{ footer.icpNumber }}</a>
      <span
        v-else
        class="site-footer-block__text"
      >{{ footer.icpNumber }}</span>
    </template>

    <template v-if="footer?.psbNumber">
      <span
        v-if="footer?.copyrightText || footer?.icpNumber"
        class="site-footer-block__sep"
      >·</span>
      <a
        v-if="psbHref"
        class="site-footer-block__link"
        :href="psbHref"
        target="_blank"
        rel="noopener noreferrer"
      >{{ footer.psbNumber }}</a>
      <span
        v-else
        class="site-footer-block__text"
      >{{ footer.psbNumber }}</span>
    </template>
  </div>
</template>

<style scoped>
.site-footer-block {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  line-height: 1.5;
  color: var(--text-color-3);
}

.site-footer-block--compact {
  justify-content: center;
}

.site-footer-block__link {
  color: inherit;
  text-decoration: none;
}

.site-footer-block__link:hover {
  color: var(--primary-color);
  text-decoration: underline;
}

.site-footer-block__sep {
  opacity: 0.55;
}
</style>
