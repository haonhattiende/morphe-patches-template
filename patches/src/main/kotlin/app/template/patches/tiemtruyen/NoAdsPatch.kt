package app.template.patches.tiemtruyen

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstructions
import app.morphe.patcher.patch.Patch
import app.morphe.patcher.patch.patch

// 1. Tạo "Dấu vân tay" để tìm chính xác hàm attachInfo trong class MobileAdsInitProvider
object MobileAdsInitProviderFingerprint : Fingerprint(
    definingClass = "Lcom/google/android/gms/ads/MobileAdsInitProvider;",
    name = "attachInfo",
    returnType = "V",
    parameters = listOf(
        "Landroid/content/Context;",
        "Landroid/content/pm/ProviderInfo;"
    )
)

// 2. Tạo bản vá
val noAdsPatch = patch(
    name = "Xóa quảng cáo",
    description = "Chặn Google AdMob khởi động, từ đó loại bỏ quảng cáo.",
    compatibleWith = listOf(TIEMTRUYEN_TARGET)
) {
    execute {
        // Tìm hàm attachInfo
        val targetMethod = MobileAdsInitProviderFingerprint.method
        
        // Thay thế toàn bộ code bên trong hàm bằng lệnh "return-void" (không làm gì cả và thoát ra)
        targetMethod.replaceInstructions(
            "return-void"
        )
    }
}
