package app.template.patches.tiemtruyen

import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

val COMPATIBILITY_TIEMTRUYEN = Compatibility(
    name = "Tiệm Truyện Chữ",
    packageName = "com.tiemtruyenchu.app",
    targets = listOf(
        AppTarget(version = "1.1.1", versionCode = 58)
    )
)
