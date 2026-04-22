import api from "src/boot/axios";
import { v4 as uuidv4 } from "uuid";

export async function request(config) {
  const response = await api(config);
  return response.data;
}

export const productsApi = {
  list(params = {}) {
    return request({
      method: "GET",
      url: "/api/v1/products",
      params,
    });
  },

  getById(id) {
    return request({
      method: "GET",
      url: `/api/v1/products/${id}`,
    });
  },

  create(payload) {
    return request({
      method: "POST",
      url: "/api/v1/products",
      data: payload,
    });
  },

  update(id, payload) {
    return request({
      method: "PATCH",
      url: `/api/v1/products/${id}`,
      data: payload,
    });
  },

  delete(id) {
    return request({
      method: "DELETE",
      url: `/api/v1/products/${id}`,
    });
  },
};

export const inventoryApi = {
  getByProductId(productId) {
    return request({
      method: "GET",
      url: `/api/v1/inventory/${productId}`,
    });
  },
};

export const purchasesApi = {
  purchase(payload) {
    return request({
      method: "POST",
      url: "/api/v1/purchases",
      headers: {
        "Idempotency-Key": uuidv4(),
      },
      data: payload,
    });
  },
};

export const authApi = {
  login(payload) {
    return request({
      method: "POST",
      url: "/api/v1/auth/login",
      data: payload,
    });
  },

  register(payload) {
    return request({
      method: "POST",
      url: "/api/v1/auth/register",
      data: payload,
    });
  },
};
