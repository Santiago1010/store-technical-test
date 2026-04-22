<template>
  <div class="q-pa-md">
    <div v-if="loading">
      <q-spinner />
    </div>

    <AppError v-else-if="error" :message="error" :retry="load" />

    <div v-else>
      <div class="text-h5">{{ product.name }}</div>
      <div class="text-subtitle1">Price: {{ product.price }}</div>

      <div class="q-mt-md">Stock: {{ inventory?.available }}</div>

      <div class="q-mt-md row items-center q-gutter-sm">
        <q-input v-model.number="quantity" type="number" dense />
        <q-btn label="Buy" color="primary" @click="buy" />
      </div>

      <div v-if="purchaseError" class="text-negative q-mt-sm">
        {{ purchaseError }}
      </div>

      <div v-if="success" class="text-positive q-mt-sm">
        Purchase successful
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRoute } from "vue-router";
import { productsApi, inventoryApi, purchasesApi } from "src/api/client";
import AppError from "src/components/common/AppError.vue";

const route = useRoute();

const product = ref(null);
const inventory = ref(null);

const loading = ref(false);
const error = ref(null);

const quantity = ref(1);
const purchaseError = ref(null);
const success = ref(false);

async function load() {
  loading.value = true;
  error.value = null;

  try {
    const [p, i] = await Promise.all([
      productsApi.getById(route.params.id),
      inventoryApi.getByProductId(route.params.id),
    ]);

    product.value = p.data || p;
    inventory.value = i.data || i;
  } catch (err) {
    error.value = err.detail;
  } finally {
    loading.value = false;
  }
}

async function buy() {
  purchaseError.value = null;
  success.value = false;

  try {
    await purchasesApi.purchase({
      productId: route.params.id,
      quantity: quantity.value,
    });

    success.value = true;
    await load();
  } catch (err) {
    purchaseError.value = err.detail;
  }
}

onMounted(load);
</script>
