<template>
  <div class="q-pa-md flex flex-center">
    <q-card style="width: 400px">
      <q-card-section>
        <div class="text-h6">Register</div>
      </q-card-section>

      <q-card-section>
        <q-input
          v-model="username"
          label="Username"
          :rules="[(val) => !!val || 'Username is required']"
        />

        <q-input
          v-model="email"
          label="Email"
          type="email"
          class="q-mt-md"
          :rules="[
            (val) => !!val || 'Email is required',
            (val) => /.+@.+\..+/.test(val) || 'Email must be valid',
          ]"
        />

        <q-input
          v-model="password"
          label="Password"
          type="password"
          class="q-mt-md"
          :rules="[
            (val) => !!val || 'Password is required',
            (val) => val.length >= 8 || 'Min 8 characters',
          ]"
        />
      </q-card-section>

      <q-card-section>
        <q-btn
          label="Register"
          color="primary"
          :loading="auth.loading"
          @click="handleRegister"
        />
      </q-card-section>

      <q-card-section v-if="auth.error">
        <div class="text-negative">{{ auth.error }}</div>
      </q-card-section>
    </q-card>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "@/stores/auth.store";

const username = ref("");
const email = ref("");
const password = ref("");

const router = useRouter();
const auth = useAuthStore();

async function handleRegister() {
  try {
    await auth.register({
      username: username.value,
      email: email.value,
      password: password.value,
    });

    router.push("/");
  } catch (e) {
    // error already handled in store
  }
}
</script>
