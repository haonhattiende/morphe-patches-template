package app.template.patches.tiemtruyen

import app.morphe.patcher.patch.resourcePatch
import java.io.File
import java.security.MessageDigest

private const val BUNDLE_ENTRY_PATH = "assets/index.android.bundle"
private const val OPCODE_LOAD_CONST_TRUE: Byte = 0x78
private const val SHA1_TRAILER_SIZE = 20

private fun findPatternOffsets(data: ByteArray): List<Int> {
    if (data.size < SHA1_TRAILER_SIZE + 6) return emptyList()
    val limit = data.size - SHA1_TRAILER_SIZE - 5
    val offsets = ArrayList<Int>()
    var i = 0
    while (i < limit) {
        if (data[i] == 0x37.toByte() &&
            data[i + 4] == 0xFF.toByte() &&
            data[i + 5] == 0x3A.toByte()
        ) {
            offsets.add(i)
            i += 6
        } else {
            i++
        }
    }
    return offsets
}

private fun updateHbcSha1Trailer(data: ByteArray) {
    require(data.size > SHA1_TRAILER_SIZE) { "Kích thước bundle quá nhỏ." }
    val payloadEnd = data.size - SHA1_TRAILER_SIZE
    val digest = MessageDigest.getInstance("SHA-1").digest(data.copyOfRange(0, payloadEnd))
    System.arraycopy(digest, 0, data, payloadEnd, SHA1_TRAILER_SIZE)
}

@Suppress("unused")
val patchHermesBundle = resourcePatch(
    name = "Patch Hermes bundle flags",
    description = "Tự động dò mẫu bytecode, ghi đè LoadConstTrue (0x78) và tính lại SHA-1 cuối file index.android.bundle."
) {
    compatibleWith(COMPATIBILITY_TIEMTRUYEN)

    execute { context ->
        // Trong resourcePatch, context đại diện cho thư mục giải nén APK
        val bundleFile = context[BUNDLE_ENTRY_PATH]
        check(bundleFile.exists()) {
            "Không tìm thấy $BUNDLE_ENTRY_PATH trong gói ứng dụng."
        }

        val data = bundleFile.readBytes()
        val offsets = findPatternOffsets(data)

        check(offsets.isNotEmpty()) {
            "Không tìm thấy vị trí nào khớp mẫu 0x37 [reg] [b2] [b3] 0xFF 0x3A trong $BUNDLE_ENTRY_PATH."
        }

        for (off in offsets) {
            val reg = data[off + 1]
            // Ghi 3 lần LoadConstTrue (0x78, reg) = 6 byte
            data[off] = OPCODE_LOAD_CONST_TRUE
            data[off + 1] = reg
            data[off + 2] = OPCODE_LOAD_CONST_TRUE
            data[off + 3] = reg
            data[off + 4] = OPCODE_LOAD_CONST_TRUE
            data[off + 5] = reg
        }

        updateHbcSha1Trailer(data)
        bundleFile.writeBytes(data)
    }
}
