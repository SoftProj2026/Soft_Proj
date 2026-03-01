/*
 Sprint 3 — Notifications & Mocking (User Stories)
 
US3.1 – Send appointment reminders
Story: As a system, I want to send reminders for upcoming appointments so that users are notified in advance.
Acceptance:

After a user logs in (normal user login), the reminder service starts automatically.
The system checks periodically (e.g., every 1 minute) for the logged-in user’s CONFIRMED appointments.
If an appointment starts within the configured reminder window (e.g., 60 minutes), a reminder notification is shown.
Reminders are only shown for the currently logged-in user.
US3.2 – Stop reminders on logout
Story: As a system, I want to stop sending reminders when the user logs out so that notifications do not appear after the session ends.
Acceptance:

When the user logs out from the main dashboard, the reminder service stops.
After logout, no further reminder popups appear for that user session.
US3.3 – Do not start reminders for admin sessions
Story: As a system, I want admin sessions to avoid user reminder notifications so that admin mode stays focused on administration tasks.
Acceptance:

When logging in through Admin mode, the reminder service is not started.
Admin dashboard is shown without reminder popups.
US3.4 – Mock/notification mechanism via UI popup
Story: As a system, I want reminders to be delivered using a simple mock notification mechanism so that reminder behavior can be tested without external services.
Acceptance:

Reminder notifications are displayed using a simple UI mechanism (e.g., JOptionPane).
The reminder message includes enough details to identify the appointment (time and/or category).

 * */

