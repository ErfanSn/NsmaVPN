@file:OptIn(ExperimentalMaterial3WindowSizeClassApi::class)

package ir.erfansn.nsmavpn.feature.auth

import androidx.annotation.StringRes
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.Scope
import com.google.api.services.gmail.GmailScopes
import ir.erfansn.nsmavpn.R
import ir.erfansn.nsmavpn.feature.auth.google.*
import ir.erfansn.nsmavpn.ui.component.NsmaVpnBackground
import ir.erfansn.nsmavpn.ui.component.NsmaVpnScaffold
import ir.erfansn.nsmavpn.ui.theme.NsmaVpnTheme
import ir.erfansn.nsmavpn.ui.util.preview.AuthPreviews
import ir.erfansn.nsmavpn.ui.util.preview.PreviewLightDarkLandscape
import ir.erfansn.nsmavpn.ui.util.preview.parameter.VpnGateSubscriptionStatusParameterProvider
import ir.erfansn.nsmavpn.ui.util.rememberErrorNotifier
import kotlinx.coroutines.launch
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@Composable
fun AuthRoute(
    windowSize: WindowSizeClass,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AuthScreen(
        modifier = modifier,
        uiState = uiState,
        windowSize = windowSize,
        onSuccessfulSignIn = viewModel::verifyVpnGateSubscriptionAndSaveIt,
        onErrorShown = viewModel::notifyMessageShown,
        onNavigateToHome = onNavigateToHome,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AuthScreen(
    windowSize: WindowSizeClass,
    uiState: AuthUiState,
    modifier: Modifier = Modifier,
    onSuccessfulSignIn: (GoogleSignInAccount) -> Unit = { },
    onErrorShown: () -> Unit = { },
    onNavigateToHome: () -> Unit = { },
    googleAuthState: GoogleAuthState = rememberGoogleAuthState(
        clientId = R.string.web_client_id,
        Scope(GmailScopes.GMAIL_READONLY),
        Scope(Scopes.PROFILE),
        Scope(Scopes.EMAIL)
    ),
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val errorNotifier = rememberErrorNotifier(snackbarHostState, coroutineScope)

    DisposableEffect(googleAuthState) {
        googleAuthState.onSignInResult = {
            when (it) {
                is GoogleAccountSignInResult.Error -> coroutineScope.launch {
                    errorNotifier.showErrorMessage(
                        messageId = it.messageId,
                        actionLabelId = R.string.ok
                    )
                }

                is GoogleAccountSignInResult.Success -> {
                    it.googleSignInAccount?.run(onSuccessfulSignIn)
                }
            }
        }
        onDispose {
            googleAuthState.onSignInResult = null
        }
    }

    LaunchedEffect(googleAuthState, uiState.errorMessage, onErrorShown) {
        if (uiState.errorMessage != null) {
            googleAuthState.signOut()
            errorNotifier.showErrorMessage(
                messageId = uiState.errorMessage,
                actionLabelId = R.string.ok
            )
            onErrorShown()
        }
    }

    NsmaVpnScaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { contentPadding ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            val contentLayoutModifier = when {
                windowSize.widthSizeClass == WindowWidthSizeClass.Compact &&
                        windowSize.heightSizeClass == WindowHeightSizeClass.Compact -> {
                    Modifier.fillMaxSize()
                }
                windowSize.heightSizeClass == WindowHeightSizeClass.Compact -> {
                    Modifier.aspectRatio(10 / 7f)
                }
                else -> {
                    Modifier.aspectRatio(7 / 10f)
                }
            }
            val content: @Composable LayoutType.() -> Unit = {
                AuthContent(
                    contentPadding = contentPadding,
                    subscriptionStatus = uiState.subscriptionStatus,
                    onNavigateToHome = onNavigateToHome,
                    googleAuthState = googleAuthState,
                )
            }

            if (windowSize.heightSizeClass == WindowHeightSizeClass.Compact) {
                Row(
                    modifier = contentLayoutModifier
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    LayoutType.Row(this).content()
                }
            } else {
                Column(
                    modifier = contentLayoutModifier
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    LayoutType.Column(this).content()
                }
            }
        }
    }
}

@Composable
private fun LayoutType.AuthContent(
    contentPadding: PaddingValues,
    subscriptionStatus: VpnGateSubscriptionStatus,
    onNavigateToHome: () -> Unit,
    googleAuthState: GoogleAuthState,
) {
    val commonModifier = if (isColumn()) with(scope) {
        Modifier
            .weight(1.0f, fill = false)
            .padding(horizontal = 16.dp)
    } else with(scope) {
        Modifier
            .weight(1.0f, fill = false)
            .padding(vertical = 16.dp)
    }

    Image(
        modifier = Modifier
            .size(240.dp)
            .then(commonModifier),
        colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.secondary),
        painter = painterResource(id = R.drawable.ic_round_key),
        contentDescription = null
    )

    Spacer(modifier = Modifier.size(24.dp))

    AnimatedContent(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .then(commonModifier),
        targetState = googleAuthState.authStatus,
        label = "auth_content"
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when (it) {
                AuthenticationStatus.InProgress, AuthenticationStatus.SignedOut -> {
                    DescriptionText(stringId = R.string.auth_explanation)
                    Button(
                        enabled = googleAuthState.authStatus != AuthenticationStatus.InProgress,
                        onClick = googleAuthState::signIn,
                    ) {
                        Text(
                            text = stringResource(id = R.string.sign_in)
                        )
                    }
                }
                AuthenticationStatus.PermissionsNotGranted -> {
                    DescriptionText(stringId = R.string.permission_rationals)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = googleAuthState::requestPermissions,
                        ) {
                            Text(
                                text = stringResource(id = R.string.request)
                            )
                        }
                        Button(
                            onClick = googleAuthState::signOut
                        ) {
                            Text(text = stringResource(id = R.string.sign_out))
                        }
                    }
                }
                AuthenticationStatus.SignedIn -> {
                    SignedInSubContent(
                        onNavigateToHome = onNavigateToHome,
                        onSignOut = googleAuthState::signOut,
                        subscriptionStatus = subscriptionStatus,
                    )
                }
                AuthenticationStatus.PreSignedIn -> {
                    LaunchedEffect(Unit) {
                        googleAuthState.signOut()
                    }
                }
            }
        }
    }
}

@Composable
private fun SignedInSubContent(
    onNavigateToHome: () -> Unit,
    onSignOut: () -> Unit,
    subscriptionStatus: VpnGateSubscriptionStatus,
) {
    AnimatedContent(subscriptionStatus, label = "signed-in") {
        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when (it) {
                VpnGateSubscriptionStatus.Unknown -> {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(32.dp)
                    )
                }

                VpnGateSubscriptionStatus.Is -> {
                    LaunchedEffect(Unit) {
                        onNavigateToHome()
                    }
                }

                VpnGateSubscriptionStatus.IsNot -> {
                    DescriptionText(stringId = R.string.not_being_subscribed_to_vpngate)
                    Button(
                        onClick = onSignOut
                    ) {
                        Text(text = stringResource(id = R.string.sign_out))
                    }
                }
            }
        }
    }
}

@Composable
private fun DescriptionText(
    @StringRes stringId: Int,
) {
    Text(
        modifier = Modifier.widthIn(max = 320.dp),
        text = stringResource(id = stringId),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onBackground
    )
}

private sealed interface LayoutType {
    data class Column(val scope: ColumnScope) : LayoutType
    data class Row(val scope: RowScope) : LayoutType
}

@OptIn(ExperimentalContracts::class)
private fun LayoutType.isColumn(): Boolean {
    contract {
        returns(true) implies (this@isColumn is LayoutType.Column)
        returns(false) implies (this@isColumn is LayoutType.Row)
    }
    return this is LayoutType.Column
}

@AuthPreviews.PreSignedIn
@Composable
private fun SignInScreenPreview_PreSignedIn() {
    SignInScreenPreview(
        uiState = AuthUiState(subscriptionStatus = VpnGateSubscriptionStatus.entries.random()),
        authenticationStatus = AuthenticationStatus.PreSignedIn,
    )
}

@AuthPreviews.SignedOut
@Composable
private fun SignInScreenPreview_SignedOut() {
    SignInScreenPreview(
        uiState = AuthUiState(subscriptionStatus = VpnGateSubscriptionStatus.entries.random()),
        authenticationStatus = AuthenticationStatus.SignedOut,
    )
}

@AuthPreviews.InProgress
@Composable
private fun SignInScreenPreview_InProgress() {
    SignInScreenPreview(
        uiState = AuthUiState(subscriptionStatus = VpnGateSubscriptionStatus.entries.random()),
        authenticationStatus = AuthenticationStatus.InProgress,
    )
}

@AuthPreviews.PermissionsNotGranted
@Composable
private fun SignInScreenPreview_PermissionsNotGranted() {
    SignInScreenPreview(
        uiState = AuthUiState(subscriptionStatus = VpnGateSubscriptionStatus.entries.random()),
        authenticationStatus = AuthenticationStatus.PermissionsNotGranted,
    )
}

@AuthPreviews.SignedIn
@Composable
private fun SignInScreenPreview_SignedIn(
    @PreviewParameter(VpnGateSubscriptionStatusParameterProvider::class) params: VpnGateSubscriptionStatus
) {
    SignInScreenPreview(
        uiState = AuthUiState(subscriptionStatus = params),
        authenticationStatus = AuthenticationStatus.SignedIn,
    )
}

@PreviewLightDarkLandscape
@Composable
private fun SignInScreenPreview_Landscape() {
    SignInScreenPreview(
        uiState = AuthUiState(subscriptionStatus = VpnGateSubscriptionStatus.entries.random()),
        authenticationStatus = AuthenticationStatus.SignedOut,
    )
}

@Composable
private fun SignInScreenPreview(uiState: AuthUiState, authenticationStatus: AuthenticationStatus) {
    BoxWithConstraints {
        NsmaVpnTheme {
            NsmaVpnBackground {
                val windowSize = WindowSizeClass.calculateFromSize(DpSize(maxWidth, maxHeight))
                AuthScreen(
                    uiState = uiState,
                    windowSize = windowSize,
                    googleAuthState = object : GoogleAuthState {
                        override val authStatus: AuthenticationStatus = authenticationStatus

                        override var onSignInResult: ((GoogleAccountSignInResult) -> Unit)? = null

                        override fun signIn() = Unit

                        override fun requestPermissions() = Unit

                        override fun signOut() = Unit
                    },
                )
            }
        }
    }
}
