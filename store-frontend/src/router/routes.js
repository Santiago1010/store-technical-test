const routes = [
  {
    path: "/auth",
    children: [
      {
        path: "login",
        name: "login",
        component: () => import("@/pages/auth/LoginPage.vue"),
      },
    ],
  },
  {
    path: "/",
    component: () => import("@/layouts/MainLayout.vue"),
    meta: { requiresAuth: true },
    children: [
      {
        path: "",
        name: "products",
        component: () => import("@/pages/products/ProductsListPage.vue"),
      },
      {
        path: "products/:id",
        name: "product-detail",
        component: () => import("@/pages/products/ProductDetailPage.vue"),
        props: true,
      },
    ],
  },
  {
    path: "/:catchAll(.*)*",
    name: "not-found",
    component: () => import("@/pages/ErrorNotFound.vue"),
  },
];

export default routes;
