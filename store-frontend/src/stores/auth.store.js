import { defineStore } from "pinia";
import { authApi } from "@/api/client";

export const useAuthStore = defineStore("auth", {
  state: () => ({
    token: localStorage.getItem("token"),
    user: JSON.parse(localStorage.getItem("user") || "null"),
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
        const res = await authApi.login(payload);

        this.token = res.access_token;
        this.user = {
          username: res.username,
          role: res.role,
        };

        localStorage.setItem("token", this.token);
        localStorage.setItem("user", JSON.stringify(this.user));
      } catch (err) {
        this.error = err.detail;
        throw err;
      } finally {
        this.loading = false;
      }
    },

    logout() {
      this.token = null;
      this.user = null;

      localStorage.removeItem("token");
      localStorage.removeItem("user");
    },
  },
});
