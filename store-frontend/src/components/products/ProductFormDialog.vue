<template>
  <q-dialog v-model="open" persistent>
    <q-card style="min-width: 420px">
      <q-card-section class="row items-center q-pb-none">
        <div class="text-h6">{{ isEdit ? "Edit Product" : "New Product" }}</div>
        <q-space />
        <q-btn icon="close" flat round dense @click="open = false" />
      </q-card-section>

      <q-card-section class="column q-gutter-sm">
        <q-input
          v-model="form.name"
          label="Name *"
          :error="!!errors.name"
          :error-message="errors.name"
          dense
        />

        <q-input
          v-model="form.sku"
          label="SKU *"
          :error="!!errors.sku"
          :error-message="errors.sku"
          dense
        />

        <q-input
          v-model.number="form.price"
          label="Price *"
          type="number"
          prefix="$"
          :error="!!errors.price"
          :error-message="errors.price"
          dense
        />

        <q-input
          v-model="form.description"
          label="Description"
          type="textarea"
          dense
          autogrow
        />

        <q-select
          v-model="form.status"
          :options="statusOptions"
          label="Status"
          dense
        />
      </q-card-section>

      <q-card-actions align="right">
        <q-btn flat label="Cancel" @click="open = false" />
        <q-btn
          :label="isEdit ? 'Save' : 'Create'"
          color="primary"
          :loading="saving"
          @click="submit"
        />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch } from "vue";

const props = defineProps({
  modelValue: Boolean,
  product: { type: Object, default: null },
});

const emit = defineEmits(["update:modelValue", "saved"]);

const open = computed({
  get: () => props.modelValue,
  set: (v) => emit("update:modelValue", v),
});

const isEdit = computed(() => !!props.product);

const statusOptions = ["ACTIVE", "INACTIVE"];

const defaultForm = () => ({
  name: "",
  sku: "",
  price: null,
  description: "",
  status: "ACTIVE",
});

const form = reactive(defaultForm());
const errors = reactive({});
const saving = ref(false);

// Populate on edit
watch(
  () => props.product,
  (p) => {
    if (p) Object.assign(form, { ...defaultForm(), ...p });
    else Object.assign(form, defaultForm());
    Object.keys(errors).forEach((k) => delete errors[k]);
  },
  { immediate: true },
);

function validate() {
  Object.keys(errors).forEach((k) => delete errors[k]);
  if (!form.name?.trim()) errors.name = "Required";
  if (!form.sku?.trim()) errors.sku = "Required";
  if (!form.price || form.price <= 0) errors.price = "Must be > 0";
  return !Object.keys(errors).length;
}

async function submit() {
  if (!validate()) return;
  emit("saved", { ...form });
  open.value = false;
}
</script>
