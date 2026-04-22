import { defineStore } from "pinia";
import { productsApi } from "@/api/client";

const CACHE_TTL = 60 * 1000;

export const useProductsStore = defineStore("products", {
  state: () => ({
    cache: {},
    loading: false,
    error: null,
  }),

  actions: {
    invalidateCache() {
      this.cache = {};
    },

    async fetchProducts(query) {
      const key = JSON.stringify(query);
      const cached = this.cache[key];

      if (cached && Date.now() - cached.timestamp < CACHE_TTL) {
        return cached.data;
      }

      this.loading = true;
      this.error = null;

      try {
        const res = await productsApi.list(query);
        const data = res?.data || res;
        this.cache[key] = { data, timestamp: Date.now() };
        return data;
      } catch (err) {
        this.error = err.detail;
        throw err;
      } finally {
        this.loading = false;
      }
    },

    async createProduct(payload) {
      const res = await productsApi.create(payload);
      this.invalidateCache();
      return res?.data || res;
    },

    async updateProduct(id, payload) {
      const res = await productsApi.update(id, payload);
      this.invalidateCache();
      return res?.data || res;
    },

    async deleteProduct(id) {
      await productsApi.delete(id);
      this.invalidateCache();
    },
  },
});
