#!/bin/sh
install_gsid() {
  ui_print "- Installing custom gsid binary..."
  ui_print "- API Level: $API"
  ui_print "- Arch: $ARCH"

  gsid_path="$MODPATH/bin-$ARCH/$API/gsid"
  if [ ! -f "$gsid_path" ]; then
    ui_print "! Custom gsid binary not found: $gsid_path"
    ui_print "! Falling back to stock system gsid"
    return 1
  fi

  mkdir -p "$MODPATH/system/bin"
  cp "$gsid_path" "$MODPATH/system/bin/gsid"
  chmod 0755 "$MODPATH/system/bin/gsid"
  chcon u:object_r:gsid_exec:s0 "$MODPATH/system/bin/gsid"

  echo "ro.com.dsu.extended.gsid_min_alloc=0.20" >>"$MODPATH/system.prop"
  echo "ro.com.dsu.extended.gsid_super_reserve_percent=50" >>"$MODPATH/system.prop"
  ui_print "- Custom gsid binary installed"
  return 0
}

clean() {
  rm -rf "$MODPATH/bin-arm"
  rm -rf "$MODPATH/bin-arm64"
}

if [ "$ARCH" = "arm64" ] || [ "$ARCH" = "arm" ]; then
  if [ "$API" -ge 29 ] && [ "$API" -le 36 ]; then
    install_gsid
  else
    ui_print "! API $API is outside custom gsid range (29-36), using stock gsid"
  fi
else
  ui_print "! Unsupported arch for custom gsid: $ARCH"
fi

clean
setprop persist.sys.fflag.override.settings_dynamic_system true

echo "- Installing Dsu Extended..."
pm install "$MODPATH/system/priv-app/DsuExtended/ReleaseDsuExtended.apk"
