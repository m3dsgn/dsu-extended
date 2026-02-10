#!/system/bin/sh

set_prop_value() {
  key="$1"
  value="$2"
  if command -v resetprop >/dev/null 2>&1; then
    resetprop -n "$key" "$value"
  else
    setprop "$key" "$value"
  fi
}

set_prop_value persist.sys.fflag.override.settings_dynamic_system true
set_prop_value persist.sys.fflag.override.settings_dynamic_system_uis true
