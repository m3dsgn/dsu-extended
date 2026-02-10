#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

SIGN_PROPS_FILE="${SIGN_PROPS_FILE:-.sign/dsu_extended.prop}"
OUT_DIR="${OUT_DIR:-build/release-artifacts/arm-signed}"

if [ ! -f "$SIGN_PROPS_FILE" ]; then
  echo "Missing signing properties: $SIGN_PROPS_FILE" >&2
  exit 1
fi

if ! command -v rg >/dev/null 2>&1; then
  echo "ripgrep (rg) is required" >&2
  exit 1
fi

version_name="$(rg -n 'val versionName by extra' build.gradle.kts | sed -E 's/.*"([^"]+)".*/\1/' | head -n1)"
version_code="$(rg -n 'val versionCode by extra' build.gradle.kts | sed -E 's/.*\{ *([0-9]+) *\}.*/\1/' | head -n1)"

echo "Building signed ARM APKs for version: ${version_name:-unknown} (${version_code:-unknown})"

./gradlew :app:clean :app:assembleRelease -PENABLE_ABI_SPLITS=true --no-daemon

mkdir -p "$OUT_DIR"
rm -f "$OUT_DIR"/app-*-release.apk

mapfile -t abi_apks < <(find app/build/outputs/apk/release -maxdepth 1 -type f -name "app-*-release.apk" | sort)
if [ "${#abi_apks[@]}" -eq 0 ]; then
  echo "No ABI release APKs were produced. Check ABI split config." >&2
  exit 1
fi

for apk in "${abi_apks[@]}"; do
  cp -f "$apk" "$OUT_DIR/"
done

sdk_dir=""
if [ -f local.properties ]; then
  sdk_dir="$(sed -n 's/^sdk\.dir=//p' local.properties | sed 's#\\:#:#g' | head -n1)"
fi

if [ -z "$sdk_dir" ] && [ -n "${ANDROID_SDK_ROOT:-}" ]; then
  sdk_dir="$ANDROID_SDK_ROOT"
fi
if [ -z "$sdk_dir" ] && [ -n "${ANDROID_HOME:-}" ]; then
  sdk_dir="$ANDROID_HOME"
fi

apksigner_bin=""
if [ -n "$sdk_dir" ] && [ -d "$sdk_dir/build-tools" ]; then
  apksigner_bin="$(find "$sdk_dir/build-tools" -type f -name apksigner 2>/dev/null | sort -V | tail -n1)"
fi

if [ -n "$apksigner_bin" ] && [ -x "$apksigner_bin" ]; then
  echo
  echo "Signature summary:"
  for apk in "$OUT_DIR"/app-*-release.apk; do
    echo "== $(basename "$apk") =="
    "$apksigner_bin" verify --print-certs "$apk" | sed -n '1,40p'
  done
else
  echo "apksigner not found, skipping signature print."
fi

echo
echo "Done. Output:"
find "$OUT_DIR" -maxdepth 1 -type f -name "app-*-release.apk" | sort
