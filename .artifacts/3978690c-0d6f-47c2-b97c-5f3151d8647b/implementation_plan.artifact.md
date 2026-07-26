# Advanced Ledger, Split & Bill Notification Enhancements

This plan details several feature improvements to make the app more user-friendly and functional: implementing real Android notifications for bills, adding date range filtering, making fields optional, and enabling split record editing.

## User Review Required

> [!IMPORTANT]
> - Real notifications require the **POST_NOTIFICATIONS** permission on Android 13+. Users will see a permission prompt.
> - "Optional" title field means transactions may appear as "Untitled" in the ledger if no title is provided.

## Proposed Changes

### [app]

#### [MODIFY] [libs.versions.toml](file:///C:/Users/saiku/Downloads/budgetbuddy/gradle/libs.versions.toml) & [build.gradle.kts](file:///C:/Users/saiku/Downloads/budgetbuddy/app/build.gradle.kts)
- Add `androidx.work:work-runtime-ktx` dependency for background notification scheduling.
- Uncomment `accompanist-permissions` for easier notification permission handling.

#### [NEW] [NotificationHelper.kt](file:///C:/Users/saiku/Downloads/budgetbuddy/app/src/main/java/com/example/util/NotificationHelper.kt)
- Utility to create notification channels and schedule/cancel bill reminders using `WorkManager`.

#### [NEW] [BillNotificationWorker.kt](file:///C:/Users/saiku/Downloads/budgetbuddy/app/src/main/java/com/example/util/BillNotificationWorker.kt)
- A background worker that triggers a system notification when a bill is due.

#### [MODIFY] [FinanceViewModel.kt](file:///C:/Users/saiku/Downloads/budgetbuddy/app/src/main/java/com/example/ui/viewmodel/FinanceViewModel.kt)
- Integrate `NotificationHelper` to automatically schedule notifications when a bill is added or its reminder is toggled on.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/saiku/Downloads/budgetbuddy/app/src/main/java/com/example/MainActivity.kt)
- Initialize the notification channel.
- Implement a permission request for notifications.

#### [MODIFY] [TransactionsScreen.kt](file:///C:/Users/saiku/Downloads/budgetbuddy/app/src/main/java/com/example/ui/screens/TransactionsScreen.kt)
- **Mandatory Fields**: Update `AddTransactionDialog` and `EditTransactionDialog` to only require `amount > 0`.
- **Date Range Filter**: Add UI (e.g., an "Event" icon button) and logic to filter transactions by a custom date range.

#### [MODIFY] [SplitScreen.kt](file:///C:/Users/saiku/Downloads/budgetbuddy/app/src/main/java/com/example/ui/screens/SplitScreen.kt)
- **Edit Split**: Implement `EditSplitDialog` to allow modifying recorded splits (including total amount and member count).
- **Mandatory Fields**: Update `AddSplitDialog` to only require `totalAmount > 0`.
- **Date Range Filter**: Add date range filtering consistent with the Ledger screen.

## Verification Plan

### Manual Verification
1. **Notifications**: Add a bill due in 1 minute, enable notifications, and verify the system notification appears.
2. **Optional Fields**: Add a transaction with only an amount and category. Verify it saves and displays correctly (e.g., as "Untitled Transaction").
3. **Date Filtering**: Select a date range that excludes some transactions and verify they disappear from the list.
4. **Edit Split**: Click an existing split record, change the member count, and verify the split amount updates correctly in the ledger.
