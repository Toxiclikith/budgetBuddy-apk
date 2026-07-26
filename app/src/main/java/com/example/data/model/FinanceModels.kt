package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val amount: Double,
    val type: String, // "INCOME", "EXPENSE", "TRANSFER"
    val category: String, // e.g., "Food", "Shopping", "Rent", "Salary", "Bonus", "Investment"
    val account: String, // "Cash", "Card", "Bank", "UPI", "Crypto"
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = "",
    val tags: String = "", // comma-separated tags
    val receiptPath: String? = null, // path to local stored receipt image
    val isFavorite: Boolean = false,
    val isRecurring: Boolean = false,
    val totalAmount: Double? = null, // Original amount before split
    val memberCount: Int? = null    // Number of people involved in split
)

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val amount: Double,
    val type: String, // "DAILY", "WEEKLY", "MONTHLY", "CATEGORY"
    val category: String? = null // only used if type is "CATEGORY"
)

@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val category: String, // "Emergency Fund", "Vacation", "House", "Car", "Education", "Custom"
    val targetDate: Long = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000) // Default 30 days
)

@Entity(tableName = "bills")
data class Bill(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val amount: Double,
    val category: String, // "Electricity", "Rent", "Netflix", "Spotify", "Insurance", "Custom"
    val dueDate: Long,
    val isRecurring: Boolean = true,
    val isPaid: Boolean = false,
    val frequency: String = "Monthly", // "Monthly", "Weekly", "Yearly"
    val isNotificationEnabled: Boolean = true
)
