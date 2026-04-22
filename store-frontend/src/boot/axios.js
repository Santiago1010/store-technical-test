import axios from "axios";
import { v4 as uuidv4 } from "uuid";
import { useAuthStore } from "@/stores/auth.store";
import { useRouter } from "vue-router";

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10000,
});

api.interceptors.request.use((config) => {
  const auth = useAuthStore();

  if (auth.token) {
    config.headers.Authorization = `Bearer ${auth.token}`;
  }

  config.headers["X-Correlation-Id"] = uuidv4();

  return config;
});

api.interceptors.response.use(
  (res) => res,
  (error) => {
    const auth = useAuthStore();

    if (error.response?.status === 401) {
      auth.logout();
      window.location.href = "/auth/login";
    }

    return Promise.reject(normalizeError(error));
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
