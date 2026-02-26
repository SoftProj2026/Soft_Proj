
/*Sprint 1 – Core Scheduling & Authentication (Week 2)
In Sprint 1, we implemented the core authentication and slot viewing functionality. The login process is handled by AuthService.login(), which validates the provided username and password against users stored in the in-memory DataRepository. Upon successful authentication, the current user is stored and the UI navigates from LoginFrame to MainDashboardFrame.
Logout is implemented through AuthService.logout() and triggered by the “Logout” button in the dashboard, which clears the current session and returns the user to the login screen.
For viewing available slots (US1.3), the dashboard loads time slots by category and displays only available (not booked) slots using TimeSlot.isAvailable(). Additionally, blocked break-time slots (12:00–13:00) are disabled using BlockedSlotsRule to ensure users cannot select them.
*/

/*
 Sprint 1 : User Stories Summary
(Authentication & Viewing Available Slots)
US1 – Login 
Story: As a user (and admin), I want to log in using my username and password so that I can access the system.

US2 – Logout 
Story: As a user (and admin), I want to log out so that my session ends securely.
	
US3 – View categories to start booking 
Story: As a user, I want to see a list of booking categories so that I can choose the type of service I want.

US4 – View available appointment slots per category 
Story: As a user, I want to view available time slots for a selected category so that I can select a suitable time.

US5 – Seed/generate slots automatically 
Story: As a system, I want to generate time slots for upcoming days so that users can book without manual slot creation.

US6 – Keep me logged in / remember username 
Story: As a user, I want the system to remember my username so that I can log in faster next time.

US7 – Forgot password recovery
Story: As a user, I want to recover my password if I forget it so that I can access my account again.

*/