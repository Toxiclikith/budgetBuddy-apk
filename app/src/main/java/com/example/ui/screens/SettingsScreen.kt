package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SettingsBackupRestore
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElectricTeal
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RubyRed
import com.example.ui.theme.SlateCardBg
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.FinanceViewModel
import java.util.Locale

@Composable
fun SettingsScreen(
    viewModel: FinanceViewModel
) {
    val currencySymbol by viewModel.currencySymbol.collectAsState()
    val dailyLimit by viewModel.dailySpendingLimit.collectAsState()
    val pinLockEnabled by viewModel.pinLockEnabled.collectAsState()
    val securityPin by viewModel.securityPin.collectAsState()

    var customLimitText by remember(dailyLimit) { mutableStateOf(dailyLimit.toString()) }
    var pinText by remember(securityPin) { mutableStateOf(securityPin) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .testTag("settings_screen")
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
            color = TextPrimary
        )
        Text(
            text = "Personalize your finance workspace",
            style = MaterialTheme.typography.bodySmall,
            color = ElectricTeal
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Currency Picker Group
        SettingsSectionHeader(title = "Currency Configuration", icon = Icons.Default.AttachMoney)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SlateCardBg)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Select Display Currency Symbol",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                val symbols = listOf("$", "€", "£", "₹", "¥", "₩", "₪", "₫")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    symbols.forEach { sym ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (currencySymbol == sym) ElectricTeal
                                    else SlateCardBg.copy(alpha = 0.4f)
                                )
                                .clickable { viewModel.setCurrencySymbol(sym) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = sym,
                                color = if (currencySymbol == sym) SlateCardBg else TextPrimary,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                var customSymbolText by remember(currencySymbol) { mutableStateOf(currencySymbol) }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = customSymbolText,
                        onValueChange = {
                            customSymbolText = it
                            if (it.isNotEmpty()) {
                                viewModel.setCurrencySymbol(it)
                            }
                        },
                        label = { Text("Custom Symbol (e.g. kr, AED, RM, Fr)", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricTeal,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    Button(
                        onClick = {
                            try {
                                val sym = java.util.Currency.getInstance(java.util.Locale.getDefault()).symbol
                                if (sym != null) {
                                    viewModel.setCurrencySymbol(sym)
                                }
                            } catch (e: Exception) {
                                viewModel.setCurrencySymbol("$")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricTeal.copy(alpha = 0.15f), contentColor = ElectricTeal)
                    ) {
                        Text("Auto-Detect", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Daily Limit Slider
        SettingsSectionHeader(title = "Daily Limit Alert Trigger", icon = Icons.Default.Notifications)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SlateCardBg)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daily Spending Limit",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = String.format(Locale.getDefault(), "%s%,.0f", currencySymbol, dailyLimit),
                        color = ElectricTeal,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Slider(
                    value = dailyLimit.toFloat(),
                    onValueChange = { viewModel.setDailySpendingLimit(it.toDouble()) },
                    valueRange = 10f..500f,
                    colors = SliderDefaults.colors(
                        thumbColor = ElectricTeal,
                        activeTrackColor = ElectricTeal,
                        inactiveTrackColor = SlateCardBg.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = customLimitText,
                    onValueChange = {
                        customLimitText = it
                        val parseVal = it.toDoubleOrNull()
                        if (parseVal != null && parseVal > 0.0) {
                            viewModel.setDailySpendingLimit(parseVal)
                        }
                    },
                    label = { Text("Custom Amount Limit", color = TextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricTeal,
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Security Lock Lock
        SettingsSectionHeader(title = "Biometrics & Security Locks", icon = Icons.Default.Security)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SlateCardBg)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Secure PIN Keypad Lock",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Lock app upon launch with a 4-digit PIN",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    Switch(
                        checked = pinLockEnabled,
                        onCheckedChange = { viewModel.setPinLockEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ElectricTeal,
                            checkedTrackColor = ElectricTeal.copy(alpha = 0.4f),
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = SlateCardBg.copy(alpha = 0.5f)
                        )
                    )
                }

                AnimatedVisibility(visible = pinLockEnabled) {
                    Column(modifier = Modifier.padding(top = 16.dp)) {
                        Divider(color = SlateCardBg.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = pinText,
                            onValueChange = {
                                if (it.length <= 4) {
                                    pinText = it
                                    if (it.length == 4) {
                                        viewModel.setSecurityPin(it)
                                    }
                                }
                            },
                            label = { Text("Set 4-Digit Passcode Pin", color = TextSecondary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricTeal,
                                unfocusedBorderColor = Color.Gray,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Device Migration and Backup Group
        SettingsSectionHeader(title = "Backup & Device Migration", icon = Icons.Default.SettingsBackupRestore)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SlateCardBg)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Transfer Data Between Devices",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "Copy this JSON backup on your old device, paste it below on your new device, and instantly restore all your transactions, budgets, goals, and bill trackers.",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                var backupJsonState by remember { mutableStateOf("") }
                var importJsonInput by remember { mutableStateOf("") }
                var statusMessage by remember { mutableStateOf("") }
                var showConfirmImportDialog by remember { mutableStateOf(false) }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.exportDataToJson { json ->
                                backupJsonState = json
                                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(json))
                                statusMessage = "Backup generated and copied to Clipboard!"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricTeal.copy(alpha = 0.15f), contentColor = ElectricTeal),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Export Backup")
                    }

                    Button(
                        onClick = {
                            if (importJsonInput.trim().isNotEmpty()) {
                                showConfirmImportDialog = true
                            } else {
                                statusMessage = "Please paste a backup JSON payload first."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen.copy(alpha = 0.15f), contentColor = EmeraldGreen),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Import Backup")
                    }
                }

                if (statusMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SlateCardBg.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = statusMessage,
                            color = ElectricTeal,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (backupJsonState.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Your Generated Backup Payload:",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = backupJsonState,
                        onValueChange = {},
                        readOnly = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricTeal,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = TextSecondary,
                            unfocusedTextColor = TextSecondary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .padding(top = 4.dp),
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Paste Backup Payload to Import:",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = importJsonInput,
                    onValueChange = { importJsonInput = it },
                    placeholder = { Text("Paste JSON backup code here...", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .padding(top = 4.dp),
                    textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                )

                if (showConfirmImportDialog) {
                    Dialog(onDismissRequest = { showConfirmImportDialog = false }) {
                        androidx.compose.material3.Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = SlateCardBg,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = "Confirm Data Restoration",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "WARNING: Restoring this backup will permanently overwrite your current transactions, budgets, goals, and bill records. This action cannot be undone.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = RubyRed
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(onClick = { showConfirmImportDialog = false }) {
                                        Text("Cancel", color = TextSecondary)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            showConfirmImportDialog = false
                                            viewModel.importDataFromJson(importJsonInput) { success, msg ->
                                                statusMessage = msg
                                                if (success) {
                                                    importJsonInput = ""
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                                    ) {
                                        Text("Restore Backup", color = SlateCardBg, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Developer / Testing Management Tools
        SettingsSectionHeader(title = "Database Administration", icon = Icons.Default.SettingsBackupRestore)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SlateCardBg)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                var showPurgeConfirm by remember { mutableStateOf(false) }
                var dbAdminMessage by remember { mutableStateOf("") }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Wipe Database Logs",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Purge all records (transactions, budgets, goals, streak)",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    Button(
                        onClick = { showPurgeConfirm = true },
                        colors = ButtonDefaults.buttonColors(containerColor = RubyRed.copy(alpha = 0.15f), contentColor = RubyRed)
                    ) {
                        Text("Purge DB")
                    }
                }

                Divider(color = SlateCardBg.copy(alpha = 0.5f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Initialize Premium Mock Data",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Instantly pre-populate database tables with standard metrics",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.addSampleData()
                            dbAdminMessage = "Demo transactions, budgets, goals, and monthly bills pre-populated successfully!"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricTeal.copy(alpha = 0.15f), contentColor = ElectricTeal)
                    ) {
                        Text("Populate")
                    }
                }

                if (dbAdminMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SlateCardBg.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = dbAdminMessage,
                            color = ElectricTeal,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (showPurgeConfirm) {
                    Dialog(onDismissRequest = { showPurgeConfirm = false }) {
                        androidx.compose.material3.Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = SlateCardBg,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = "Confirm Full Reset",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Are you absolutely sure you want to wipe the local database? This will permanently delete all transitions, budgets, goals, and monthly bills.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(onClick = { showPurgeConfirm = false }) {
                                        Text("Cancel", color = TextSecondary)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            viewModel.clearAllData()
                                            dbAdminMessage = "All database records have been purged successfully."
                                            showPurgeConfirm = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = RubyRed)
                                    ) {
                                        Text("Wipe Everything", color = SlateCardBg, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Product info
        val uriHandler = LocalUriHandler.current
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SlateCardBg.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "About",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "BudgetBuddy Version 1.0.0 (Release build). Strictly licensed under open-source MIT guidelines. Encrypted and compiled locally.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                val annotatedLinkString = buildAnnotatedString {
                    append("Developed by ")
                    withStyle(style = SpanStyle(
                        color = ElectricTeal,
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline
                    )) {
                        append("toxiclikith")
                    }
                    append(" | Portfolio")
                }

                Text(
                    text = annotatedLinkString,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            uriHandler.openUri("https://toxiclikith.github.io/portfolio/")
                        },
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = TextSecondary
        )
    }
}
