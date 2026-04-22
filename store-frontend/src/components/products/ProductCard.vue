<template>
  <q-card class="cursor-pointer hover-shadow">
    <q-card-section
      class="row items-center justify-between"
      @click="goToDetail"
    >
      <!-- LEFT -->
      <div class="column">
        <div class="text-h6">{{ product.name }}</div>
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

    <!-- ACTIONS -->
    <q-card-actions align="right" class="q-pt-none">
      <q-btn
        flat
        dense
        icon="edit"
        color="primary"
        @click.stop="emit('edit', product)"
      />
      <q-btn
        flat
        dense
        icon="delete"
        color="negative"
        @click.stop="emit('delete', product)"
      />
    </q-card-actions>
  </q-card>
</template>

<script setup>
import { useRouter } from "vue-router";

const props = defineProps({
  product: { type: Object, required: true },
});

const emit = defineEmits(["edit", "delete"]);
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
