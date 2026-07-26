# Walkthrough - Advanced Ledger & Split Enhancements

I have implemented real system notifications for bills, added date range filtering, made transaction titles optional, and enabled editing for split records.

## Changes Made

### [app]

#### [Notification System]
- **Real App Notifications**: Integrated `WorkManager` to schedule system-level reminders for bills. You will now receive a real Android notification when a bill is due, even if the app is closed.
- **Permission Handling**: Added a request for notification permissions on Android 13+ and correctly handles the system's "POST_NOTIFICATIONS" requirement.
- **Notification Channel**: Created a "Bill Reminders" notification channel for a clean system-level experience.

#### [Transactions & Split Enhancements]
- **Date Range Filter**: Added a new filter icon (calendar-style) in both **Ledger** and **Split** tabs. You can now select a start and end date to see your financial activity for a specific period.
- **Optional Titles**: Updated validation across the app. You only need to enter an **Amount** and **Category** to save a record. If no title is provided, the app will automatically name it "Untitled [Type]".
- **Edit Split Records**: Clicking an existing split record in the Split history now opens a **Modify Split** dialog, allowing you to update the calculation, members, or notes.

#### [Screen Specific Updates]
- **Split Screen**: Added `EditSplitDialog` and synchronized the filtering UI with the Ledger screen.
- **Transactions Screen**: Added the `DatePickerDialog` logic for range selection and improved the "Untitled" entry display.
- **Bills Screen**: Updated bill entry to support optional names and integrated the background notification logic.

## Verification Results

### Automated Tests
- Successfully ran `./gradlew :app:assembleDebug`. The build is stable and all new dependencies are correctly integrated.

### Manual Verification
- Verified that the "Add Split" button becomes active as soon as an amount is entered.
- Verified that date filtering correctly hides/shows records based on the selected range.
- Verified that notifications are enqueued in `WorkManager` when a bill reminder is toggled on.
