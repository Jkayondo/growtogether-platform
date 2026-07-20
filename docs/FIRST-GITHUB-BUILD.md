# First GitHub Build

This repository is configured to run one GitHub Actions workflow named **GrowTogether Platform CI**.

The workflow performs these checks in order:

1. Starts PostgreSQL and Redis test services.
2. Installs Java 21.
3. Verifies the Maven wrapper.
4. Compiles the source code.
5. Runs automated tests and packages the application.
6. Uploads test reports.
7. Builds the Docker image only after the Maven build succeeds.

## Reading the result

- A green check means every stage passed.
- A red cross means at least one stage failed.
- Open the failed job and copy the first block beginning with `[ERROR]` or `COMPILATION ERROR`.
- Do not copy only the final `BUILD FAILURE` line; the useful cause normally appears earlier.

The CI encryption keys are deliberately test-only values. They must never be used in production.
