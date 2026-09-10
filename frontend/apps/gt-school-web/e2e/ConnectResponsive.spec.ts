import {
  expect,
  test,
} from "@playwright/test";

const mobileViewport = {
  width: 390,
  height: 844,
};

test.use({
  viewport: mobileViewport,
});

test(
  "GT Connect remains usable at mobile viewport",
  async ({
    page
  }) => {

    /*
     * Test-only authenticated session.
     *
     * GT School AuthProvider reconstructs its
     * user from localStorage. No production
     * authentication behaviour is modified.
     */
    await page.addInitScript(
      () => {

        localStorage.setItem(
          "gt_access_token",
          "playwright-connect-access-token"
        );

        localStorage.setItem(
          "gt_refresh_token",
          "playwright-connect-refresh-token"
        );

        localStorage.setItem(
          "gt_user",
          JSON.stringify({
            id:
              "user-mobile-qa",

            name:
              "Mobile QA Administrator",

            email:
              "mobile.qa@example.test",

            organisationId:
              "tenant-mobile-qa",

            organisationName:
              "GT Mobile QA School",

            role:
              "SCHOOL_ADMIN",

            permissions: [
              "VIEW_DASHBOARD",
            ],
          })
        );
      }
    );

    /*
     * Browser acceptance is intentionally
     * isolated from backend availability.
     *
     * We verify the real React route,
     * production CSS and browser layout while
     * supplying deterministic Connect API data.
     */
    await page.route(
      "**/api/v1/connect/**",
      async route => {

        const request =
          route.request();

        const url =
          new URL(
            request.url()
          );

        let data: unknown =
          [];

        if (
          request.method() === "GET"
          && url.pathname.endsWith(
            "/api/v1/connect/spaces"
          )
        ) {
          data = [
            {
              id:
                "space-mobile-qa",

              spaceType:
                "INSTITUTION",

              name:
                "Mobile QA Space",

              contextType:
                null,

              contextReference:
                null,
            },
          ];
        }

        const payload = {
          success:
            true,

          code:
            "OK",

          message:
            "Browser QA response",

          data,

          errors:
            [],

          metadata: {
            correlationId:
              "pw-connect-responsive",

            tenantId:
              "tenant-mobile-qa",

            timestamp:
              "2026-09-07T00:00:00Z",
          },
        };

        await route.fulfill({
          status:
            200,

          contentType:
            "application/json",

          body:
            JSON.stringify(
              payload
            ),
        });
      }
    );

    await page.goto(
      "/connect"
    );

    await expect(
      page.getByText(
        "GT Connect",
        {
          exact: true,
        }
      ).first()
    ).toBeVisible();

    await expect(
      page.getByText(
        "Your authorised GT Connect spaces"
      )
    ).toBeVisible();

    await expect(
      page.getByRole(
        "heading",
        {
          name: "Mobile QA Space",
          exact: true,
        }
      )
    ).toBeVisible();

    const layout =
      page.locator(
        ".gt-connect-layout"
      );

    const spaces =
      page.locator(
        ".gt-connect-spaces"
      );

    const conversation =
      page.locator(
        ".gt-connect-conversation"
      );

    const composer =
      page.locator(
        ".gt-connect-composer"
      );

    const composerInput =
      page.locator(
        ".gt-connect-composer-input"
      );

    const sendButton =
      page.locator(
        ".gt-connect-composer .gt-button"
      );

    await expect(
      layout
    ).toBeVisible();

    await expect(
      spaces
    ).toBeVisible();

    await expect(
      conversation
    ).toBeVisible();

    await expect(
      composer
    ).toBeVisible();

    await expect(
      composerInput
    ).toBeVisible();

    await expect(
      sendButton
    ).toBeVisible();

    /*
     * At <= 900px the Connect panels must
     * become a single vertical flow.
     */
    const spacesBox =
      await spaces.boundingBox();

    const conversationBox =
      await conversation.boundingBox();

    if (
      !spacesBox
      || !conversationBox
    ) {
      throw new Error(
        "Unable to measure GT Connect mobile panels."
      );
    }

    expect(
      Math.abs(
        spacesBox.x
        - conversationBox.x
      )
    ).toBeLessThanOrEqual(
      3
    );

    expect(
      conversationBox.y
    ).toBeGreaterThanOrEqual(
      spacesBox.y
    );

    /*
     * The conversation must remain within
     * the 390px mobile viewport.
     */
    expect(
      conversationBox.width
    ).toBeLessThanOrEqual(
      mobileViewport.width
    );

    /*
     * At <= 600px the composer must remain
     * usable, not merely visible.
     */
    await composerInput.fill(
      "GT Connect mobile responsive browser QA"
    );

    await expect(
      composerInput
    ).toHaveValue(
      "GT Connect mobile responsive browser QA"
    );

    await expect(
      sendButton
    ).toBeEnabled();

    /*
     * Critical mobile acceptance boundary:
     * no page-level horizontal overflow.
     */
    const viewportState =
      await page.evaluate(
        () => ({
          viewportWidth:
            window.innerWidth,

          documentWidth:
            document.documentElement.scrollWidth,

          bodyWidth:
            document.body.scrollWidth,
        })
      );

    expect(
      viewportState.viewportWidth
    ).toBe(
      mobileViewport.width
    );

    expect(
      viewportState.documentWidth
    ).toBeLessThanOrEqual(
      mobileViewport.width + 1
    );

    expect(
      viewportState.bodyWidth
    ).toBeLessThanOrEqual(
      mobileViewport.width + 1
    );
  }
);
