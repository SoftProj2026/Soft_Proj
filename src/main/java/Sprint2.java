/*In Sprint 2, we implemented the appointment booking workflow and enforced key business rules. The booking flow starts from the UI (MainDashboardFrame), where the user selects a slot and inputs participants and duration. The system then creates an Appointment and submits it to BookingService.book().
US2.1 (Book appointment) is completed by confirming the appointment through Appointment.confirm(), which sets the appointment status to CONFIRMED and marks the selected TimeSlot as booked, followed by saving the appointment in the repository.
US2.2 (Enforce visit duration rule) is implemented in DurationRule, rejecting appointments that exceed the allowed maximum duration. US2.3 (Enforce participant limit) is implemented in ParticipantLimitRule, rejecting bookings that exceed the maximum number of participants. The booking service applies these rules using the Strategy pattern (BookingRuleStrategy) and returns a clear success or failure message to the user.

*/

/*
 *  Sprint 2 – User Stories Summary
 US1 – Book an appointment 
Story: As a user, I want to book an appointment for a chosen time slot so that I can reserve it.

US2 – Confirm booking marks slot unavailable
•	Story: As a system, I want confirmed appointments to mark the time slot as booked so that no one else can select it.

US3 – Enforce duration rule 
Story: As a system, I want to limit appointment duration so that bookings do not exceed the allowed time.

US4 – Enforce participant limit 
Story: As a system, I want to limit the number of participants per appointment so that capacity rules are respected.

US5 – Prevent booking blocked break time 
Story: As a system, I want to block bookings during break time (12:00–13:00) so that no appointments are scheduled during that period.

US6 – Prevent overlapping bookings 
Story: As a system, I want to prevent overlapping appointments so that schedules stay consistent.

US7 – View my bookings 
Story: As a user, I want to view my booked appointments so that I can track my reservations.

US8 – Cancel a confirmed booking and free the slot 
Story: As a user, I want to cancel a confirmed booking so that the time slot becomes available again.

US9– Register a new user 
Story: As a new user, I want to create an account so that I can log in and book appointments.

US10– Validate strong password 
Story: As a system, I want to enforce strong passwords so that accounts are more secure.

US11– Enforce age >= 18 
Story: As a system, I want to restrict registration to users 18+ so that policy requirements are met.

 */

