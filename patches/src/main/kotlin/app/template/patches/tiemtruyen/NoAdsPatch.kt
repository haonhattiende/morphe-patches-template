package app.template.patches.tiemtruyen

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.util.returnEarly

object MobileAdsInitProviderFingerprint : Fingerprint(
    definingClass = "Lcom/google/android/gms/ads/MobileAdsInitProvider;",
    name = "attachInfo",
    returnType = "V",
    parameters = listOf(
        "Landroid/content/Context;",
        "Landroid/content/pm/ProviderInfo;"
    )
)

val noAdsPatch = bytecodePatch(
    name = "Xóa quảng cáo",
    description = "Chặn Google AdMob khởi động, từ đó loại bỏ quảng cáo."
) {
    // Sử dụng đúng tên biến đã khai báo trong Constants.kt
    compatibleWith(COMPATIBILITY_TIEMTRUYEN)
    execute {
        MobileAdsInitProviderFingerprint.method.returnEarly()
    }
}
