import axios from "axios";
import { v4 as uuidv4 } from "uuid";
import { useAuthStore } from "src/stores/auth.store";

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10000,
});

api.interceptors.request.use((config) => {
  const authStore = useAuthStore();

  if (authStore.token) {
    config.headers.Authorization = `Bearer ${authStore.token}`;
  }

  config.headers["X-Correlation-Id"] = uuidv4();

  return config;
});

api.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    const normalizedError = normalizeError(error);
    return Promise.reject(normalizedError);
  },
);

function normalizeError(error) {
  if (!error.response) {
    return {
      status: 0,
      title: "Network Error",
      detail: "Unable to connect to server",
    };
  }

  const { status, data, headers } = error.response;

  const apiError = data?.errors?.[0];

  return {
    status,
    title: apiError?.title || "Error",
    detail: apiError?.detail || "Unexpected error",
    correlationId:
      apiError?.meta?.correlationId ||
      apiError?.correlationId ||
      headers["x-correlation-id"],
  };
}

export default api;
