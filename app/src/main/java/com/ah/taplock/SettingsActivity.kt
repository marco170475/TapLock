package com.ah.taplock

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.ah.taplock.ui.theme.TapLockTheme
import androidx.core.content.edit

class SettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TapLockTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TapLockScreen()
                }
            }
        }
    }

}

@Composable
fun TapLockScreen() {
    val context = LocalContext.current
    val githubUrl = stringResource(R.string.github_url)
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    var showDialog by remember { mutableStateOf(false) } //

    var isAccessibilityEnabled by remember {
        mutableStateOf(isAccessibilityEnabled(context))
    }

    var timeoutValue by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        if (!isAccessibilityEnabled) {
            showDialog = true
        }

        timeoutValue = context
            .getSharedPreferences(R.string.shared_pref_name.toString(), Context.MODE_PRIVATE)
            .getInt(context.getString(R.string.double_tap_timeout), 300)
            .toString()
    }

    val uriHandler = LocalUriHandler.current
    val disclaimerString = buildAnnotatedString {
        append(stringResource(R.string.home_screen_disclaimer))
        append(" ")
        pushStringAnnotation(tag="URL", annotation = githubUrl)
        withStyle(style = SpanStyle(
            textDecoration = TextDecoration.Underline
        )) {
            append(githubUrl)
        }
        pop()
    }

    DisposableEffect(Unit) {
        val lifecycleOwner = context as ComponentActivity
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isAccessibilityEnabled = isAccessibilityEnabled(context)

                showDialog = !isAccessibilityEnabled
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(8.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    stringResource(R.string.home_screen_description),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    stringResource(R.string.home_screen_instructions),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    disclaimerString,
                    modifier = Modifier.clickable {
                        uriHandler.openUri(githubUrl)
                    },
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.settings_access_instructions),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }


        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    stringResource(R.string.required_permissions),
                    style = MaterialTheme.typography.titleMedium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.accessibility_service), modifier = Modifier.weight(1f))
                    Button(
                        onClick = { showDialog = true },
                        enabled = !isAccessibilityEnabled,
                    ) {
                        Text(text = if (isAccessibilityEnabled) stringResource(R.string.enabled) else stringResource(R.string.enable))
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    stringResource(R.string.settings_label),
                    style = MaterialTheme.typography.titleMedium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val timeoutInt = timeoutValue.toIntOrNull()
                    val isValid = timeoutInt != null && timeoutInt in 100..500

                    TextField(
                        label = { Text(stringResource(R.string.timeout_label)) },
                        value = timeoutValue,
                        onValueChange = { newValue: String ->
                            timeoutValue = newValue
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester),
                        trailingIcon = {
                            if (timeoutValue.isNotEmpty()) {
                                IconButton(onClick = { timeoutValue = "" }) {
                                    Icon(Icons.Filled.Clear, contentDescription = stringResource(R.string.clear_value))
                                }
                            }
                        },
                    )

                    Button(
                        enabled = isValid,
                        onClick = {
                            context.getSharedPreferences(R.string.shared_pref_name.toString(), Context.MODE_PRIVATE)
                                .edit {
                                    putInt(
                                        context.getString(R.string.double_tap_timeout),
                                        timeoutInt!!
                                    )
                                }
                            Toast.makeText(context, context.getString(R.string.timeout_updated), Toast.LENGTH_SHORT).show()
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        }
                    ) {
                        Text(stringResource(R.string.update))
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Vibration Settings",
                    style = MaterialTheme.typography.titleMedium
                )

                val prefs = context.getSharedPreferences(
                    R.string.shared_pref_name.toString(),
                    Context.MODE_PRIVATE
                )

                var vibrateWidget by remember { mutableStateOf(prefs.getBoolean("vibrate_widget", false)) }
                var vibrateTile by remember { mutableStateOf(prefs.getBoolean("vibrate_tile", false)) }
                var vibrateLauncher by remember { mutableStateOf(prefs.getBoolean("vibrate_launcher", false)) }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.pref_vibrate_widget), modifier = Modifier.weight(1f))
                    androidx.compose.material3.Switch(
                        checked = vibrateWidget,
                        onCheckedChange = {
                            vibrateWidget = it
                            prefs.edit { putBoolean("vibrate_widget", it) }
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.pref_vibrate_tile), modifier = Modifier.weight(1f))
                    androidx.compose.material3.Switch(
                        checked = vibrateTile,
                        onCheckedChange = {
                            vibrateTile = it
                            prefs.edit { putBoolean("vibrate_tile", it) }
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.pref_vibrate_launcher), modifier = Modifier.weight(1f))
                    androidx.compose.material3.Switch(
                        checked = vibrateLauncher,
                        onCheckedChange = {
                            vibrateLauncher = it
                            prefs.edit { putBoolean("vibrate_launcher", it) }
                        }
                    )
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(stringResource(R.string.accessibility_permission_title)) },
                text = { Text(stringResource(R.string.accessibility_permission_description)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDialog = false
                            context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                        }
                    ) {
                        Text(stringResource(R.string.agree))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDialog = false }
                    ) {
                        Text(stringResource(R.string.not_now))
                    }
                }
            )
        }
    }
}