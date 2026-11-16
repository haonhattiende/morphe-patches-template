package app.template.patches.tiemtruyen

import app.morphe.patcher.patch.Patch
import app.morphe.patcher.patch.patch

val noAdsPatch = patch(
    name = "Xóa quảng cáo",
    description = "Chặn các quảng cáo xuất hiện trong Tiệm Truyện Chữ.",
    compatibleWith = listOf(TIEMTRUYEN_TARGET)
) {
    // Khu vực viết logic vá (chúng ta sẽ điền vào đây sau)
}
