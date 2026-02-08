#!/usr/bin/env bash
set -euo pipefail

REPO_URL="${REPO_URL:-https://github.com/m3dsgn/dsu-extended.git}"
BRANCH="${BRANCH:-release/0.5-beta-test}"
COMMIT_MSG="${COMMIT_MSG:-test}"
TAG="${TAG:-v0.5-beta}"
RELEASE_TITLE="${RELEASE_TITLE:-0.5-beta}"
RELEASE_NOTES="${RELEASE_NOTES:-Beta 0.5 release build.}"
ART_DIR="${ART_DIR:-build/release-artifacts/0.5-beta}"

SIGNED_APK="$ART_DIR/app-release.apk"
SIGNED_ALIAS_APK="$ART_DIR/app-release-signed.apk"
UNSIGNED_APK="$ART_DIR/app-release-unsigned.apk"
MINI_DEBUG_APK="$ART_DIR/app-miniDebug.apk"

for cmd in git gh; do
  if ! command -v "$cmd" >/dev/null 2>&1; then
    echo "Missing required tool: $cmd" >&2
    exit 1
  fi
done

for required_file in "$SIGNED_APK" "$UNSIGNED_APK"; do
  if [ ! -f "$required_file" ]; then
    echo "Missing required artifact: $required_file" >&2
    exit 1
  fi
done

if [ ! -f "$MINI_DEBUG_APK" ]; then
  echo "Warning: optional artifact missing: $MINI_DEBUG_APK" >&2
fi

if ! git rev-parse --is-inside-work-tree >/dev/null 2>&1; then
  git init
fi

if git remote get-url origin >/dev/null 2>&1; then
  git remote set-url origin "$REPO_URL"
else
  git remote add origin "$REPO_URL"
fi

git checkout -B "$BRANCH"

git add -A

# Explicit exclusions requested by user
# README.md is intentionally excluded from commit.
git rm --cached --ignore-unmatch -- README.md >/dev/null 2>&1 || true

# Extra safety exclusions for local/private files.
git rm -r --cached --ignore-unmatch -- .sign local.properties .gradle .kotlin build app/build hidden-api-stub/build >/dev/null 2>&1 || true

BLOCKED_PATTERN='(^README\.md$|^local\.properties$|^\.sign/|^\.gradle/|^\.kotlin/|^build/)'
if git diff --cached --name-only | rg -n "$BLOCKED_PATTERN" >/dev/null 2>&1; then
  echo "Blocked files are staged. Aborting for safety." >&2
  git diff --cached --name-only | rg "$BLOCKED_PATTERN" >&2 || true
  exit 1
fi

if [ -z "$(git diff --cached --name-only)" ]; then
  echo "No staged changes to commit."
else
  git commit -m "$COMMIT_MSG"
fi

git push -u origin "$BRANCH"

assets=("$SIGNED_APK#app-release.apk")
[ -f "$SIGNED_ALIAS_APK" ] && assets+=("$SIGNED_ALIAS_APK#app-release-signed.apk")
[ -f "$UNSIGNED_APK" ] && assets+=("$UNSIGNED_APK#app-release-unsigned.apk")
[ -f "$MINI_DEBUG_APK" ] && assets+=("$MINI_DEBUG_APK#app-miniDebug.apk")

if gh release view "$TAG" >/dev/null 2>&1; then
  gh release upload "$TAG" "${assets[@]}" --clobber
else
  gh release create "$TAG" \
    "${assets[@]}" \
    --title "$RELEASE_TITLE" \
    --notes "$RELEASE_NOTES" \
    --target "$BRANCH" \
    --prerelease
fi

echo
echo "Done."
echo "Branch: $BRANCH"
echo "Tag/Release: $TAG"
echo "Repo: $REPO_URL"
