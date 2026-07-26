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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Groups
import androidx.compose.ui.platform.LocalContext
import java.util.Calendar
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.Transaction
import com.example.ui.theme.ElectricTeal
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RubyRed
import com.example.ui.theme.SlateCardBg
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    viewModel: FinanceViewModel
) {
    val transactions by viewModel.allTransactions.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = All, 1 = Income, 2 = Expense
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedTransactionForEdit by remember { mutableStateOf<Transaction?>(null) }
    
    var startDate by remember { mutableStateOf<Long?>(null) }
    var endDate by remember { mutableStateOf<Long?>(null) }

    // Filtering logic
    val filteredTransactions = transactions.filter { tx ->
        val matchesSearch = tx.title.contains(searchQuery, ignoreCase = true) ||
                tx.category.contains(searchQuery, ignoreCase = true) ||
                tx.notes.contains(searchQuery, ignoreCase = true)

        val matchesTab = when (selectedTab) {
            1 -> tx.type == "INCOME"
            2 -> tx.type == "EXPENSE"
            else -> true
        }
        
        val matchesDate = (startDate == null || tx.timestamp >= startDate!!) &&
                (endDate == null || tx.timestamp <= (endDate!! + 24L * 60 * 60 * 1000 - 1))

        matchesSearch && matchesTab && matchesDate
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = ElectricTeal,
                contentColor = SlateCardBg,
                modifier = Modifier.testTag("add_transaction_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Transaction")
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
            // Search & Filter Block
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("search_transactions_input"),
                    placeholder = { Text("Search logs...", color = TextSecondary, fontSize = 14.sp) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = TextSecondary, modifier = Modifier.size(20.dp)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricTeal,
                        unfocusedBorderColor = SlateCardBg,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                val context = LocalContext.current
                IconButton(
                    onClick = {
                        val calendar = Calendar.getInstance()
                        android.app.DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                val startCal = Calendar.getInstance()
                                startCal.set(year, month, dayOfMonth, 0, 0, 0)
                                startDate = startCal.timeInMillis
                                
                                // Show end date picker immediately after
                                android.app.DatePickerDialog(
                                    context,
                                    { _, endYear, endMonth, endDayOfMonth ->
                                        val endCal = Calendar.getInstance()
                                        endCal.set(endYear, endMonth, endDayOfMonth, 23, 59, 59)
                                        endDate = endCal.timeInMillis
                                    },
                                    year, month, dayOfMonth
                                ).apply {
                                    setTitle("Select End Date")
                                }.show()
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).apply {
                            setTitle("Select Start Date")
                        }.show()
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(if (startDate != null) ElectricTeal.copy(alpha = 0.2f) else SlateCardBg, shape = RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList, 
                        contentDescription = "Date Range", 
                        tint = if (startDate != null) ElectricTeal else TextSecondary
                    )
                }

                if (startDate != null) {
                    IconButton(onClick = { startDate = null; endDate = null }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Clear Dates", tint = RubyRed, modifier = Modifier.size(20.dp))
                    }
                }
            }

            if (startDate != null && endDate != null) {
                val sdf = remember { SimpleDateFormat("MMM d", Locale.getDefault()) }
                Text(
                    text = "Filtering: ${sdf.format(Date(startDate!!))} - ${sdf.format(Date(endDate!!))}",
                    style = MaterialTheme.typography.labelSmall,
                    color = ElectricTeal,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
            }

            // Tabs for All / Income / Expenses
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = ElectricTeal,
                indicator = { tabPositions ->
                    TabRowDefaults.PrimaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = ElectricTeal
                    )
                },
                divider = {}
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("All Logs", fontWeight = FontWeight.Bold, color = if (selectedTab == 0) ElectricTeal else TextSecondary) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Income", fontWeight = FontWeight.Bold, color = if (selectedTab == 1) EmeraldGreen else TextSecondary) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Expenses", fontWeight = FontWeight.Bold, color = if (selectedTab == 2) RubyRed else TextSecondary) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Transactions Scrollable List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredTransactions, key = { it.id }) { tx ->
                    val defaultTitle = "Untitled ${tx.type.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}"
                    TransactionRowWithActions(
                        transaction = tx.copy(title = tx.title.ifEmpty { defaultTitle }),
                        currencySymbol = currencySymbol,
                        onDeleteClick = { viewModel.deleteTransaction(tx.id) },
                        onFavoriteClick = { viewModel.updateTransaction(tx.copy(isFavorite = !tx.isFavorite)) },
                        onRowClick = { selectedTransactionForEdit = tx }
                    )
                }

                if (filteredTransactions.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No transactions found matching criteria.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }

        // Add Transaction Dialog
        if (showAddDialog) {
            AddTransactionDialog(
                currencySymbol = currencySymbol,
                onDismiss = { showAddDialog = false },
                onSave = { title, amount, type, category, account, notes, tags, timestamp ->
                    viewModel.insertTransaction(
                        Transaction(
                            title = title,
                            amount = amount,
                            type = type,
                            category = category,
                            account = account,
                            notes = notes,
                            tags = tags,
                            timestamp = timestamp
                        )
                    )
                    showAddDialog = false
                }
            )
        }

        // Edit Transaction Dialog
        selectedTransactionForEdit?.let { tx ->
            EditTransactionDialog(
                transaction = tx,
                currencySymbol = currencySymbol,
                onDismiss = { selectedTransactionForEdit = null },
                onSave = { updatedTx ->
                    viewModel.updateTransaction(updatedTx)
                    selectedTransactionForEdit = null
                }
            )
        }
    }
}

@Composable
fun TransactionRowWithActions(
    transaction: Transaction,
    currencySymbol: String,
    onDeleteClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onRowClick: () -> Unit
) {
    val isExpense = transaction.type == "EXPENSE"
    val isIncome = transaction.type == "INCOME"

    val statusColor = when {
        isIncome -> EmeraldGreen
        isExpense -> RubyRed
        else -> ElectricTeal
    }

    val sdf = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }
    val formattedDate = sdf.format(Date(transaction.timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onRowClick() },
        colors = CardDefaults.cardColors(containerColor = SlateCardBg),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Main icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(statusColor.copy(alpha = 0.1f), shape = RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isIncome) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Text(
                    text = "${transaction.category} • ${transaction.account} • $formattedDate",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                if (transaction.memberCount != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = null,
                            tint = ElectricTeal,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Split between ${transaction.memberCount} members",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = ElectricTeal
                        )
                    }
                }
                if (transaction.notes.isNotEmpty()) {
                    Text(
                        text = transaction.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary,
                        maxLines = 1
                    )
                }
            }

            // Price & Actions
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = String.format(Locale.getDefault(), "%s%s%,.2f", if (isExpense) "-" else if (isIncome) "+" else "", currencySymbol, transaction.amount),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = statusColor
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onFavoriteClick, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = if (transaction.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Favorite",
                            tint = if (transaction.isFavorite) ElectricTeal else TextTertiary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(onClick = onDeleteClick, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = RubyRed.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (String, Double, String, String, String, String, String, Long) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("EXPENSE") } // INCOME or EXPENSE
    var selectedCategory by remember { mutableStateOf("Food") }
    var selectedAccount by remember { mutableStateOf("Bank") }
    var notes by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var selectedTimestamp by remember { mutableStateOf(System.currentTimeMillis()) }

    val categories = if (type == "INCOME") {
        listOf("Salary", "Bonus", "Gift", "Freelancing", "Investment", "Custom")
    } else {
        listOf("Food", "Shopping", "Rent", "Transportation", "Entertainment", "Utilities", "Insurance", "Medical", "Custom")
    }

    val accounts = listOf("Bank", "Card", "Cash", "UPI", "Crypto")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SlateCardBg,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .background(Color.Transparent),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Add Mindful Transaction",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )

                // Type Toggle
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { type = "EXPENSE"; selectedCategory = "Food" },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type == "EXPENSE") RubyRed else SlateCardBg.copy(alpha = 0.2f),
                            contentColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Expense")
                    }

                    Button(
                        onClick = { type = "INCOME"; selectedCategory = "Salary" },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type == "INCOME") EmeraldGreen else SlateCardBg.copy(alpha = 0.2f),
                            contentColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Income")
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title / Payee", color = TextSecondary) },
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
                    label = { Text("Amount ($currencySymbol)", color = TextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricTeal,
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Category dropdown
                var categoryExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = selectedCategory,
                        onValueChange = {},
                        label = { Text("Category", color = TextSecondary) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
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
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false },
                        modifier = Modifier.background(SlateCardBg)
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat, color = TextPrimary) },
                                onClick = {
                                    selectedCategory = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                // Account dropdown
                var accountExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = accountExpanded,
                    onExpandedChange = { accountExpanded = !accountExpanded }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = selectedAccount,
                        onValueChange = {},
                        label = { Text("Account / Wallet", color = TextSecondary) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountExpanded) },
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
                        expanded = accountExpanded,
                        onDismissRequest = { accountExpanded = false },
                        modifier = Modifier.background(SlateCardBg)
                    ) {
                        accounts.forEach { acc ->
                            DropdownMenuItem(
                                text = { Text(acc, color = TextPrimary) },
                                onClick = {
                                    selectedAccount = acc
                                    accountExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricTeal,
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

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
                        label = { Text("Transaction Date", color = TextSecondary) },
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
 
                // Dialog Buttons
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
                                    title.ifEmpty { "Untitled" }, 
                                    amtVal, type, selectedCategory, selectedAccount, notes, tags, selectedTimestamp
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricTeal, contentColor = SlateCardBg),
                        enabled = amount.isNotEmpty() && (amount.toDoubleOrNull() ?: 0.0) > 0.0
                    ) {
                        Text("Save Entry", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionDialog(
    transaction: Transaction,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (Transaction) -> Unit
) {
    var title by remember { mutableStateOf(transaction.title) }
    var amount by remember { mutableStateOf(transaction.amount.toString()) }
    var type by remember { mutableStateOf(transaction.type) } // INCOME or EXPENSE
    var selectedCategory by remember { mutableStateOf(transaction.category) }
    var selectedAccount by remember { mutableStateOf(transaction.account) }
    var notes by remember { mutableStateOf(transaction.notes) }
    var selectedTimestamp by remember { mutableStateOf(transaction.timestamp) }

    val categories = if (type == "INCOME") {
        listOf("Salary", "Bonus", "Gift", "Freelancing", "Investment", "Custom")
    } else {
        listOf("Food", "Shopping", "Rent", "Transportation", "Entertainment", "Utilities", "Insurance", "Medical", "Custom")
    }

    val accounts = listOf("Bank", "Card", "Cash", "UPI", "Crypto")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SlateCardBg,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .background(Color.Transparent),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Modify Transaction",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )

                // Type Toggle
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { type = "EXPENSE"; selectedCategory = "Food" },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type == "EXPENSE") RubyRed else SlateCardBg.copy(alpha = 0.2f),
                            contentColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Expense")
                    }

                    Button(
                        onClick = { type = "INCOME"; selectedCategory = "Salary" },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type == "INCOME") EmeraldGreen else SlateCardBg.copy(alpha = 0.2f),
                            contentColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Income")
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title / Payee", color = TextSecondary) },
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
                    label = { Text("Amount ($currencySymbol)", color = TextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricTeal,
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Category dropdown
                var categoryExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = selectedCategory,
                        onValueChange = {},
                        label = { Text("Category", color = TextSecondary) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
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
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false },
                        modifier = Modifier.background(SlateCardBg)
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat, color = TextPrimary) },
                                onClick = {
                                    selectedCategory = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                // Account dropdown
                var accountExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = accountExpanded,
                    onExpandedChange = { accountExpanded = !accountExpanded }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = selectedAccount,
                        onValueChange = {},
                        label = { Text("Account / Wallet", color = TextSecondary) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountExpanded) },
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
                        expanded = accountExpanded,
                        onDismissRequest = { accountExpanded = false },
                        modifier = Modifier.background(SlateCardBg)
                    ) {
                        accounts.forEach { acc ->
                            DropdownMenuItem(
                                text = { Text(acc, color = TextPrimary) },
                                onClick = {
                                    selectedAccount = acc
                                    accountExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricTeal,
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

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
                        label = { Text("Transaction Date", color = TextSecondary) },
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

                // Dialog Buttons
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
                                    transaction.copy(
                                        title = title.ifEmpty { "Untitled" },
                                        amount = amtVal,
                                        type = type,
                                        category = selectedCategory,
                                        account = selectedAccount,
                                        notes = notes,
                                        timestamp = selectedTimestamp
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
