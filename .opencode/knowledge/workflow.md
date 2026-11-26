# Local workflow

Repository: `clodhub/video-calls`. Licence: `Apache-2.0` (`LICENCE`, `NOTICE`).

Required-local suites, from the package root: `node --test tests/video.test.mjs`, `node --test tests/browser-process.test.mjs`, `node --test tests/provider.test.mjs`, the three `python3 -m unittest discover -s tests -p test_*.py` suites (`test_dependency_metadata.py`, `test_apple_results.py`, `test_native_runner.py`) and `npm run typecheck` (after `npm ci --ignore-scripts --no-audit --no-fund`).
Heavy suites (Xcode, Gradle, Chromium download, deployed provider): `scripts/platform-check.py apple-build|apple|android`, `implementation/verification/provider-runner.mjs`. Do not run them casually.
Review a fetched archive: `python3 -m clod review <digest> --store STORE --policy POLICY --trusted --allow-network`. Always `-B`; remove any `tests/__pycache__`.
