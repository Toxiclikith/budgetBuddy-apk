package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.example.data.model.Bill
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.ElectricTeal
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RubyRed
import com.example.ui.theme.SlateCardBg
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.viewmodel.FinanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillsScreen(
    viewModel: FinanceViewModel
) {
    val bills by viewModel.allBills.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedBillForEdit by remember { mutableStateOf<Bill?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = ElectricTeal,
                contentColor = SlateCardBg,
                modifier = Modifier.testTag("add_bill_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Bill Reminder")
            }
        },
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Text(
                text = "Bills & Subscriptions",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = TextPrimary,
                modifier = Modifier.padding(24.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Monthly Bill Notification Center
                item {
                    val now = System.currentTimeMillis()
                    val upcomingNotifications = bills.filter { !it.isPaid && it.isNotificationEnabled && ((it.dueDate - now) / (24L * 60 * 60 * 1000)).toInt() <= 5 }
                    var demoAlertText by remember { mutableStateOf("") }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = SlateCardBg.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Alerts Center",
                                        tint = if (upcomingNotifications.isNotEmpty()) AmberWarning else ElectricTeal,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Bill Notification Center",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }

                                if (upcomingNotifications.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .background(RubyRed.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "${upcomingNotifications.size} Pending",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = RubyRed,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            if (upcomingNotifications.isNotEmpty()) {
                                Text(
                                    text = "The following monthly bills have reminder alerts scheduled and are due soon:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                upcomingNotifications.forEach { bill ->
                                    val daysLeft = ((bill.dueDate - now) / (24L * 60 * 60 * 1000)).toInt()
                                    val dueLabel = if (daysLeft <= 0) "Today!" else "in $daysLeft days"
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .background(AmberWarning, shape = RoundedCornerShape(3.dp))
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = bill.name,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextPrimary,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                        Text(
                                            text = "Due $dueLabel (${String.format(Locale.getDefault(), "%s%,.2f", currencySymbol, bill.amount)})",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = AmberWarning,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    text = "All monthly bill notifications are up-to-date. No upcoming due dates within 5 days with alerts configured.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    onClick = {
                                        if (bills.isNotEmpty()) {
                                            val randomBill = bills.random()
                                            demoAlertText = "🔔 BudgetBuddy Bill Alert: Your '${randomBill.name}' payment of ${currencySymbol}${String.format(Locale.getDefault(), "%,.2f", randomBill.amount)} is due shortly! Make sure notifications are active."
                                        } else {
                                            demoAlertText = "🔔 BudgetBuddy Bill Alert: No custom bill triggers yet. Create a bill tracker below to preview notification triggers!"
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ElectricTeal.copy(alpha = 0.15f), contentColor = ElectricTeal),
                                    modifier = Modifier.height(32.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp)
                                ) {
                                    Text("Test Notification Alert", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (demoAlertText.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = demoAlertText,
                                            color = ElectricTeal,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(
                                            onClick = { demoAlertText = "" },
                                            modifier = Modifier.size(16.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Dismiss",
                                                tint = EmeraldGreen,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                items(bills, key = { it.id }) { bill ->
                    BillReminderCard(
                        bill = bill,
                        currencySymbol = currencySymbol,
                        onPaidToggle = { viewModel.toggleBillPaid(bill) },
                        onNotificationToggle = { viewModel.toggleBillNotification(bill) },
                        onDeleteClick = { viewModel.deleteBill(bill.id) },
                        onRowClick = { selectedBillForEdit = bill }
                    )
                }

                if (bills.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No active recurring bills or Netflix subscriptions tracked.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            AddBillDialog(
                onDismiss = { showAddDialog = false },
                onSave = { name, amount, category, frequency, notify, dueDate ->
                    viewModel.insertBill(
                        Bill(
                            name = name,
                            amount = amount,
                            category = category,
                            dueDate = dueDate,
                            isRecurring = true,
                            isPaid = false,
                            frequency = frequency,
                            isNotificationEnabled = notify
                        )
                    )
                    showAddDialog = false
                }
            )
        }

        // Edit Bill Dialog
        selectedBillForEdit?.let { bill ->
            EditBillDialog(
                bill = bill,
                onDismiss = { selectedBillForEdit = null },
                onSave = { updatedBill ->
                    viewModel.updateBill(updatedBill)
                    selectedBillForEdit = null
                }
            )
        }
    }
}

@Composable
fun BillReminderCard(
    bill: Bill,
    currencySymbol: String,
    onPaidToggle: () -> Unit,
    onNotificationToggle: () -> Unit,
    onDeleteClick: () -> Unit,
    onRowClick: () -> Unit
) {
    val now = System.currentTimeMillis()
    val isOverdue = !bill.isPaid && bill.dueDate < now
    val daysLeft = ((bill.dueDate - now) / (24L * 60 * 60 * 1000)).toInt()
 
    val statusColor = when {
        bill.isPaid -> EmeraldGreen
        isOverdue -> RubyRed
        daysLeft <= 3 -> AmberWarning
        else -> ElectricTeal
    }
 
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onRowClick() },
        colors = CardDefaults.cardColors(containerColor = SlateCardBg),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(statusColor.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (bill.isPaid) Icons.Default.CheckCircle else if (isOverdue) Icons.Default.ErrorOutline else Icons.Default.EventNote,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
 
                    Spacer(modifier = Modifier.width(12.dp))
 
                    Column {
                        Text(
                            text = bill.name,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = "${bill.category} • ${bill.frequency}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
 
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onNotificationToggle) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Toggle Notification Reminder",
                            tint = if (bill.isNotificationEnabled) ElectricTeal else TextSecondary.copy(alpha = 0.4f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Bill",
                            tint = RubyRed.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
 
            Spacer(modifier = Modifier.height(16.dp))
 
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = String.format(Locale.getDefault(), "%s%,.2f", currencySymbol, bill.amount),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
 
                    Spacer(modifier = Modifier.height(4.dp))
 
                    val sdf = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }
                    val exactDateStr = sdf.format(Date(bill.dueDate))
                    val dueText = when {
                        bill.isPaid -> "Paid (Due $exactDateStr)"
                        isOverdue -> "Overdue by ${-daysLeft} days ($exactDateStr)"
                        daysLeft == 0 -> "Due today! ($exactDateStr)"
                        daysLeft == 1 -> "Due tomorrow ($exactDateStr)"
                        else -> "Due in $daysLeft days ($exactDateStr)"
                    }
                    Text(
                        text = dueText,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = statusColor
                    )
                }
 
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (bill.isPaid) "Mark Paid" else "Mark Unpaid",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = bill.isPaid,
                        onCheckedChange = { onPaidToggle() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = EmeraldGreen,
                            checkedTrackColor = EmeraldGreen.copy(alpha = 0.4f),
                            uncheckedThumbColor = TextTertiary,
                            uncheckedTrackColor = SlateCardBg.copy(alpha = 0.5f)
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBillDialog(
    onDismiss: () -> Unit,
    onSave: (String, Double, String, String, Boolean, Long) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Rent") }
    var selectedFrequency by remember { mutableStateOf("Monthly") }
    var isNotificationEnabled by remember { mutableStateOf(true) }
    var selectedTimestamp by remember { mutableStateOf(System.currentTimeMillis()) }
 
    val categories = listOf("Rent", "Electricity", "Internet", "Water", "Phone", "Netflix", "Spotify", "Insurance", "Subscription", "Custom")
    val frequencies = listOf("Monthly", "Weekly", "Yearly")
 
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SlateCardBg,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Track Bill / Subscription",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
 
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Provider/Service Name (Optional)", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricTeal,
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
 
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Bill Cost", color = TextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricTeal,
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
 
                // Category selection
                var catExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = catExpanded,
                    onExpandedChange = { catExpanded = !catExpanded }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = selectedCategory,
                        onValueChange = {},
                        label = { Text("Category", color = TextSecondary) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catExpanded) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricTeal,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = catExpanded,
                        onDismissRequest = { catExpanded = false },
                        modifier = Modifier.background(SlateCardBg)
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat, color = TextPrimary) },
                                onClick = {
                                    selectedCategory = cat
                                    catExpanded = false
                                }
                            )
                        }
                    }
                }
 
                // Frequency selection
                var freqExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = freqExpanded,
                    onExpandedChange = { freqExpanded = !freqExpanded }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = selectedFrequency,
                        onValueChange = {},
                        label = { Text("Frequency", color = TextSecondary) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = freqExpanded) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricTeal,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = freqExpanded,
                        onDismissRequest = { freqExpanded = false },
                        modifier = Modifier.background(SlateCardBg)
                    ) {
                        frequencies.forEach { freq ->
                            DropdownMenuItem(
                                text = { Text(freq, color = TextPrimary) },
                                onClick = {
                                    selectedFrequency = freq
                                    freqExpanded = false
                                }
                            )
                        }
                    }
                }

                // Date Selection Wrapper
                val context = LocalContext.current
                val dateSdf = remember { SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()) }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val calendar = Calendar.getInstance()
                            calendar.timeInMillis = selectedTimestamp
                            android.app.DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    val selectedCal = Calendar.getInstance()
                                    selectedCal.set(Calendar.YEAR, year)
                                    selectedCal.set(Calendar.MONTH, month)
                                    selectedCal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                    selectedTimestamp = selectedCal.timeInMillis
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }
                ) {
                    OutlinedTextField(
                        value = dateSdf.format(Date(selectedTimestamp)),
                        onValueChange = {},
                        enabled = false,
                        label = { Text("Bill Due Date", color = TextSecondary) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.EventNote,
                                contentDescription = "Select Date",
                                tint = ElectricTeal
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = TextPrimary,
                            disabledBorderColor = Color.Gray,
                            disabledLabelColor = TextSecondary,
                            disabledLeadingIconColor = ElectricTeal
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
 
                // Notification toggle Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Enable Alerts", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Text("Notify me before this bill is due", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(
                        checked = isNotificationEnabled,
                        onCheckedChange = { isNotificationEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ElectricTeal,
                            checkedTrackColor = ElectricTeal.copy(alpha = 0.4f),
                            uncheckedThumbColor = TextTertiary,
                            uncheckedTrackColor = SlateCardBg.copy(alpha = 0.5f)
                        )
                    )
                }
 
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amtVal = amount.toDoubleOrNull() ?: 0.0
                            if (amtVal > 0.0) {
                                onSave(name.ifEmpty { "Untitled Bill" }, amtVal, selectedCategory, selectedFrequency, isNotificationEnabled, selectedTimestamp)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricTeal, contentColor = SlateCardBg),
                        enabled = amount.isNotEmpty() && (amount.toDoubleOrNull() ?: 0.0) > 0.0
                    ) {
                        Text("Save Tracker", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBillDialog(
    bill: Bill,
    onDismiss: () -> Unit,
    onSave: (Bill) -> Unit
) {
    var name by remember { mutableStateOf(bill.name) }
    var amount by remember { mutableStateOf(bill.amount.toString()) }
    var selectedCategory by remember { mutableStateOf(bill.category) }
    var selectedFrequency by remember { mutableStateOf(bill.frequency) }
    var isNotificationEnabled by remember { mutableStateOf(bill.isNotificationEnabled) }
    var selectedTimestamp by remember { mutableStateOf(bill.dueDate) }

    val categories = listOf("Rent", "Electricity", "Internet", "Water", "Phone", "Netflix", "Spotify", "Insurance", "Subscription", "Custom")
    val frequencies = listOf("Monthly", "Weekly", "Yearly")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SlateCardBg,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Modify Bill / Subscription",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Provider/Service Name (Optional)", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricTeal,
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Bill Cost", color = TextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricTeal,
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Category selection
                var catExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = catExpanded,
                    onExpandedChange = { catExpanded = !catExpanded }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = selectedCategory,
                        onValueChange = {},
                        label = { Text("Category", color = TextSecondary) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catExpanded) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricTeal,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = catExpanded,
                        onDismissRequest = { catExpanded = false },
                        modifier = Modifier.background(SlateCardBg)
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat, color = TextPrimary) },
                                onClick = {
                                    selectedCategory = cat
                                    catExpanded = false
                                }
                            )
                        }
                    }
                }

                // Frequency selection
                var freqExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = freqExpanded,
                    onExpandedChange = { freqExpanded = !freqExpanded }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = selectedFrequency,
                        onValueChange = {},
                        label = { Text("Frequency", color = TextSecondary) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = freqExpanded) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricTeal,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = freqExpanded,
                        onDismissRequest = { freqExpanded = false },
                        modifier = Modifier.background(SlateCardBg)
                    ) {
                        frequencies.forEach { freq ->
                            DropdownMenuItem(
                                text = { Text(freq, color = TextPrimary) },
                                onClick = {
                                    selectedFrequency = freq
                                    freqExpanded = false
                                }
                            )
                        }
                    }
                }

                // Date Selection Wrapper
                val context = LocalContext.current
                val dateSdf = remember { SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()) }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val calendar = Calendar.getInstance()
                            calendar.timeInMillis = selectedTimestamp
                            android.app.DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    val selectedCal = Calendar.getInstance()
                                    selectedCal.set(Calendar.YEAR, year)
                                    selectedCal.set(Calendar.MONTH, month)
                                    selectedCal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                    selectedTimestamp = selectedCal.timeInMillis
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }
                ) {
                    OutlinedTextField(
                        value = dateSdf.format(Date(selectedTimestamp)),
                        onValueChange = {},
                        enabled = false,
                        label = { Text("Bill Due Date", color = TextSecondary) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.EventNote,
                                contentDescription = "Select Date",
                                tint = ElectricTeal
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = TextPrimary,
                            disabledBorderColor = Color.Gray,
                            disabledLabelColor = TextSecondary,
                            disabledLeadingIconColor = ElectricTeal
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Notification toggle Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Enable Alerts", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Text("Notify me before this bill is due", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(
                        checked = isNotificationEnabled,
                        onCheckedChange = { isNotificationEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ElectricTeal,
                            checkedTrackColor = ElectricTeal.copy(alpha = 0.4f),
                            uncheckedThumbColor = TextTertiary,
                            uncheckedTrackColor = SlateCardBg.copy(alpha = 0.5f)
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amtVal = amount.toDoubleOrNull() ?: 0.0
                            if (amtVal > 0.0) {
                                onSave(
                                    bill.copy(
                                        name = name.ifEmpty { "Untitled Bill" },
                                        amount = amtVal,
                                        category = selectedCategory,
                                        frequency = selectedFrequency,
                                        isNotificationEnabled = isNotificationEnabled,
                                        dueDate = selectedTimestamp
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricTeal, contentColor = SlateCardBg),
                        enabled = amount.isNotEmpty() && (amount.toDoubleOrNull() ?: 0.0) > 0.0
                    ) {
                        Text("Save Changes", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
