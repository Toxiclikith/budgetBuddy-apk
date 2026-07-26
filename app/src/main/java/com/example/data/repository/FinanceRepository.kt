package com.example.data.repository

import com.example.data.local.BillDao
import com.example.data.local.BudgetDao
import com.example.data.local.GoalDao
import com.example.data.local.TransactionDao
import com.example.data.model.Bill
import com.example.data.model.Budget
import com.example.data.model.Goal
import com.example.data.model.Transaction
import kotlinx.coroutines.flow.Flow

class FinanceRepository(
    private val transactionDao: TransactionDao,
    private val budgetDao: BudgetDao,
    private val goalDao: GoalDao,
    private val billDao: BillDao
) {
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()
    val allBudgets: Flow<List<Budget>> = budgetDao.getAllBudgets()
    val allGoals: Flow<List<Goal>> = goalDao.getAllGoals()
    val allBills: Flow<List<Bill>> = billDao.getAllBills()

    fun searchTransactions(query: String): Flow<List<Transaction>> {
        return transactionDao.searchTransactions(query)
    }

    fun getTransactionsInRange(startTime: Long, endTime: Long): Flow<List<Transaction>> {
        return transactionDao.getTransactionsInRange(startTime, endTime)
    }

    suspend fun insertTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun deleteTransactionById(id: Int) {
        transactionDao.deleteTransactionById(id)
    }

    suspend fun insertBudget(budget: Budget) {
        budgetDao.insertBudget(budget)
    }

    suspend fun updateBudget(budget: Budget) {
        budgetDao.updateBudget(budget)
    }

    suspend fun deleteBudgetById(id: Int) {
        budgetDao.deleteBudgetById(id)
    }

    suspend fun insertGoal(goal: Goal) {
        goalDao.insertGoal(goal)
    }

    suspend fun updateGoal(goal: Goal) {
        goalDao.updateGoal(goal)
    }

    suspend fun deleteGoalById(id: Int) {
        goalDao.deleteGoalById(id)
    }

    suspend fun insertBill(bill: Bill): Long {
        return billDao.insertBill(bill)
    }

    suspend fun updateBill(bill: Bill) {
        billDao.updateBill(bill)
    }

    suspend fun deleteBillById(id: Int) {
        billDao.deleteBillById(id)
    }

    suspend fun clearAllData() {
        transactionDao.deleteAllTransactions()
        budgetDao.deleteAllBudgets()
        goalDao.deleteAllGoals()
        billDao.deleteAllBills()
    }

    suspend fun addSampleData() {
        // Clear first to avoid duplicates
        clearAllData()

        val now = System.currentTimeMillis()

        // Sample Transactions
        val samples = listOf(
            Transaction(title = "Monthly Salary", amount = 4500.0, type = "INCOME", category = "Salary", account = "Bank", timestamp = now - 3L * 24 * 60 * 60 * 1000),
            Transaction(title = "Whole Foods Grocery", amount = 124.50, type = "EXPENSE", category = "Food", account = "Card", timestamp = now - 2L * 24 * 60 * 60 * 1000, notes = "Weekly meal prep"),
            Transaction(title = "Rent Payment", amount = 1200.0, type = "EXPENSE", category = "Rent", account = "Bank", timestamp = now - 5L * 24 * 60 * 60 * 1000),
            Transaction(title = "Freelance UI Design", amount = 650.0, type = "INCOME", category = "Freelancing", account = "UPI", timestamp = now - 1L * 24 * 60 * 60 * 1000),
            Transaction(title = "Gas Station", amount = 45.0, type = "EXPENSE", category = "Transportation", account = "Cash", timestamp = now - 12 * 60 * 60 * 1000),
            Transaction(title = "Netflix Premium", amount = 15.49, type = "EXPENSE", category = "Entertainment", account = "Card", timestamp = now - 4 * 60 * 60 * 1000, isRecurring = true),
            Transaction(title = "Stock Dividend", amount = 85.00, type = "INCOME", category = "Investment", account = "Bank", timestamp = now - 8L * 24 * 60 * 60 * 1000),
            Transaction(title = "Coffee Shop", amount = 6.75, type = "EXPENSE", category = "Food", account = "UPI", timestamp = now - 2 * 60 * 60 * 1000, isFavorite = true)
        )
        for (tx in samples) {
            transactionDao.insertTransaction(tx)
        }

        // Sample Budgets
        val budgets = listOf(
            Budget(name = "Monthly Food Budget", amount = 500.0, type = "CATEGORY", category = "Food"),
            Budget(name = "Monthly Transport", amount = 150.0, type = "CATEGORY", category = "Transportation"),
            Budget(name = "Entertainment", amount = 100.0, type = "CATEGORY", category = "Entertainment"),
            Budget(name = "Shopping Budget", amount = 300.0, type = "CATEGORY", category = "Shopping")
        )
        for (bg in budgets) {
            budgetDao.insertBudget(bg)
        }

        // Sample Goals
        val goals = listOf(
            Goal(name = "Emergency Fund", targetAmount = 10000.0, currentAmount = 4500.0, category = "Emergency Fund"),
            Goal(name = "Europe Summer Trip", targetAmount = 3000.0, currentAmount = 1200.0, category = "Vacation", targetDate = now + (90L * 24 * 60 * 60 * 1000)),
            Goal(name = "Tesla Down Payment", targetAmount = 15000.0, currentAmount = 3000.0, category = "Car", targetDate = now + (365L * 24 * 60 * 60 * 1000))
        )
        for (g in goals) {
            goalDao.insertGoal(g)
        }

        // Sample Bills
        val bills = listOf(
            Bill(name = "House Rent", amount = 1200.0, category = "Rent", dueDate = now + (15L * 24 * 60 * 60 * 1000), isPaid = false),
            Bill(name = "Highspeed Internet", amount = 65.0, category = "Internet", dueDate = now + (5L * 24 * 60 * 60 * 1000), isPaid = false),
            Bill(name = "Netflix Standard", amount = 15.49, category = "Netflix", dueDate = now + (1L * 24 * 60 * 60 * 1000), isPaid = false),
            Bill(name = "Spotify Family", amount = 16.99, category = "Spotify", dueDate = now + (12L * 24 * 60 * 60 * 1000), isPaid = true),
            Bill(name = "Power Utility Bill", amount = 110.0, category = "Electricity", dueDate = now + (2L * 24 * 60 * 60 * 1000), isPaid = false)
        )
        for (b in bills) {
            billDao.insertBill(b)
        }
    }
}
