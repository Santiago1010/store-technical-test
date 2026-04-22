<template>
  <div class="q-pa-md">
    <!-- HEADER -->
    <div class="row items-center justify-between q-mb-md">
      <div class="text-h5">Products</div>
      <q-btn
        label="New Product"
        color="primary"
        icon="add"
        @click="openCreate"
      />
    </div>

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
    <div v-if="loading" class="flex flex-center q-pa-xl">
      <q-spinner size="40px" />
    </div>

    <AppError v-else-if="error" :message="error" :retry="load" />

    <div v-else-if="!items.length" class="text-grey text-center q-pa-xl">
      No products found
    </div>

    <!-- LIST -->
    <div v-else class="column q-gutter-sm">
      <ProductCard
        v-for="p in items"
        :key="p.id"
        :product="p"
        @edit="openEdit(p)"
        @delete="confirmDelete(p)"
      />
    </div>

    <!-- PAGINATION -->
    <div class="row justify-center q-mt-md">
      <q-pagination
        v-model="page"
        :max="totalPages"
        @update:model-value="onPageChange"
      />
    </div>

    <!-- FORM DIALOG -->
    <ProductFormDialog
      v-model="showForm"
      :product="editingProduct"
      @saved="onSaved"
    />

    <!-- DELETE CONFIRM -->
    <q-dialog v-model="showDeleteConfirm">
      <q-card>
        <q-card-section>
          <div class="text-h6">Delete product</div>
          <div class="text-body2 q-mt-sm">
            Are you sure you want to delete
            <strong>{{ deletingProduct?.name }}</strong
            >?
          </div>
        </q-card-section>
        <q-card-actions align="right">
          <q-btn flat label="Cancel" v-close-popup />
          <q-btn
            label="Delete"
            color="negative"
            :loading="deleting"
            @click="executeDelete"
          />
        </q-card-actions>
      </q-card>
    </q-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useQuasar } from "quasar";
import { useProductsStore } from "@/stores/products.store";
import ProductCard from "@/components/products/ProductCard.vue";
import ProductFormDialog from "@/components/products/ProductFormDialog.vue";
import AppError from "@/components/common/AppError.vue";

const store = useProductsStore();
const route = useRoute();
const router = useRouter();
const $q = useQuasar();

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

// Form dialog
const showForm = ref(false);
const editingProduct = ref(null);

// Delete dialog
const showDeleteConfirm = ref(false);
const deletingProduct = ref(null);
const deleting = ref(false);

const statusOptions = ["ACTIVE", "INACTIVE"];
const sortOptions = ["price,asc", "price,desc", "createdAt,desc"];

async function load() {
  loading.value = true;
  error.value = null;
  try {
    const res = await store.fetchProducts({
      page: page.value - 1,
      size: 10,
      search: filters.search || undefined,
      status: filters.status || undefined,
      sort: filters.sort,
    });
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

// CREATE
function openCreate() {
  editingProduct.value = null;
  showForm.value = true;
}

// EDIT
function openEdit(product) {
  editingProduct.value = product;
  showForm.value = true;
}

// SAVE (create or update)
async function onSaved(formData) {
  try {
    if (formData.id) {
      await store.updateProduct(formData.id, formData);
      $q.notify({ type: "positive", message: "Product updated" });
    } else {
      await store.createProduct(formData);
      $q.notify({ type: "positive", message: "Product created" });
    }
    await load();
  } catch (err) {
    $q.notify({ type: "negative", message: err.detail || "Error saving" });
  }
}

// DELETE
function confirmDelete(product) {
  deletingProduct.value = product;
  showDeleteConfirm.value = true;
}

async function executeDelete() {
  deleting.value = true;
  try {
    await store.deleteProduct(deletingProduct.value.id);
    $q.notify({ type: "positive", message: "Product deleted" });
    showDeleteConfirm.value = false;
    await load();
  } catch (err) {
    $q.notify({ type: "negative", message: err.detail || "Error deleting" });
  } finally {
    deleting.value = false;
  }
}

onMounted(load);
</script>
