package app.template.patches.tiemtruyen

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstructions
import app.morphe.patcher.patch.patch

object MobileAdsInitProviderFingerprint : Fingerprint(
    definingClass = "Lcom/google/android/gms/ads/MobileAdsInitProvider;",
    name = "attachInfo",
    returnType = "V",
    parameters = listOf(
        "Landroid/content/Context;",
        "Landroid/content/pm/ProviderInfo;"
    )
)

val noAdsPatch = patch(
    name = "Xóa quảng cáo",
    description = "Chặn Google AdMob khởi động, từ đó loại bỏ quảng cáo.",
    compatibleWith = listOf(TIEMTRUYEN_TARGET)
) {
    execute {
        val targetMethod = MobileAdsInitProviderFingerprint.method
        
        // Thêm số 0 vào trước chuỗi "return-void"
        targetMethod.replaceInstructions(0, "return-void")
    }
}
