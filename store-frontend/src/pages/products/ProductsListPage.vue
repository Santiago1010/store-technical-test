<template>
  <div class="q-pa-md">
    <!-- FILTERS -->
    <div class="row q-gutter-sm q-mb-md">
      <q-input
        v-model="filters.search"
        label="Search"
        dense
        debounce="400"
        @update:model-value="onFilterChange"
      />

      <q-select
        v-model="filters.status"
        :options="statusOptions"
        label="Status"
        dense
        clearable
        @update:model-value="onFilterChange"
      />

      <q-select
        v-model="filters.sort"
        :options="sortOptions"
        label="Sort"
        dense
        @update:model-value="onFilterChange"
      />
    </div>

    <!-- STATES -->
    <div v-if="loading">
      <q-spinner />
    </div>

    <AppError v-else-if="error" :message="error" :retry="load" />

    <div v-else-if="!items.length">No products found</div>

    <!-- LIST -->
    <div v-else class="column q-gutter-sm">
      <ProductCard v-for="p in items" :key="p.id" :product="p" />
    </div>

    <!-- PAGINATION -->
    <div class="row justify-center q-mt-md">
      <q-pagination
        v-model="page"
        :max="totalPages"
        @update:model-value="onPageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useProductsStore } from "src/stores/products.store";
import ProductCard from "src/components/products/ProductCard.vue";
import AppError from "src/components/common/AppError.vue";

const store = useProductsStore();
const route = useRoute();
const router = useRouter();

const items = ref([]);
const loading = ref(false);
const error = ref(null);

const page = ref(Number(route.query.page) || 1);
const totalPages = ref(1);

const filters = reactive({
  search: route.query.search || "",
  status: route.query.status || null,
  sort: route.query.sort || "createdAt,desc",
});

const statusOptions = ["ACTIVE", "INACTIVE"];
const sortOptions = ["price,asc", "price,desc", "createdAt,desc"];

async function load() {
  loading.value = true;
  error.value = null;

  try {
    const query = {
      page: page.value - 1, // backend suele ser 0-based
      size: 10,
      search: filters.search || undefined,
      status: filters.status || undefined,
      sort: filters.sort,
    };

    const res = await store.fetchProducts(query);

    items.value = res.content || res;
    totalPages.value = res.totalPages || 1;
  } catch (err) {
    error.value = err.detail;
  } finally {
    loading.value = false;
  }
}

function syncUrl() {
  router.replace({
    query: {
      page: page.value,
      search: filters.search || undefined,
      status: filters.status || undefined,
      sort: filters.sort,
    },
  });
}

function onFilterChange() {
  page.value = 1;
  syncUrl();
  load();
}

function onPageChange() {
  syncUrl();
  load();
}

onMounted(load);
</script>
