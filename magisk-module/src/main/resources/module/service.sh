#!/system/bin/sh

MODDIR=${0%/*}
PKG_NAME="com.dsu.extended"
LOG_TAG="DSUExtHelper"
LOG_FILE="$MODDIR/dsuext-helper.log"

log_msg() {
  msg="$1"
  log -t "$LOG_TAG" "$msg"
  echo "$(date '+%Y-%m-%d %H:%M:%S') $msg" >>"$LOG_FILE"
}

print_watermark() {
  while IFS= read -r line; do
    [ -n "$line" ] && log_msg "$line"
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

set_prop_value() {
  key="$1"
  value="$2"
  if command -v resetprop >/dev/null 2>&1; then
    resetprop -n "$key" "$value"
  else
    setprop "$key" "$value"
  fi
}

apply_dsu_prerequisites() {
  set_prop_value persist.sys.fflag.override.settings_dynamic_system true
  set_prop_value persist.sys.fflag.override.settings_dynamic_system_uis true
  settings put global dynamic_system_developer_mode 1 >/dev/null 2>&1
}

wait_boot_completed() {
  timeout=200
  while [ "$timeout" -gt 0 ]; do
    if [ "$(getprop sys.boot_completed)" = "1" ]; then
      sleep 3
      return 0
    fi
    sleep 2
    timeout=$((timeout - 2))
  done
  return 1
}

resolve_app_uid() {
  cmd package list packages -U "$PKG_NAME" 2>/dev/null | sed -n 's/.* uid:\([0-9][0-9]*\).*/\1/p' | head -n1
}

magisk_sql() {
  sql="$1"
  if ! command -v magisk >/dev/null 2>&1; then
    return 1
  fi
  magisk --sqlite "$sql" >/dev/null 2>&1
}

grant_magisk_root() {
  uid="$1"
  [ -n "$uid" ] || return 1

  # Magisk policy values: 0=prompt, 1=deny, 2=allow.
  magisk_sql "INSERT OR REPLACE INTO policies (uid, policy, until, logging, notification) VALUES ($uid, 2, 0, 1, 0);" && return 0
  magisk_sql "INSERT OR REPLACE INTO policies (uid, policy, logging, notification, until) VALUES ($uid, 2, 1, 0, 0);" && return 0
  magisk_sql "INSERT OR REPLACE INTO policies (uid, policy, logging, notification) VALUES ($uid, 2, 1, 0);" && return 0
  return 1
}

main() {
  print_watermark
  apply_dsu_prerequisites

  if ! wait_boot_completed; then
    log_msg "Boot completion timeout, applied DSU prerequisites only"
    return 0
  fi

  apply_dsu_prerequisites

  if [ -f "$MODDIR/disable_autogrant" ]; then
    log_msg "Auto-grant disabled by $MODDIR/disable_autogrant"
    return 0
  fi

  uid="$(resolve_app_uid)"
  if [ -z "$uid" ]; then
    log_msg "Package $PKG_NAME not installed yet, skipping auto-grant"
    return 0
  fi

  if grant_magisk_root "$uid"; then
    log_msg "Magisk root policy granted for $PKG_NAME (uid=$uid)"
    return 0
  fi

  if [ -d /data/adb/ksu ]; then
    log_msg "KernelSU detected: DSU prerequisites applied. Grant root for $PKG_NAME once in KernelSU Manager."
  else
    log_msg "Could not write root policy automatically; DSU prerequisites still applied"
  fi
}

main "$@"
