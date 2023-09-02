@file:OptIn(ExperimentalMaterial3Api::class)

package ir.erfansn.nsmavpn.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.lerp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.erfansn.nsmavpn.BuildConfig
import ir.erfansn.nsmavpn.R
import ir.erfansn.nsmavpn.data.model.ThemeMode
import ir.erfansn.nsmavpn.ui.component.NsmaVpnBackground
import ir.erfansn.nsmavpn.ui.component.NsmaVpnLargeTopBar
import ir.erfansn.nsmavpn.ui.component.NsmaVpnScaffold
import ir.erfansn.nsmavpn.ui.theme.NsmaVpnTheme
import ir.erfansn.nsmavpn.ui.util.preview.ThemePreviews

@Composable
fun SettingsRoute(
    onNavigateToBack: () -> Unit,
    onNavigateToTunnelSplitting: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SettingsScreen(
        modifier = modifier,
        uiState = uiState,
        onChangeThemeMode = viewModel::updateThemeMode,
        onNavigateToBack = onNavigateToBack,
        onNavigateToTunnelSplitting = onNavigateToTunnelSplitting,
    )
}

@Composable
private fun SettingsScreen(
    uiState: SettingsUiState,
    onChangeThemeMode: (ThemeMode) -> Unit,
    onNavigateToBack: () -> Unit,
    onNavigateToTunnelSplitting: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    NsmaVpnScaffold(
        modifier = modifier,
        topBar = {
            NsmaVpnLargeTopBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.title_settings),
                        style = lerp(
                            start = LocalTextStyle.current,
                            stop = MaterialTheme.typography.displayMedium,
                            fraction = 1f - scrollBehavior.state.collapsedFraction
                        ),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateToBack) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = stringResource(id = R.string.cd_back)
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        scrollBehavior = scrollBehavior,
    ) {
        val context = LocalContext.current
        val uriHandler = LocalUriHandler.current
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = it,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Spacer(modifier = Modifier.padding(top = 12.dp))
            }
            item {
                ThemeModeItem(
                    themeMode = uiState.themeMode,
                    onChangeThemeMode = onChangeThemeMode
                )
            }
            item {
                SettingsItem(
                    title = stringResource(R.string.title_split_tunneling),
                    supporting = stringResource(R.string.desc_split_tunneling),
                    onClick = onNavigateToTunnelSplitting
                )
            }
            item {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
            item {
                SettingsItem(
                    title = stringResource(R.string.title_bug_reports),
                    onClick = {
                        uriHandler.openUri(context.getString(R.string.link_feedback))
                    }
                )
            }
            item {
                SettingsItem(
                    title = stringResource(R.string.title_licence),
                    onClick = {
                        uriHandler.openUri(context.getString(R.string.link_licences))
                    }
                )
            }
            item {
                Spacer(modifier = Modifier.height(2.dp))
            }
            item {
                SettingsItem(
                    title = stringResource(R.string.app_version),
                    supporting = BuildConfig.VERSION_NAME,
                    enabled = false,
                )
            }
            item {
                Spacer(modifier = Modifier.padding(top = 12.dp))
            }
        }
    }
}

@Composable
private fun ThemeModeItem(
    themeMode: ThemeMode,
    onChangeThemeMode: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .heightIn(min = 56.dp)
                .padding(vertical = 8.dp)
                .padding(start = 16.dp, end = 24.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                text = stringResource(id = R.string.theme_mode),
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        }
        HorizontalDivider(
            color = MaterialTheme.colorScheme.surface,
        )
        ThemeModeOptions(
            themeMode = themeMode,
            onChangeThemeMode = onChangeThemeMode,
        )
    }
}

@Composable
private fun ThemeModeOptions(
    themeMode: ThemeMode,
    onChangeThemeMode: (mode: ThemeMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .selectableGroup()
            .padding(vertical = 8.dp)
    ) {
        SelectiveRow(
            text = stringResource(R.string.theme_mode_default),
            selected = themeMode == ThemeMode.SYSTEM,
            onSelect = { onChangeThemeMode(ThemeMode.SYSTEM) }
        )
        SelectiveRow(
            text = stringResource(R.string.theme_mode_light),
            selected = themeMode == ThemeMode.LIGHT,
            onSelect = { onChangeThemeMode(ThemeMode.LIGHT) }
        )
        SelectiveRow(
            text = stringResource(R.string.theme_mode_dark),
            selected = themeMode == ThemeMode.DARK,
            onSelect = { onChangeThemeMode(ThemeMode.DARK) }
        )
    }
}

@Composable
private fun SelectiveRow(
    text: String,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onSelect,
            )
            .padding(8.dp)
            .padding(start = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun SettingsItem(
    title: String,
    modifier: Modifier = Modifier,
    supporting: String? = null,
    onClick: () -> Unit = { },
    enabled: Boolean = true,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        enabled = enabled,
    ) {
        Column(
            modifier = Modifier
                .heightIn(min = 56.dp)
                .padding(vertical = 8.dp)
                .padding(start = 16.dp, end = 24.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            supporting?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}

@ThemePreviews
@Composable
private fun SettingsDialogPreview() {
    NsmaVpnTheme {
        NsmaVpnBackground {
            SettingsScreen(
                uiState = SettingsUiState(
                    themeMode = ThemeMode.SYSTEM,
                ),
                onChangeThemeMode = { },
                onNavigateToBack = { },
                onNavigateToTunnelSplitting = { }
            )
        }
    }
}