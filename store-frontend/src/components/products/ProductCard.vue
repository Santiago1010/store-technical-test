<template>
  <q-card class="cursor-pointer hover-shadow" @click="goToDetail">
    <q-card-section class="row items-center justify-between">
      <!-- LEFT -->
      <div class="column">
        <div class="text-h6">
          {{ product.name }}
        </div>

        <div class="text-caption text-grey">SKU: {{ product.sku }}</div>
      </div>

      <!-- RIGHT -->
      <div class="column items-end">
        <q-badge :color="product.status === 'ACTIVE' ? 'positive' : 'grey'">
          {{ product.status }}
        </q-badge>

        <div class="text-subtitle1 q-mt-sm">
          {{ formatPrice(product.price) }}
        </div>
      </div>
    </q-card-section>
  </q-card>
</template>

<script setup>
import { useRouter } from "vue-router";

const props = defineProps({
  product: {
    type: Object,
    required: true,
  },
});

const router = useRouter();

function goToDetail() {
  router.push(`/products/${props.product.id}`);
}

function formatPrice(value) {
  return new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: "USD",
  }).format(value);
}
</script>

<style scoped>
.hover-shadow:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  transition: 0.2s;
}
</style>
