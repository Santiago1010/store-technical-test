import { route } from "quasar/wrappers";
import { createRouter, createWebHistory } from "vue-router";
import routes from "./routes";
import { useAuthStore } from "@/stores/auth.store";

export default route(function () {
  const Router = createRouter({
    history: createWebHistory(),
    routes,
  });

  Router.beforeEach((to, from, next) => {
    const auth = useAuthStore();

    if (to.meta.requiresAuth && !auth.isAuthenticated) {
      return next("/auth/login");
    }

    if (to.path === "/auth/login" && auth.isAuthenticated) {
      return next("/");
    }

    next();
  });

  return Router;
});
