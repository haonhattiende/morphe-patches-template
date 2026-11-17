package app.template.patches.tiemtruyen

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

@Suppress("unused")
val pairIPBypassPatch = bytecodePatch(
    name = "Bypass PairIP License Check",
    description = "Vô hiệu hóa kiểm tra PairIP cho phép cài đặt từ nguồn ngoài (không qua Google Play)."
) {
    // Chỉ truyền thông tin Compatibility, không truyền Fingerprint vào đây
    compatibleWith(COMPATIBILITY_TIEMTRUYEN)

    execute {
        // 1. Vá performLocalInstallerCheck: Ép luôn trả về true
        performLocalInstallerCheckFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """.trimIndent()
        )

        // 2. Vá processResponse: Ép trạng thái thành FULL_CHECK_OK và thoát sớm
        processResponseFingerprint.method.addInstructions(
            0,
            """
                sget-object v0, Lcom/pairip/licensecheck/LicenseClient${'$'}LicenseCheckState;->FULL_CHECK_OK:Lcom/pairip/licensecheck/LicenseClient${'$'}LicenseCheckState;
                sput-object v0, Lcom/pairip/licensecheck/LicenseClient;->licenseCheckState:Lcom/pairip/licensecheck/LicenseClient${'$'}LicenseCheckState;
                return-void
            """.trimIndent()
        )
    }
}
