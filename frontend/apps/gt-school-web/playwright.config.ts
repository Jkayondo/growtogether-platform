import {
  defineConfig
} from "@playwright/test";

export default defineConfig({
  testDir: "./e2e",

  fullyParallel: false,

  workers: 1,

  timeout: 30_000,

  reporter: "line",

  use: {
    baseURL:
      "http://127.0.0.1:4174",

    trace:
      "retain-on-failure",
  },

  webServer: {
    command:
      "npm run dev -- --host 127.0.0.1 --port 4174",

    url:
      "http://127.0.0.1:4174",

    reuseExistingServer:
      false,

    timeout:
      120_000,
  },
});
