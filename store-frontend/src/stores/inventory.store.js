import { defineStore } from "pinia";
import { inventoryApi, purchasesApi } from "@/api/client";

export const useInventoryStore = defineStore("inventory", {
  state: () => ({
    stock: null,
    loading: false,
    error: null,
    success: false,
  }),

  actions: {
    async fetchInventory(productId) {
      this.loading = true;
      this.error = null;

      try {
        const response = await inventoryApi.getByProductId(productId);
        this.stock = response?.data || response;
      } catch (err) {
        this.error = mapError(err);
        throw err;
      } finally {
        this.loading = false;
      }
    },

    async purchase(productId, quantity) {
      this.loading = true;
      this.error = null;
      this.success = false;

      try {
        await purchasesApi.purchase({
          productId,
          quantity,
        });

        this.success = true;
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
      return "Conflict (concurrent update)";
    case 422:
      return "Insufficient stock";
    case 503:
      return "Inventory service unavailable. Try again later.";
    default:
      return err.detail || "Unexpected error";
  }
}
