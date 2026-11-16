package app.template.patches.tiemtruyen

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstructions
import app.morphe.patcher.patch.bytecodePatch

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
    compatibleWith(COMPATIBILITY_TIEMTRUYEN)
    execute {
        // Ghi đè lệnh đầu tiên (số 0) của hàm bằng lệnh "return-void" (thoát ngay lập tức)
        MobileAdsInitProviderFingerprint.method.replaceInstructions(0, "return-void")
    }
}
