import { defineStore } from "pinia";
import { productsApi } from "src/api/client";

const CACHE_TTL = 60 * 1000; // 60s

export const useProductsStore = defineStore("products", {
  state: () => ({
    items: [],
    loading: false,
    error: null,
    lastFetch: null,
  }),

  getters: {
    isCacheValid(state) {
      if (!state.lastFetch) return false;
      return Date.now() - state.lastFetch < CACHE_TTL;
    },
  },

  actions: {
    async fetchProducts({ force = false } = {}) {
      if (!force && this.isCacheValid) {
        return this.items;
      }

      this.loading = true;
      this.error = null;

      try {
        const response = await productsApi.list();

        this.items = response?.data || response;

        this.lastFetch = Date.now();

        return this.items;
      } catch (err) {
        this.error = mapError(err);
        throw err;
      } finally {
        this.loading = false;
      }
    },

    async getProduct(id) {
      this.loading = true;
      this.error = null;

      try {
        const response = await productsApi.getById(id);
        return response?.data || response;
      } catch (err) {
        this.error = mapError(err);
        throw err;
      } finally {
        this.loading = false;
      }
    },
  },
});

function mapError(err) {
  switch (err.status) {
    case 404:
      return "Product not found";
    case 409:
      return "Conflict";
    case 422:
      return "Validation error";
    case 503:
      return "Service unavailable";
    default:
      return err.detail || "Unexpected error";
  }
}
