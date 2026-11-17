package app.template.patches.tiemtruyen

import app.morphe.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

// Tìm phương thức performLocalInstallerCheck (private, trả về boolean, không có tham số)
internal val performLocalInstallerCheckFingerprint = fingerprint {
    accessFlags(AccessFlags.PRIVATE)
    returns("Z")
    parameters()
    strings("LicenseClient")
}

// Tìm phương thức processResponse (private, trả về void, tham số I và Bundle)
internal val processResponseFingerprint = fingerprint {
    accessFlags(AccessFlags.PRIVATE)
    returns("V")
    parameters("I", "Landroid/os/Bundle;")
    strings("LicenseClient")
}
