<template>
  <div class="q-mt-md">
    <q-btn color="primary" label="Retry" @click="$emit('retry')" />
  </div>
</template>

<script setup>
defineEmits(["retry"]);
</script>
