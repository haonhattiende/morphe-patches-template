package app.template.patches.tiemtruyen

import app.morphe.patcher.patch.BytecodePatchContext
import app.morphe.patcher.patch.byteArrayPatch
import java.io.File
import java.security.MessageDigest

private const val BUNDLE_ENTRY_PATH = "assets/index.android.bundle"
private const val OPCODE_LOAD_CONST_TRUE: Byte = 0x78
private const val SHA1_TRAILER_SIZE = 20

/**
 * Tìm tất cả vị trí khớp mẫu 6 byte:
 * [0] = 0x37
 * [1] = reg (thanh ghi đích)
 * [2..3] = 2 byte bất kỳ
 * [4..5] = 0xFF, 0x3A
 */
private fun findPatternOffsets(data: ByteArray): List<Int> {
    if (data.size < SHA1_TRAILER_SIZE + 6) return emptyList()

    val limit = data.size - SHA1_TRAILER_SIZE - 5
    val offsets = ArrayList<Int>()
    var i = 0

    while (i < limit) {
        if (
            data[i] == 0x37.toByte() &&
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

/**
 * Tính lại 20 byte SHA-1 ở cuối file cho chuẩn HBC v96.
 */
private fun updateHbcSha1Trailer(data: ByteArray) {
    require(data.size > SHA1_TRAILER_SIZE) {
        "Kích thước bundle quá nhỏ để chứa SHA-1 trailer."
    }
    val payloadEnd = data.size - SHA1_TRAILER_SIZE
    val digest = MessageDigest.getInstance("SHA-1").digest(data.copyOfRange(0, payloadEnd))
    require(digest.size == SHA1_TRAILER_SIZE) {
        "Độ dài SHA-1 không hợp lệ: ${digest.size}"
    }
    System.arraycopy(digest, 0, data, payloadEnd, SHA1_TRAILER_SIZE)
}

/**
 * Kiểm tra 20 byte cuối file đã khớp chuẩn SHA-1 của phần dữ liệu phía trước chưa.
 */
private fun verifyHbcSha1Trailer(data: ByteArray): Boolean {
    if (data.size <= SHA1_TRAILER_SIZE) return false
    val payloadEnd = data.size - SHA1_TRAILER_SIZE
    val expected = MessageDigest.getInstance("SHA-1").digest(data.copyOfRange(0, payloadEnd))
    return data.copyOfRange(payloadEnd, data.size).contentEquals(expected)
}

private fun findBundleFile(relativeEntryPath: String): File? {
    val normalizedTarget = relativeEntryPath.replace('\\', '/')
    val searchRoots = listOfNotNull(
        File(".").takeIf { it.exists() },
        File("build").takeIf { it.exists() },
    )

    for (root in searchRoots) {
        val found = root.walkTopDown()
            .firstOrNull { file ->
                file.isFile && file.path.replace('\\', '/').endsWith("/$normalizedTarget")
            }
        if (found != null) return found
    }
    return null
}

@Suppress("unused")
val patchHermesBundle = byteArrayPatch(
    name = "Patch Hermes bundle flags",
    description = "Tự động dò mẫu bytecode, ghi đè LoadConstTrue (0x78) và tính lại SHA-1 cuối file index.android.bundle.",
) {
    compatibleWith(COMPATIBILITY_TIEMTRUYEN)

    execute { _: BytecodePatchContext ->
        val bundleFile = findBundleFile(BUNDLE_ENTRY_PATH)
            ?: error("Không tìm thấy $BUNDLE_ENTRY_PATH trong gói ứng dụng.")

        val data = bundleFile.readBytes()
        val offsets = findPatternOffsets(data)

        check(offsets.isNotEmpty()) {
            "Không tìm thấy vị trí nào khớp mẫu 0x37 [reg] [b2] [b3] 0xFF 0x3A trong $BUNDLE_ENTRY_PATH."
        }

        for (off in offsets) {
            check(
                data[off] == 0x37.toByte() &&
                    data[off + 4] == 0xFF.toByte() &&
                    data[off + 5] == 0x3A.toByte()
            ) {
                "Sai byte tại offset 0x${off.toString(16)}"
            }

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
        check(verifyHbcSha1Trailer(data)) {
            "Xác minh SHA-1 thất bại sau khi ghi đè $BUNDLE_ENTRY_PATH."
        }

        bundleFile.writeBytes(data)
    }
}
