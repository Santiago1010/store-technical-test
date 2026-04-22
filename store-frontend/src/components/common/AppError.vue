<template>
  <div class="column items-center q-pa-md">
    <div class="text-negative text-h6">
      {{ message }}
    </div>

    <RetryButton v-if="retry" @retry="retry" />
  </div>
</template>

<script setup>
import RetryButton from "./RetryButton.vue";

defineProps({
  message: String,
  retry: Function,
});
</script>
