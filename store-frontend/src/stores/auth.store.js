import { defineStore } from "pinia";
import { authApi } from "@/api/client";

export const useAuthStore = defineStore("auth", {
  state: () => ({
    token: localStorage.getItem("token") || null,
    user: null,
    loading: false,
    error: null,
  }),

  getters: {
    isAuthenticated: (state) => !!state.token,
  },

  actions: {
    async login(payload) {
      this.loading = true;
      this.error = null;

      try {
        const data = await authApi.login(payload);

        this.token = data.access_token;
        this.user = {
          username: data.username,
          role: data.role,
        };

        localStorage.setItem("token", this.token);
      } catch (err) {
        this.error = err?.response?.data?.errors?.[0]?.detail || "Login failed";

        throw err;
      } finally {
        this.loading = false;
      }
    },

    async register(payload) {
      this.loading = true;
      this.error = null;

      try {
        const data = await authApi.register(payload);

        this.token = data.access_token;
        this.user = {
          username: data.username,
          role: data.role,
        };

        localStorage.setItem("token", this.token);
      } catch (err) {
        this.error =
          err?.response?.data?.errors?.[0]?.detail || "Register failed";

        throw err;
      } finally {
        this.loading = false;
      }
    },

    logout() {
      this.token = null;
      this.user = null;
      localStorage.removeItem("token");
    },
  },
});
