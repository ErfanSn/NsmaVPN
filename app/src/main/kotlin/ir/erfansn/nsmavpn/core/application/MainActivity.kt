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
package ir.erfansn.nsmavpn.core.application

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.ReportDrawnWhen
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import ir.erfansn.nsmavpn.data.model.Configurations
import ir.erfansn.nsmavpn.data.repository.UserProfileRepository
import ir.erfansn.nsmavpn.data.util.NetworkMonitor
import ir.erfansn.nsmavpn.navigation.NsmaVpnNavHost
import ir.erfansn.nsmavpn.ui.component.NsmaVpnBackground
import ir.erfansn.nsmavpn.ui.theme.NsmaVpnTheme
import ir.erfansn.nsmavpn.ui.theme.isSupportDynamicScheme
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject lateinit var networkMonitor: NetworkMonitor

    @Inject lateinit var userProfileRepository: UserProfileRepository

    private val viewModel by viewModels<MainActivityViewModel>()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)

        var uiState: MainActivityUiState by mutableStateOf(MainActivityUiState.Loading)
        var isBackgroundDrawn by mutableStateOf(false)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState
                    .collect {
                        uiState = it
                    }
            }
        }
        splash.setKeepOnScreenCondition {
            uiState == MainActivityUiState.Loading || !isBackgroundDrawn
        }

        enableEdgeToEdge()

        setContent {
            // Equals with TTID because of blocking the frame rendering by Splash
            ReportDrawnWhen {
                uiState is MainActivityUiState.Success && isBackgroundDrawn
            }

            shouldUseDarkTheme(uiState).let { useDarkTheme ->
                LaunchedEffect(useDarkTheme) {
                    val transparentStyle = SystemBarStyle.auto(
                        Color.TRANSPARENT,
                        Color.TRANSPARENT,
                        detectDarkMode = { useDarkTheme }
                    )
                    enableEdgeToEdge(
                        statusBarStyle = transparentStyle,
                        navigationBarStyle = transparentStyle
                    )
                }
            }
            NsmaVpnTheme(
                darkTheme = shouldUseDarkTheme(uiState),
                dynamicColor = shouldUseDynamicColor(uiState)
            ) {
                NsmaVpnApp(
                    onResetApp = viewModel::resetApp,
                    networkMonitor = networkMonitor,
                    windowSize = calculateWindowSizeClass(activity = this),
                    isCompletedAuthFlow = userProfileRepository::isUserProfileSaved,
                    onBackgroundDrawn = {
                        isBackgroundDrawn = true
                    }
                )
            }
        }
    }
}

@Composable
@ReadOnlyComposable
private fun shouldUseDarkTheme(uiState: MainActivityUiState): Boolean {
    return when (uiState) {
        MainActivityUiState.Loading -> isSystemInDarkTheme()
        is MainActivityUiState.Success -> when (uiState.themeMode) {
            Configurations.ThemeMode.System -> isSystemInDarkTheme()
            Configurations.ThemeMode.Light -> false
            Configurations.ThemeMode.Dark -> true
        }
    }
}

@Composable
@ReadOnlyComposable
private fun shouldUseDynamicColor(uiState: MainActivityUiState): Boolean {
    return when (uiState) {
        MainActivityUiState.Loading -> isSupportDynamicScheme()
        is MainActivityUiState.Success -> uiState.isEnableDynamicScheme
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun NsmaVpnApp(
    onResetApp: () -> Unit,
    networkMonitor: NetworkMonitor,
    windowSize: WindowSizeClass,
    isCompletedAuthFlow: suspend () -> Boolean,
    onBackgroundDrawn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NsmaVpnBackground(
        modifier = modifier.semantics { testTagsAsResourceId = true },
        onDrawn = onBackgroundDrawn
    ) {
        NsmaVpnNavHost(
            networkMonitor = networkMonitor,
            windowSize = windowSize,
            onResetApp = onResetApp,
            isCompletedAuthFlow = isCompletedAuthFlow,
        )
    }
}
