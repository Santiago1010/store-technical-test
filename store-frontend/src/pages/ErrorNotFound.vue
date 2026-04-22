<template>
  <div class="fullscreen flex flex-center column text-center q-pa-md">
    <div class="text-h1 text-weight-bold text-primary">404</div>

    <div class="text-h5 q-mt-md">Page not found</div>

    <div class="text-subtitle2 text-grey q-mt-sm">
      The page you are looking for does not exist or has been moved.
    </div>

    <div class="q-mt-lg">
      <q-btn color="primary" label="Go Home" @click="goHome" />
    </div>
  </div>
</template>

<script setup>
import { useRouter } from "vue-router";

const router = useRouter();

function goHome() {
  router.push({ name: "products" });
}
</script>
