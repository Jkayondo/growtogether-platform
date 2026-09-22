import {
  expect,
  test
} from "@playwright/test";

import path from "node:path";


function requiredEnvironment(
  name: string
): string {

  const value =
    process.env[name];

  if (!value) {

    throw new Error(
      `Missing required E2E environment variable: ${name}`
    );

  }

  return value;

}


test.use({
  trace: "off",
  video: "off"
});


test(
  "authenticated teacher updates own Curriculum Progress",
  async ({
    page
  }) => {

    const username =
      requiredEnvironment(
        "GT_E2E_USERNAME"
      );

    const password =
      requiredEnvironment(
        "GT_E2E_PASSWORD"
      );

    const coverageId =
      requiredEnvironment(
        "GT_E2E_COVERAGE_ID"
      );

    const coverageItem =
      requiredEnvironment(
        "GT_E2E_COVERAGE_ITEM"
      );

    const screenshotDirectory =
      requiredEnvironment(
        "GT_E2E_SCREENSHOT_DIR"
      );


    page.setDefaultTimeout(
      20_000
    );


    await page.goto(
      "/login"
    );


    await expect(
      page.getByText(
        "GT School Login",
        {
          exact: true
        }
      )
    ).toBeVisible();


    const loginForm =
      page.locator(
        "form"
      );


    await loginForm
      .locator(
        'input[autocomplete="username"]'
      )
      .fill(
        username
      );


    await loginForm
      .locator(
        'input[type="password"]'
      )
      .fill(
        password
      );


    const loginResponsePromise =
      page.waitForResponse(
        response =>
          response.url().includes(
            "/api/v1/eiam/auth/login"
          )
          && response.request().method()
            === "POST"
      );


    await page
      .getByRole(
        "button",
        {
          name: "Login",
          exact: true
        }
      )
      .click();


    const loginResponse =
      await loginResponsePromise;


    expect(
      loginResponse.status()
    ).toBe(
      200
    );


    await page.waitForFunction(
      () =>
        Boolean(
          localStorage.getItem(
            "gt_access_token"
          )
        )
    );


    console.log(
      "BROWSER_REAL_LOGIN=PASS"
    );


    await page.goto(
      "/teacher/workspace"
    );


    await expect(
      page.getByRole(
        "heading",
        {
          name: "Teacher Workspace",
          exact: true
        }
      )
    ).toBeVisible();


    const programme =
      page.getByTestId(
        "teacher-programme"
      );


    await expect(
      programme
    ).toBeVisible();


    await expect(
      programme.getByText(
        "Today's Programme",
        {
          exact: true
        }
      )
    ).toBeVisible();


    await expect(
      programme.getByTestId(
        "teacher-programme-error"
      )
    ).toHaveCount(
      0
    );


    await expect(
      programme
    ).not.toContainText(
      "Today's programme is unavailable."
    );


    const coverageHeading =
      page.getByRole(
        "heading",
        {
          name: "Curriculum Progress",
          exact: true
        }
      );


    await expect(
      coverageHeading
    ).toBeVisible();


    const coverage = page.getByTestId("teacher-coverage");


    await expect(
      coverage.getByText(
        coverageItem,
        {
          exact: true
        }
      )
    ).toBeVisible();


    console.log(
      "BROWSER_TEACHER_WORKSPACE=PASS"
    );

    console.log(
      "BROWSER_TODAYS_PROGRAMME_VISIBLE=YES"
    );

    console.log(
      "BROWSER_COVERAGE_FIXTURE_VISIBLE=YES"
    );


    await page.screenshot({
      path:
        path.join(
          screenshotDirectory,
          "coverage-before-update.png"
        ),

      fullPage:
        true
    });


    const buttons =
      coverage.getByRole(
        "button"
      );


    const buttonTexts =
      await buttons.allTextContents();


    console.log(
      "BROWSER_COVERAGE_ACTIONS="
      + buttonTexts
        .map(
          text => text.trim()
        )
        .filter(Boolean)
        .join("|")
    );


    const progressIndex =
      buttonTexts.findIndex(
        text =>
          /progress/i.test(text)
          && !/ahead/i.test(text)
      );


    expect(
      progressIndex,
      "An In Progress Coverage lifecycle action must be present."
    ).toBeGreaterThanOrEqual(
      0
    );


    const patchPromise =
      page.waitForResponse(
        response =>
          response.url().includes(
            `/api/v1/school/teacher/coverage/${coverageId}/progress`
          )
          && response.request().method()
            === "PATCH"
      );


    await buttons
      .nth(
        progressIndex
      )
      .click();


    const patchResponse =
      await patchPromise;


    console.log(
      `BROWSER_COVERAGE_PATCH_HTTP_STATUS=${patchResponse.status()}`
    );


    expect(
      patchResponse.status()
    ).toBe(
      200
    );


    await expect.poll(
      async () =>
        (
          await coverage.textContent()
        )
        ?? ""
    ).toMatch(
      /IN[_ ]PROGRESS/i
    );


    await expect(
      programme
    ).toBeVisible();


    await page.screenshot({
      path:
        path.join(
          screenshotDirectory,
          "coverage-after-update.png"
        ),

      fullPage:
        true
    });


    console.log(
      "BROWSER_COVERAGE_STATUS_IN_PROGRESS=YES"
    );

    console.log(
      "BROWSER_PROGRAMME_REMAINS_VISIBLE_AFTER_UPDATE=YES"
    );

    console.log(
      "STEP_20_BROWSER_WORKFLOW=PASS"
    );

  }
);
