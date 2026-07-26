package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.PreferencesManager
import com.example.data.model.Bill
import com.example.data.model.Budget
import com.example.data.model.Goal
import com.example.data.model.Transaction
import com.example.data.repository.FinanceRepository
import com.example.util.NotificationHelper
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FinanceViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = FinanceRepository(
        db.transactionDao(),
        db.budgetDao(),
        db.goalDao(),
        db.billDao()
    )
    private val preferencesManager = PreferencesManager(application)

    // Reactive StateFlows from Database
    val allTransactions: StateFlow<List<Transaction>> = repository.allTransactions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val splitTransactions: StateFlow<List<Transaction>> = allTransactions
        .map { list -> list.filter { it.memberCount != null } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allBudgets: StateFlow<List<Budget>> = repository.allBudgets
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allGoals: StateFlow<List<Goal>> = repository.allGoals
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allBills: StateFlow<List<Bill>> = repository.allBills
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Reactive StateFlows from Preferences
    val currencySymbol: StateFlow<String> = preferencesManager.currencySymbol
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "$"
        )

    val dailySpendingLimit: StateFlow<Double> = preferencesManager.dailySpendingLimit
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 50.0
        )

    val pinLockEnabled: StateFlow<Boolean> = preferencesManager.pinLockEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val securityPin: StateFlow<String> = preferencesManager.securityPin
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "1234"
        )

    val savingsStreak: StateFlow<Int> = preferencesManager.savingsStreak
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val onboardingCompleted: StateFlow<Boolean> = preferencesManager.onboardingCompleted
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val lastStreakUpdate: StateFlow<Long> = preferencesManager.lastStreakUpdate
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0L
        )

    // --- Database Operations ---

    fun insertTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.insertTransaction(transaction)
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
        }
    }

    fun deleteTransaction(id: Int) {
        viewModelScope.launch {
            repository.deleteTransactionById(id)
        }
    }

    fun insertBudget(budget: Budget) {
        viewModelScope.launch {
            repository.insertBudget(budget)
        }
    }

    fun deleteBudget(id: Int) {
        viewModelScope.launch {
            repository.deleteBudgetById(id)
        }
    }

    fun insertGoal(goal: Goal) {
        viewModelScope.launch {
            repository.insertGoal(goal)
        }
    }

    fun updateGoal(goal: Goal) {
        viewModelScope.launch {
            repository.updateGoal(goal)
        }
    }

    fun deleteGoal(id: Int) {
        viewModelScope.launch {
            repository.deleteGoalById(id)
        }
    }

    fun insertBill(bill: Bill) {
        viewModelScope.launch {
            val id = repository.insertBill(bill)
            NotificationHelper.scheduleBillNotification(getApplication(), bill.copy(id = id.toInt()))
        }
    }

    fun updateBill(bill: Bill) {
        viewModelScope.launch {
            repository.updateBill(bill)
            NotificationHelper.scheduleBillNotification(getApplication(), bill)
        }
    }

    fun toggleBillPaid(bill: Bill) {
        viewModelScope.launch {
            val updatedBill = bill.copy(isPaid = !bill.isPaid)
            repository.updateBill(updatedBill)
            NotificationHelper.scheduleBillNotification(getApplication(), updatedBill)
        }
    }

    fun toggleBillNotification(bill: Bill) {
        viewModelScope.launch {
            val updatedBill = bill.copy(isNotificationEnabled = !bill.isNotificationEnabled)
            repository.updateBill(updatedBill)
            NotificationHelper.scheduleBillNotification(getApplication(), updatedBill)
        }
    }

    fun deleteBill(id: Int) {
        viewModelScope.launch {
            repository.deleteBillById(id)
            NotificationHelper.cancelBillNotification(getApplication(), id)
        }
    }

    fun addSampleData() {
        viewModelScope.launch {
            repository.addSampleData()
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            preferencesManager.resetSavingsStreak()
        }
    }

    // --- JSON Export/Import for Device Migration ---

    fun exportDataToJson(onComplete: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val transactionsList = allTransactions.first()
                val budgetsList = allBudgets.first()
                val goalsList = allGoals.first()
                val billsList = allBills.first()

                val root = org.json.JSONObject()

                // Transactions
                val txArray = org.json.JSONArray()
                for (tx in transactionsList) {
                    val obj = org.json.JSONObject()
                    obj.put("title", tx.title)
                    obj.put("amount", tx.amount)
                    obj.put("type", tx.type)
                    obj.put("category", tx.category)
                    obj.put("account", tx.account)
                    obj.put("timestamp", tx.timestamp)
                    obj.put("notes", tx.notes)
                    obj.put("tags", tx.tags)
                    obj.put("receiptPath", tx.receiptPath ?: "")
                    obj.put("isFavorite", tx.isFavorite)
                    obj.put("isRecurring", tx.isRecurring)
                    txArray.put(obj)
                }
                root.put("transactions", txArray)

                // Budgets
                val bgArray = org.json.JSONArray()
                for (bg in budgetsList) {
                    val obj = org.json.JSONObject()
                    obj.put("name", bg.name)
                    obj.put("amount", bg.amount)
                    obj.put("type", bg.type)
                    obj.put("category", bg.category ?: "")
                    bgArray.put(obj)
                }
                root.put("budgets", bgArray)

                // Goals
                val glArray = org.json.JSONArray()
                for (gl in goalsList) {
                    val obj = org.json.JSONObject()
                    obj.put("name", gl.name)
                    obj.put("targetAmount", gl.targetAmount)
                    obj.put("currentAmount", gl.currentAmount)
                    obj.put("category", gl.category)
                    obj.put("targetDate", gl.targetDate)
                    glArray.put(obj)
                }
                root.put("goals", glArray)

                // Bills
                val blArray = org.json.JSONArray()
                for (bl in billsList) {
                    val obj = org.json.JSONObject()
                    obj.put("name", bl.name)
                    obj.put("amount", bl.amount)
                    obj.put("category", bl.category)
                    obj.put("dueDate", bl.dueDate)
                    obj.put("isRecurring", bl.isRecurring)
                    obj.put("isPaid", bl.isPaid)
                    obj.put("frequency", bl.frequency)
                    obj.put("isNotificationEnabled", bl.isNotificationEnabled)
                    blArray.put(obj)
                }
                root.put("bills", blArray)

                onComplete(root.toString(2))
            } catch (e: Exception) {
                onComplete("Error: ${e.localizedMessage}")
            }
        }
    }

    fun importDataFromJson(jsonStr: String, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val root = org.json.JSONObject(jsonStr)

                // Read lists
                val importedTx = mutableListOf<Transaction>()
                val importedBg = mutableListOf<Budget>()
                val importedGl = mutableListOf<Goal>()
                val importedBl = mutableListOf<Bill>()

                if (root.has("transactions")) {
                    val arr = root.getJSONArray("transactions")
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        importedTx.add(
                            Transaction(
                                title = obj.getString("title"),
                                amount = obj.getDouble("amount"),
                                type = obj.getString("type"),
                                category = obj.getString("category"),
                                account = obj.getString("account"),
                                timestamp = obj.getLong("timestamp"),
                                notes = obj.optString("notes", ""),
                                tags = obj.optString("tags", ""),
                                receiptPath = if (obj.optString("receiptPath").isNotEmpty()) obj.getString("receiptPath") else null,
                                isFavorite = obj.optBoolean("isFavorite", false),
                                isRecurring = obj.optBoolean("isRecurring", false)
                            )
                        )
                    }
                }

                if (root.has("budgets")) {
                    val arr = root.getJSONArray("budgets")
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        importedBg.add(
                            Budget(
                                name = obj.getString("name"),
                                amount = obj.getDouble("amount"),
                                type = obj.getString("type"),
                                category = if (obj.optString("category").isNotEmpty()) obj.getString("category") else null
                            )
                        )
                    }
                }

                if (root.has("goals")) {
                    val arr = root.getJSONArray("goals")
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        importedGl.add(
                            Goal(
                                name = obj.getString("name"),
                                targetAmount = obj.getDouble("targetAmount"),
                                currentAmount = obj.getDouble("currentAmount"),
                                category = obj.getString("category"),
                                targetDate = obj.getLong("targetDate")
                            )
                        )
                    }
                }

                if (root.has("bills")) {
                    val arr = root.getJSONArray("bills")
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        importedBl.add(
                            Bill(
                                name = obj.getString("name"),
                                amount = obj.getDouble("amount"),
                                category = obj.getString("category"),
                                dueDate = obj.getLong("dueDate"),
                                isRecurring = obj.optBoolean("isRecurring", true),
                                isPaid = obj.optBoolean("isPaid", false),
                                frequency = obj.optString("frequency", "Monthly"),
                                isNotificationEnabled = obj.optBoolean("isNotificationEnabled", true)
                            )
                        )
                    }
                }

                // If parses are successful, clear the DB and insert
                repository.clearAllData()

                for (tx in importedTx) repository.insertTransaction(tx)
                for (bg in importedBg) repository.insertBudget(bg)
                for (gl in importedGl) repository.insertGoal(gl)
                for (bl in importedBl) repository.insertBill(bl)

                onComplete(true, "Data imported successfully! Loaded ${importedTx.size} Transactions, ${importedBg.size} Budgets, ${importedGl.size} Goals, and ${importedBl.size} Bill trackers.")
            } catch (e: Exception) {
                onComplete(false, "Invalid backup format: ${e.localizedMessage}")
            }
        }
    }

    // --- Preferences Operations ---

    fun setCurrencySymbol(symbol: String) {
        viewModelScope.launch {
            preferencesManager.setCurrencySymbol(symbol)
        }
    }

    fun setDailySpendingLimit(limit: Double) {
        viewModelScope.launch {
            preferencesManager.setDailySpendingLimit(limit)
        }
    }

    fun setPinLockEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setPinLockEnabled(enabled)
        }
    }

    fun setSecurityPin(pin: String) {
        viewModelScope.launch {
            preferencesManager.setSecurityPin(pin)
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            preferencesManager.setOnboardingCompleted(true)
            // Pre-seed some sample data so the app has some graphs immediately!
            addSampleData()
        }
    }

    fun updateStreak() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val lastUpdate = lastStreakUpdate.value

            if (lastUpdate == 0L) {
                // First time
                preferencesManager.setSavingsStreak(1, now)
                return@launch
            }

            val diff = now - lastUpdate
            val oneDayMs = 24 * 60 * 60 * 1000L
            val twoDaysMs = 48 * 60 * 60 * 1000L

            if (diff >= oneDayMs) {
                if (diff < twoDaysMs) {
                    // Consecutive day
                    preferencesManager.incrementSavingsStreak(now)
                } else {
                    // Missed a day
                    preferencesManager.setSavingsStreak(1, now)
                }
            }
        }
    }
}
