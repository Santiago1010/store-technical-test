import { defineStore } from "pinia";
import { authApi } from "src/api/client";

export const useAuthStore = defineStore("auth", {
  state: () => ({
    token: localStorage.getItem("token") || null,
    user: JSON.parse(localStorage.getItem("user") || "null"),
  }),

  getters: {
    isAuthenticated: (state) => !!state.token,
  },

  actions: {
    async login(credentials) {
      const response = await authApi.login(credentials);

      const token = response.access_token;

      this.token = token;
      this.user = {
        username: response.username,
        role: response.role,
      };

      localStorage.setItem("token", token);
      localStorage.setItem("user", JSON.stringify(this.user));
    },

    async register(payload) {
      const response = await authApi.register(payload);

      const token = response.access_token;

      this.token = token;
      this.user = {
        username: response.username,
        role: response.role,
      };

      localStorage.setItem("token", token);
      localStorage.setItem("user", JSON.stringify(this.user));
    },

    logout() {
      this.token = null;
      this.user = null;

      localStorage.removeItem("token");
      localStorage.removeItem("user");
    },
  },
});
