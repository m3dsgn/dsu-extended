# DSU Extended Helper Module

Magisk/KernelSU module for improving DSU Extended system-mode reliability.

## What It Does

- Installs custom `gsid` binary for supported `arm/arm64` API levels.
- Forces DSU feature flags:
  - `persist.sys.fflag.override.settings_dynamic_system=true`
  - `persist.sys.fflag.override.settings_dynamic_system_uis=true`
- Enables DSU developer mode setting on boot:
  - `settings put global dynamic_system_developer_mode 1`
- Adds helper CLI:
  - `dsuext-helper status|prep|grant`
- Attempts automatic root policy allow for `com.dsu.extended` on Magisk.
  - On KernelSU, DSU preflight is still applied, and root can be granted once in KernelSU Manager.

## Security Toggle

To disable automatic root policy grant:

```sh
touch /data/adb/modules/dsu_extended/disable_autogrant
```

## Build ZIP

```sh
./gradlew :app:assembleRelease :magisk-module:assembleMagiskModule
```

Output ZIP:

```text
magisk-module/out/module_Dsu_Extended_<versionCode>.zip
```
