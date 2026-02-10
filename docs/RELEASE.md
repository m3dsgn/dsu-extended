# Release Guide (`0.7-beta`)

This guide is for publishing Dsu Extended on GitHub with a working in-app updater.

## 1. Branch and Repository Policy
- Recommended default branch: `main`.
- Current updater metadata URL should point to `main/other/updater.json`.
- CI is configured for both `main` and `master`, but use `main` for releases.

## 2. Git / GitHub CLI Setup

### Configure Git identity
```bash
git config --global user.name "Your Name"
git config --global user.email "you@example.com"
```

### Login to GitHub CLI
```bash
gh auth login
```
Recommended flow:
- GitHub.com
- HTTPS
- Login with browser

### If this directory is not a Git repo yet
```bash
git init -b main
git add .
git commit -m "chore: initial import"
```

### Create and connect remote repo
```bash
gh repo create <your-user>/dsu-extended --source=. --remote=origin --private
# or --public
```

## 3. Signing Configuration

Release signing is read from `.sign/dsu_extended.prop`:

```properties
keystore=/absolute/path/to/your-release.keystore
keystore_pw=YOUR_STORE_PASSWORD
alias=YOUR_KEY_ALIAS
alias_pw=YOUR_KEY_PASSWORD
```

Notes:
- Do not commit `.sign/*`.
- Keep one stable release key for all future updates.

## 4. Configure Updater Build Properties

Set these in `~/.gradle/gradle.properties` (recommended) or pass via `-P` flags:

```properties
UPDATE_CHECK_URL=https://raw.githubusercontent.com/<your-user>/<your-repo>/main/other/updater.json
AUTHOR_SIGN_DIGEST=<sha1_of_your_release_certificate>
```

### How to get SHA1 digest
```bash
keytool -list -v -keystore /path/to/your-release.keystore -alias YOUR_KEY_ALIAS | rg "SHA1:"
```
Use the lowercase hex digest (without spaces) for `AUTHOR_SIGN_DIGEST`.

## 5. Version and Metadata

For `0.7-beta`, ensure:
- `build.gradle.kts`:
  - `versionCode = 12`
  - `versionName = "0.7-beta"`
- `other/updater.json`:
  - same versionCode/versionName
  - `apkUrl` points to GitHub release asset

Current updater asset naming convention:
- Signed release asset: `app-release.apk` (used by updater)

## 6. Build Artifacts

### Clean build
```bash
./gradlew clean
```

### Build signed release (requires `.sign/dsu_extended.prop`)
```bash
./gradlew :app:assembleRelease
```
Expected signed APK:
- `app/build/outputs/apk/release/app-release.apk`

### Build unsigned release
Option A (when signing file is absent):
```bash
mv .sign/dsu_extended.prop .sign/dsu_extended.prop.bak
./gradlew :app:assembleRelease
mv .sign/dsu_extended.prop.bak .sign/dsu_extended.prop
```
Expected unsigned APK:
- `app/build/outputs/apk/release/app-release-unsigned.apk`

## 7. Publish GitHub Release

```bash
git add .
git commit -m "release: 0.7-beta"
git tag -a v0.7-beta -m "0.7-beta"
git push origin main
git push origin v0.7-beta
```

Create release and upload assets:
```bash
gh release create v0.7-beta \
  app/build/outputs/apk/release/app-release.apk \
  app/build/outputs/apk/release/app-release-unsigned.apk \
  --title "0.7-beta" \
  --notes "Release 0.7-beta"
```

## 8. What to Upload
- Required for updater: signed `app-release.apk`.
- Optional: `app-release-unsigned.apk` (for reproducibility/testing).
- Do not publish debug-key builds as your main release channel.
  - They are only for local testing.
  - Switching users from debug key to your real release key requires reinstall.

## 9. Updater Validation Checklist
- `other/updater.json` is reachable on `main`.
- `versionCode` in JSON is higher than installed app.
- JSON `apkUrl` points to an existing GitHub release asset.
- Installed app signature matches `AUTHOR_SIGN_DIGEST`.
