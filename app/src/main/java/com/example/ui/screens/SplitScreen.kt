package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Transaction
import com.example.ui.theme.*
import com.example.ui.viewmodel.FinanceViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitScreen(
    viewModel: FinanceViewModel
) {
    val splitTransactions by viewModel.splitTransactions.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = All, 1 = Income, 2 = Expense
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedTransactionForEdit by remember { mutableStateOf<Transaction?>(null) }

    var startDate by remember { mutableStateOf<Long?>(null) }
    var endDate by remember { mutableStateOf<Long?>(null) }

    // Filtering logic
    val filteredTransactions = splitTransactions.filter { tx ->
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
                contentColor = SlateDarkBg,
                modifier = Modifier.testTag("add_split_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Split")
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
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Search splits...", color = TextSecondary, fontSize = 14.sp) },
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

                                android.app.DatePickerDialog(
                                    context,
                                    { _, endYear, endMonth, endDayOfMonth ->
                                        val endCal = Calendar.getInstance()
                                        endCal.set(endYear, endMonth, endDayOfMonth, 23, 59, 59)
                                        endDate = endCal.timeInMillis
                                    },
                                    year, month, dayOfMonth
                                ).apply { setTitle("Select End Date") }.show()
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).apply { setTitle("Select Start Date") }.show()
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
                    text = { Text("All Splits", fontWeight = FontWeight.Bold, color = if (selectedTab == 0) ElectricTeal else TextSecondary) }
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

            // Split History List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredTransactions, key = { it.id }) { tx ->
                    val defaultTitle = "Untitled ${tx.type.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }} Split"
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
                                text = "No split records found.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            AddSplitDialog(
                currencySymbol = currencySymbol,
                onDismiss = { showAddDialog = false },
                onSave = { title, splitAmount, type, category, account, notes, timestamp, totalAmount, memberCount ->
                    viewModel.insertTransaction(
                        Transaction(
                            title = title.ifEmpty { "Untitled Split" },
                            amount = splitAmount,
                            type = type,
                            category = category,
                            account = account,
                            notes = notes,
                            timestamp = timestamp,
                            totalAmount = totalAmount,
                            memberCount = memberCount
                        )
                    )
                    showAddDialog = false
                }
            )
        }

        // Edit Split Dialog
        selectedTransactionForEdit?.let { tx ->
            EditSplitDialog(
                transaction = tx,
                currencySymbol = currencySymbol,
                onDismiss = { selectedTransactionForEdit = null },
                onSave = { updatedTx ->
                    viewModel.updateTransaction(updatedTx.copy(title = updatedTx.title.ifEmpty { "Untitled Split" }))
                    selectedTransactionForEdit = null
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSplitDialog(
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (String, Double, String, String, String, String, Long, Double, Int) -> Unit
) {
    var totalAmountInput by remember { mutableStateOf("") }
    var memberCountInput by remember { mutableStateOf("2") }
    var isExpense by remember { mutableStateOf(true) }

    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Food") }
    var selectedAccount by remember { mutableStateOf("Bank") }
    var notes by remember { mutableStateOf("") }
    var selectedTimestamp by remember { mutableStateOf(System.currentTimeMillis()) }

    val amount = totalAmountInput.toDoubleOrNull() ?: 0.0
    val members = memberCountInput.toIntOrNull() ?: 1
    val splitAmount = if (members > 0) amount / members else 0.0

    val categories = if (!isExpense) {
        listOf("Salary", "Bonus", "Gift", "Freelancing", "Investment", "Custom")
    } else {
        listOf("Food", "Shopping", "Rent", "Transportation", "Entertainment", "Utilities", "Insurance", "Medical", "Custom")
    }
    
    val accounts = listOf("Bank", "Card", "Cash", "UPI", "Crypto")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SlateCardBg,
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Split The Load",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )

                // Result Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SlateDarkBg),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Each Member Pays",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isExpense) RubyRed else EmeraldGreen
                        )
                        Text(
                            text = String.format(Locale.getDefault(), "%s%,.2f", currencySymbol, splitAmount),
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                            color = TextPrimary
                        )
                    }
                }

                // Type Toggle
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { 
                            isExpense = true
                            selectedCategory = "Food"
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isExpense) RubyRed else SlateDarkBg.copy(alpha = 0.5f),
                            contentColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Expense")
                    }

                    Button(
                        onClick = { 
                            isExpense = false
                            selectedCategory = "Salary"
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isExpense) EmeraldGreen else SlateDarkBg.copy(alpha = 0.5f),
                            contentColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Income")
                    }
                }

                OutlinedTextField(
                    value = totalAmountInput,
                    onValueChange = { totalAmountInput = it },
                    label = { Text("Total Amount", color = TextSecondary) },
                    prefix = { Text(currencySymbol) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricTeal,
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = memberCountInput,
                    onValueChange = { 
                        if (it.isEmpty() || (it.toIntOrNull() != null && it.toInt() in 1..99)) {
                            memberCountInput = it
                        }
                    },
                    label = { Text("Number of Members", color = TextSecondary) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = ElectricTeal) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricTeal,
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider(color = SlateDarkBg)

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
                            if (splitAmount > 0) {
                                onSave(
                                    title, 
                                    splitAmount, 
                                    if (isExpense) "EXPENSE" else "INCOME", 
                                    selectedCategory, 
                                    selectedAccount, 
                                    notes, 
                                    selectedTimestamp,
                                    amount,
                                    members
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricTeal, 
                            contentColor = SlateDarkBg,
                            disabledContainerColor = ElectricTeal.copy(alpha = 0.12f),
                            disabledContentColor = TextSecondary.copy(alpha = 0.38f)
                        ),
                        enabled = splitAmount > 0
                    ) {
                        Text("Add Split", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSplitDialog(
    transaction: Transaction,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (Transaction) -> Unit
) {
    var totalAmountInput by remember { mutableStateOf(transaction.totalAmount?.toString() ?: (transaction.amount * (transaction.memberCount ?: 1)).toString()) }
    var memberCountInput by remember { mutableStateOf(transaction.memberCount?.toString() ?: "1") }
    var isExpense by remember { mutableStateOf(transaction.type == "EXPENSE") }

    var title by remember { mutableStateOf(transaction.title) }
    var selectedCategory by remember { mutableStateOf(transaction.category) }
    var selectedAccount by remember { mutableStateOf(transaction.account) }
    var notes by remember { mutableStateOf(transaction.notes) }
    var selectedTimestamp by remember { mutableStateOf(transaction.timestamp) }

    val amount = totalAmountInput.toDoubleOrNull() ?: 0.0
    val members = memberCountInput.toIntOrNull() ?: 1
    val splitAmount = if (members > 0) amount / members else 0.0

    val categories = if (!isExpense) {
        listOf("Salary", "Bonus", "Gift", "Freelancing", "Investment", "Custom")
    } else {
        listOf("Food", "Shopping", "Rent", "Transportation", "Entertainment", "Utilities", "Insurance", "Medical", "Custom")
    }
    
    val accounts = listOf("Bank", "Card", "Cash", "UPI", "Crypto")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SlateCardBg,
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Modify Split",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )

                // Result Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SlateDarkBg),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Each Member Pays",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isExpense) RubyRed else EmeraldGreen
                        )
                        Text(
                            text = String.format(Locale.getDefault(), "%s%,.2f", currencySymbol, splitAmount),
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                            color = TextPrimary
                        )
                    }
                }

                // Type Toggle
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { 
                            isExpense = true
                            selectedCategory = "Food"
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isExpense) RubyRed else SlateDarkBg.copy(alpha = 0.5f),
                            contentColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Expense")
                    }

                    Button(
                        onClick = { 
                            isExpense = false
                            selectedCategory = "Salary"
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isExpense) EmeraldGreen else SlateDarkBg.copy(alpha = 0.5f),
                            contentColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Income")
                    }
                }

                OutlinedTextField(
                    value = totalAmountInput,
                    onValueChange = { totalAmountInput = it },
                    label = { Text("Total Amount", color = TextSecondary) },
                    prefix = { Text(currencySymbol) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricTeal,
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = memberCountInput,
                    onValueChange = { 
                        if (it.isEmpty() || (it.toIntOrNull() != null && it.toInt() in 1..99)) {
                            memberCountInput = it
                        }
                    },
                    label = { Text("Number of Members", color = TextSecondary) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = ElectricTeal) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricTeal,
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider(color = SlateDarkBg)

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title / Payee (Optional)", color = TextSecondary) },
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
                                    selectedCal.set(year, month, dayOfMonth)
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
                            if (splitAmount > 0) {
                                onSave(
                                    transaction.copy(
                                        title = title,
                                        amount = splitAmount,
                                        type = if (isExpense) "EXPENSE" else "INCOME",
                                        category = selectedCategory,
                                        account = selectedAccount,
                                        notes = notes,
                                        timestamp = selectedTimestamp,
                                        totalAmount = amount,
                                        memberCount = members
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricTeal, 
                            contentColor = SlateDarkBg,
                            disabledContainerColor = ElectricTeal.copy(alpha = 0.12f),
                            disabledContentColor = TextSecondary.copy(alpha = 0.38f)
                        ),
                        enabled = splitAmount > 0
                    ) {
                        Text("Save Changes", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
