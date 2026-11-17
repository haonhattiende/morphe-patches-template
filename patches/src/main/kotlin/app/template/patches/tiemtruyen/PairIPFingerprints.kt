package app.template.patches.tiemtruyen

import app.morphe.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val performLocalInstallerCheckFingerprint = fingerprint {
    accessFlags(AccessFlags.PRIVATE)
    returns("Z")
    parameters()
    strings("LicenseClient")
}

internal val processResponseFingerprint = fingerprint {
    accessFlags(AccessFlags.PRIVATE)
    returns("V")
    parameters("I", "Landroid/os/Bundle;")
    strings("LicenseClient")
}
