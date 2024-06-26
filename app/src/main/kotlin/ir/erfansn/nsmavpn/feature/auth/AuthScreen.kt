/*
 * Copyright 2024 Erfan Sn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:OptIn(ExperimentalMaterial3WindowSizeClassApi::class)

package ir.erfansn.nsmavpn.feature.auth

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import ir.erfansn.nsmavpn.R
import ir.erfansn.nsmavpn.core.AndroidString
import ir.erfansn.nsmavpn.feature.auth.google.AuthenticationStatus
import ir.erfansn.nsmavpn.feature.auth.google.GoogleAuthState
import ir.erfansn.nsmavpn.ui.component.NsmaVpnBackground
import ir.erfansn.nsmavpn.ui.component.NsmaVpnScaffold
import ir.erfansn.nsmavpn.ui.theme.NsmaVpnTheme
import ir.erfansn.nsmavpn.ui.util.preview.AuthStates
import ir.erfansn.nsmavpn.ui.util.preview.PreviewLightDarkLandscape
import ir.erfansn.nsmavpn.ui.util.preview.parameter.VpnGateSubscriptionStatusParameterProvider
import ir.erfansn.nsmavpn.ui.util.rememberUserMessageNotifier
import kotlinx.coroutines.launch
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@Composable
fun AuthRoute(
    windowSize: WindowSizeClass,
    onNavigateToHome: () -> Unit,
    googleAuthState: GoogleAuthState,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AuthScreen(
        modifier = modifier,
        uiState = uiState,
        windowSize = windowSize,
        onSuccessfulSignIn = viewModel::verifyVpnGateSubscription,
        onErrorShown = viewModel::notifyMessageShown,
        onNavigateToHome = onNavigateToHome,
        googleAuthState = googleAuthState,
    )
}

@Composable
private fun AuthScreen(
    windowSize: WindowSizeClass,
    uiState: AuthUiState,
    googleAuthState: GoogleAuthState,
    modifier: Modifier = Modifier,
    onSuccessfulSignIn: (GoogleSignInAccount) -> Unit = { },
    onErrorShown: () -> Unit = { },
    onNavigateToHome: () -> Unit = { },
) {
    val coroutineScope = rememberCoroutineScope()
    val userNotifier = rememberUserMessageNotifier()

    DisposableEffect(googleAuthState) {
        googleAuthState.onErrorOccur = {
            coroutineScope.launch {
                userNotifier.showMessage(
                    messageId = it.id,
                    actionLabelId = R.string.ok,
                )
            }
        }
        onDispose {
            googleAuthState.onErrorOccur = null
        }
    }

    LaunchedEffect(uiState.errorMessage, googleAuthState, userNotifier, onErrorShown) {
        if (uiState.errorMessage != null) {
            googleAuthState.signOut()
            userNotifier.showMessage(
                messageId = uiState.errorMessage.id,
                actionLabelId = R.string.ok
            )
            onErrorShown()
        }
    }

    NsmaVpnScaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = userNotifier.snackbarHostState) }
    ) { contentPadding ->
        Box(
            modifier = Modifier.fillMaxSize().consumeWindowInsets(contentPadding),
            contentAlignment = Alignment.Center,
        ) {
            val contentLayoutModifier = Modifier.fillMaxSize()
            val content: @Composable LayoutType.() -> Unit = {
                AuthContent(
                    contentPadding = contentPadding,
                    subscriptionStatus = uiState.subscriptionStatus,
                    onNavigateToHome = onNavigateToHome,
                    onSuccessfulSignIn = onSuccessfulSignIn,
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
    onSuccessfulSignIn: (GoogleSignInAccount) -> Unit,
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
        painter = painterResource(id = R.drawable.round_key_24),
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
                        enabled = it != AuthenticationStatus.InProgress,
                        onClick = googleAuthState::signIn,
                        modifier = Modifier.testTag("sign_in"),
                    ) {
                        Text(
                            text = stringResource(id = R.string.sign_in)
                        )
                    }
                }
                AuthenticationStatus.PermissionsNotGranted -> {
                    DescriptionText(stringId = R.string.permission_rationals)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
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
                is AuthenticationStatus.SignedIn -> {
                    LaunchedEffect(onSuccessfulSignIn) {
                        onSuccessfulSignIn(it.account)
                    }

                    AnimatedContent(subscriptionStatus, label = "signed-in") { subscriptionStatus ->
                        when (subscriptionStatus) {
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
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    DescriptionText(stringId = R.string.not_being_subscribed_to_vpngate)
                                    Button(
                                        onClick = googleAuthState::signOut
                                    ) {
                                        Text(text = stringResource(id = R.string.sign_out))
                                    }
                                }
                            }
                        }
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

@AuthStates.PreviewSignedOut
@Composable
private fun AuthScreenPreview_SignedOut() {
    AuthScreenPreview(
        uiState = AuthUiState(subscriptionStatus = VpnGateSubscriptionStatus.entries.random()),
        authenticationStatus = AuthenticationStatus.SignedOut,
    )
}

@AuthStates.PreviewInProgress
@Composable
private fun AuthScreenPreview_InProgress() {
    AuthScreenPreview(
        uiState = AuthUiState(subscriptionStatus = VpnGateSubscriptionStatus.entries.random()),
        authenticationStatus = AuthenticationStatus.InProgress,
    )
}

@AuthStates.PreviewPermissionsNotGranted
@Composable
private fun AuthScreenPreview_PermissionsNotGranted() {
    AuthScreenPreview(
        uiState = AuthUiState(subscriptionStatus = VpnGateSubscriptionStatus.entries.random()),
        authenticationStatus = AuthenticationStatus.PermissionsNotGranted,
    )
}

@AuthStates.PreviewSignedIn
@Composable
private fun AuthScreenPreview_SignedIn(
    @PreviewParameter(VpnGateSubscriptionStatusParameterProvider::class) params: VpnGateSubscriptionStatus
) {
    AuthScreenPreview(
        uiState = AuthUiState(subscriptionStatus = params),
        authenticationStatus = AuthenticationStatus.SignedIn(GoogleSignInAccount.createDefault()),
    )
}

@PreviewLightDarkLandscape
@Composable
private fun AuthScreenPreview_Landscape() {
    AuthScreenPreview(
        uiState = AuthUiState(subscriptionStatus = VpnGateSubscriptionStatus.entries.random()),
        authenticationStatus = AuthenticationStatus.SignedOut,
    )
}

@Composable
private fun AuthScreenPreview(uiState: AuthUiState, authenticationStatus: AuthenticationStatus) {
    BoxWithConstraints {
        NsmaVpnTheme {
            NsmaVpnBackground {
                val windowSize = WindowSizeClass.calculateFromSize(DpSize(maxWidth, maxHeight))
                AuthScreen(
                    uiState = uiState,
                    windowSize = windowSize,
                    googleAuthState = object : GoogleAuthState {
                        override val authStatus: AuthenticationStatus = authenticationStatus

                        override var onErrorOccur: ((AndroidString) -> Unit)? = { }

                        override fun signIn() = Unit

                        override fun requestPermissions() = Unit

                        override fun signOut() = Unit
                    },
                )
            }
        }
    }
}
