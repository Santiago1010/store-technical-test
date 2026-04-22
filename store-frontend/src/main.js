import { createApp } from "vue";
import { Quasar } from "quasar";
import quasarLang from "quasar/lang/es";

import { createPinia } from "pinia";
import router from "./router";

import "@quasar/extras/material-icons/material-icons.css";
import "quasar/src/css/index.sass";

import App from "./App.vue";

const app = createApp(App);

const pinia = createPinia();
app.use(pinia);

app.use(router);

app.use(Quasar, {
  plugins: {},
  lang: quasarLang,
});

app.mount("#app");
