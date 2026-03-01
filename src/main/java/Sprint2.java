/*In Sprint 2, we implemented the appointment booking workflow and enforced key business rules. The booking flow starts from the UI (MainDashboardFrame), where the user selects a slot and inputs participants and duration. The system then creates an Appointment and submits it to BookingService.book().
US2.1 (Book appointment) is completed by confirming the appointment through Appointment.confirm(), which sets the appointment status to CONFIRMED and marks the selected TimeSlot as booked, followed by saving the appointment in the repository.
US2.2 (Enforce visit duration rule) is implemented in DurationRule, rejecting appointments that exceed the allowed maximum duration. US2.3 (Enforce participant limit) is implemented in ParticipantLimitRule, rejecting bookings that exceed the maximum number of participants. The booking service applies these rules using the Strategy pattern (BookingRuleStrategy) and returns a clear success or failure message to the user.

*/

/*
 *  Sprint 2 – User Stories Summary
US2.1 – Book an appointment
Story: As a user, I want to book an appointment for a chosen time slot so that I can reserve it.
Acceptance:

User can choose a mutual available slot and submit a booking.
If all validation rules pass, the booking is saved in the repository.
User receives a success message; otherwise an error message is shown.
Code evidence: MutualBookingFrame.bookSelected(), BookingService.book(), DataRepository.addAppointment().

US2.2 – Confirm booking marks slot unavailable
Story: As a system, I want confirmed appointments to mark the time slot as booked so that no one else can select it.
Acceptance:

When booking succeeds, appointment status becomes CONFIRMED.
The associated TimeSlot becomes unavailable (isAvailable() == false).
Booked slots do not appear in “available slots” lists.
Code evidence: Appointment.confirm(), TimeSlot.book(), availability filters in UI frames.

US2.3 – Enforce duration rule (max duration)
Story: As a system, I want to limit appointment duration so that bookings do not exceed the allowed time.
Acceptance:

If appointment duration is greater than the configured max (currently 60 minutes), booking is rejected.
User receives an error message explaining the duration rule.
Code evidence: DurationRule(60) in BookingService.

US2.4 – Enforce participant limit
Story: As a system, I want to limit the number of participants per appointment so that capacity rules are respected.
Acceptance:

If participants exceed the configured maximum (currently 5), booking is rejected.
User receives an error message explaining the limit.
Code evidence: ParticipantLimitRule(5) in BookingService, participants prompt in MutualBookingFrame.

US2.5 – Prevent booking during blocked break time (12:00–13:00)
Story: As a system, I want to block bookings during break time (12:00–13:00) so that no appointments are scheduled during that period.
Acceptance:

Any slot starting at 12:00 up to before 13:00 is blocked.
Booking attempts in that window are rejected with a clear message.
Break-blocked slots are visually highlighted in the company availability view.
Code evidence: BlockedSlotsRule, used in BookingService and in CompanyAvailableSlotsFrame and MutualBookingFrame filtering.

US2.6 – Prevent overlapping bookings
Story: As a system, I want to prevent overlapping appointments so that schedules stay consistent.
Acceptance:

Booking is rejected if the chosen slot overlaps any existing appointment in the repository.
User receives an overlap error message.
Code evidence: OverlapRule(repo) in BookingService.

Note: your OverlapRule currently checks overlap against all appointments in repo.getAppointments() (it doesn’t filter by status). That’s how the code behaves.

US2.7 – Show mutual slots only (company available + user free + not break-blocked)
Story: As a user, I want to see only mutual time slots so that I can quickly book times that work for me and the company.
Acceptance:

Mutual booking screen shows slots only when:
Slot matches selected category.
Slot is available (not booked).
Slot does not overlap with the user’s confirmed bookings.
Slot is not within the break-blocked period.
If no mutual slots exist, show a “no mutual slots available” message.
Code evidence: MutualBookingFrame.load(), isUserBusy(...), BlockedSlotsRule.getBlockMessageIfBlocked(...).

US2.8 – Require an Emergency booking after the Main booking (per category)
Story: As a user, I want the system to require an Emergency booking after I book the Main appointment so that I always have a backup reservation.
Acceptance:

If user has 0 confirmed bookings in the category → next booking is MAIN.
If user has 1 confirmed booking in the category → next booking must be EMERGENCY.
User cannot close the mutual booking window after MAIN until EMERGENCY is booked.
After MAIN + EMERGENCY (2 confirmed) → no more bookings allowed in that category and closing is permitted.
Code evidence: MutualBookingFrame.countConfirmedForThisCategory(), attemptClose(), booking flow messages.
 */

