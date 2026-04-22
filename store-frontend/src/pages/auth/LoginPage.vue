<template>
  <div class="fullscreen flex flex-center">
    <q-card style="width: 350px">
      <q-card-section>
        <div class="text-h6">Login</div>
      </q-card-section>

      <q-card-section>
        <q-input v-model="form.username" label="Username" dense />

        <q-input
          v-model="form.password"
          label="Password"
          type="password"
          dense
        />

        <div v-if="auth.error" class="text-negative q-mt-sm">
          {{ auth.error }}
        </div>
      </q-card-section>

      <q-card-actions align="right">
        <q-btn
          label="Login"
          color="primary"
          :loading="auth.loading"
          @click="submit"
        />
      </q-card-actions>
    </q-card>
  </div>
</template>

<script setup>
import { reactive } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "@/stores/auth.store";

const router = useRouter();
const auth = useAuthStore();

const form = reactive({
  username: "",
  password: "",
});

async function submit() {
  try {
    await auth.login(form);
    router.push("/");
  } catch (e) {
    // ya manejado en store
  }
}
</script>
