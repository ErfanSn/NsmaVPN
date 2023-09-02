package ir.erfansn.nsmavpn.ui.util.preview

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

annotation class AuthPreviews {

    @Preview(
        group = "PreSignedIn",
        name = "Light"
    )
    @Preview(
        group = "PreSignedIn",
        name = "Dark",
        uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
    )
    annotation class PreSignedIn

    @Preview(
        group = "SignedOut",
        name = "Light"
    )
    @Preview(
        group = "SignedOut",
        name = "Dark",
        uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
    )
    annotation class SignedOut


    @Preview(
        group = "InProgress",
        name = "Light"
    )
    @Preview(
        group = "InProgress",
        name = "Dark",
        uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
    )
    annotation class InProgress

    @Preview(
        group = "PermissionsNotGranted",
        name = "Light"
    )
    @Preview(
        group = "PermissionsNotGranted",
        name = "Dark",
        uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
    )
    annotation class PermissionsNotGranted

    @Preview(
        group = "SignedIn",
        name = "Light"
    )
    @Preview(
        group = "SignedIn",
        name = "Dark",
        uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
    )
    annotation class SignedIn
}