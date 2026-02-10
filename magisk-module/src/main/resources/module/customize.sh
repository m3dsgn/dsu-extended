#!/system/bin/sh

DSUEXT_PACKAGE="com.dsu.extended"

ui_msg() {
  ui_print "- $1"
}

ui_warn() {
  ui_print "! $1"
}

print_watermark() {
  while IFS= read -r line; do
    [ -n "$line" ] && ui_print "  $line"
  done <<'EOF'
▓█████▄   ██████  █    ██    ▓█████ ▒██   ██▒▄▄▄█████▓▓█████  ███▄    █ ▓█████▄ ▓█████ ▓█████▄
▒██▀ ██▌▒██    ▒  ██  ▓██▒   ▓█   ▀ ▒▒ █ █ ▒░▓  ██▒ ▓▒▓█   ▀  ██ ▀█   █ ▒██▀ ██▌▓█   ▀ ▒██▀ ██▌
░██   █▌░ ▓██▄   ▓██  ▒██░   ▒███   ░░  █   ░▒ ▓██░ ▒░▒███   ▓██  ▀█ ██▒░██   █▌▒███   ░██   █▌
░▓█▄   ▌  ▒   ██▒▓▓█  ░██░   ▒▓█  ▄  ░ █ █ ▒ ░ ▓██▓ ░ ▒▓█  ▄ ▓██▒  ▐▌██▒░▓█▄   ▌▒▓█  ▄ ░▓█▄   ▌
░▒████▓ ▒██████▒▒▒▒█████▓    ░▒████▒▒██▒ ▒██▒  ▒██▒ ░ ░▒████▒▒██░   ▓██░░▒████▓ ░▒████▒░▒████▓
 ▒▒▓  ▒ ▒ ▒▓▒ ▒ ░░▒▓▒ ▒ ▒    ░░ ▒░ ░▒▒ ░ ░▓ ░  ▒ ░░   ░░ ▒░ ░░ ▒░   ▒ ▒  ▒▒▓  ▒ ░░ ▒░ ░ ▒▒▓  ▒
 ░ ▒  ▒ ░ ░▒  ░ ░░░▒░ ░ ░     ░ ░  ░░░   ░▒ ░    ░     ░ ░  ░░ ░░   ░ ▒░ ░ ▒  ▒  ░ ░  ░ ░ ▒  ▒
 ░ ░  ░ ░  ░  ░   ░░░ ░ ░       ░    ░    ░    ░         ░      ░   ░ ░  ░ ░  ░    ░    ░ ░  ░
   ░          ░     ░           ░  ░ ░    ░              ░  ░         ░    ░       ░  ░   ░
 ░                                                                       ░              ░
EOF
}

append_prop_if_missing() {
  key="$1"
  value="$2"
  line="${key}=${value}"
  if ! grep -q "^${key}=" "$MODPATH/system.prop" 2>/dev/null; then
    echo "$line" >>"$MODPATH/system.prop"
  fi
}

ensure_module_props() {
  touch "$MODPATH/system.prop"
  append_prop_if_missing "persist.sys.fflag.override.settings_dynamic_system" "true"
  append_prop_if_missing "persist.sys.fflag.override.settings_dynamic_system_uis" "true"
}

install_gsid() {
  ui_msg "Installing custom gsid binary..."
  ui_msg "API Level: $API"
  ui_msg "Arch: $ARCH"

  gsid_path="$MODPATH/bin-$ARCH/$API/gsid"
  if [ ! -f "$gsid_path" ]; then
    ui_warn "Custom gsid binary not found: $gsid_path"
    ui_warn "Falling back to stock system gsid"
    return 1
  fi

  mkdir -p "$MODPATH/system/bin"
  cp "$gsid_path" "$MODPATH/system/bin/gsid"
  chmod 0755 "$MODPATH/system/bin/gsid"
  chcon u:object_r:gsid_exec:s0 "$MODPATH/system/bin/gsid" 2>/dev/null

  append_prop_if_missing "ro.com.dsu.extended.gsid_min_alloc" "0.20"
  append_prop_if_missing "ro.com.dsu.extended.gsid_super_reserve_percent" "50"

  ui_msg "Custom gsid binary installed"
  return 0
}

install_user_app_if_possible() {
  apk_path="$MODPATH/system/priv-app/DsuExtended/ReleaseDsuExtended.apk"
  if [ ! -f "$apk_path" ]; then
    ui_warn "Bundled app APK not found, skipping immediate install"
    return
  fi

  if [ "$BOOTMODE" != "true" ] || ! command -v pm >/dev/null 2>&1; then
    ui_msg "Skipping immediate user-space install (not bootmode)"
    return
  fi

  if pm install -r "$apk_path" >/dev/null 2>&1; then
    ui_msg "Installed/updated $DSUEXT_PACKAGE for current user"
  else
    ui_warn "Immediate pm install failed, app will be available after reboot"
  fi
}

ensure_script_permissions() {
  [ -f "$MODPATH/service.sh" ] && chmod 0755 "$MODPATH/service.sh"
  [ -f "$MODPATH/post-fs-data.sh" ] && chmod 0755 "$MODPATH/post-fs-data.sh"
  if [ -f "$MODPATH/system/bin/dsuext-helper" ]; then
    chmod 0755 "$MODPATH/system/bin/dsuext-helper"
    chcon u:object_r:system_file:s0 "$MODPATH/system/bin/dsuext-helper" 2>/dev/null
  fi
}

clean_temp_arch_bins() {
  rm -rf "$MODPATH/bin-arm"
  rm -rf "$MODPATH/bin-arm64"
}

main() {
  print_watermark
  ensure_module_props
  ensure_script_permissions

  if [ "$ARCH" = "arm64" ] || [ "$ARCH" = "arm" ]; then
    if [ "$API" -ge 29 ] && [ "$API" -le 36 ]; then
      install_gsid
    else
      ui_warn "API $API is outside custom gsid range (29-36), using stock gsid"
    fi
  else
    ui_warn "Unsupported arch for custom gsid: $ARCH"
  fi

  clean_temp_arch_bins
  install_user_app_if_possible
}

main "$@"
