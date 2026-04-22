import { createRouter, createWebHistory } from "vue-router";
import routes from "./routes";
import { useAuthStore } from "@/stores/auth.store";

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to, from, next) => {
  const auth = useAuthStore();

  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    return next({ name: "login" });
  }

  if (to.name === "login" && auth.isAuthenticated) {
    return next({ name: "products" });
  }

  next();
});

export default router;
