## AOSP Patches
This folder contains AOSP patch sets used to build custom `gsid` binaries for Dsu Extended.

Current patch targets:
- `aosp_15`
- `aosp_16`

Included fixes:
- `libfiemap`: fallback to `/sys/fs/f2fs_dev/<block>/features` when `/sys/fs/f2fs/<block>/features` is missing.
- `gsid`: configurable super free-space reservation through `ro.com.dsu.extended.gsid_super_reserve_percent` (0..100, default 100).

Legacy patches for Android 10-13 were removed from this archive because their target code paths no longer match Android 15/16 upstream behavior.
