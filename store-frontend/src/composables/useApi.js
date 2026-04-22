import { ref } from "vue";

export function useApi(fn, options = {}) {
  const loading = ref(false);
  const error = ref(null);
  const data = ref(null);

  const { immediate = false, retries = 0, retryDelay = 500 } = options;

  const execute = async (...args) => {
    loading.value = true;
    error.value = null;

    let attempt = 0;

    while (attempt <= retries) {
      try {
        const result = await fn(...args);
        data.value = result;
        return result;
      } catch (err) {
        error.value = err;

        const shouldRetry = err.status === 0 || err.status === 503;

        if (!shouldRetry || attempt === retries) {
          throw err;
        }

        await wait(retryDelay * Math.pow(2, attempt));
        attempt++;
      } finally {
        loading.value = false;
      }
    }
  };

  if (immediate) {
    execute();
  }

  return {
    loading,
    error,
    data,
    execute,
  };
}

function wait(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}
