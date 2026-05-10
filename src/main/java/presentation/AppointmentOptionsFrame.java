package presentation;

import domain.Appointment;
import domain.AppointmentStatus;
import domain.AppointmentType;
import domain.TimeSlot;
import persistence.DataRepository;
import service.AdditionalAppointmentService;
import service.AppointmentTypeRules;
import service.AppointmentTypeService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Frame for selecting and creating an additional appointment based on the original one, with various appointment type options.
 * When saving, creates a NEW appointment in a selected available slot rather than modifying the original.
 *
 * @author Qussaialaw &amp; remaa
 * @version 1.0
 */
public class AppointmentOptionsFrame extends JFrame {

    /**
     * The application's data repository.
     */
    private final DataRepository repo;

    /**
     * The appointment to base this operation on.
     */
    private final Appointment appointment;

    /**
     * Service for appointment options and notification.
     */
    private final AppointmentTypeService typeService;

    /**
     * Service for additional appointments.
     */
    private final AdditionalAppointmentService additionalAppointmentService;

    /**
     * Radio button for new appointment type.
     */
    private final JRadioButton rbNew = new JRadioButton("New appointment");

    /**
     * Radio button for emergency appointment type.
     */
    private final JRadioButton rbEmergency = new JRadioButton("Emergency");

    /**
     * Radio button for individual appointment type.
     */
    private final JRadioButton rbIndividual = new JRadioButton("Individual");

    /**
     * Radio button for group appointment type.
     */
    private final JRadioButton rbGroup = new JRadioButton("Group");

    /**
     * Label for slot selection.
     */
    private final JLabel slotLabel = new JLabel("Select available slot:");

    /**
     * Combo box containing slot choices.
     */
    private final JComboBox<SlotChoice> slotsBox = new JComboBox<>();

    /**
     * Panel that holds slot selection widgets.
     */
    private final JPanel slotRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));

    /**
     * Label for participants.
     */
    private final JLabel participantsLabel = new JLabel("Participants (1 - 5):");

    /**
     * Spinner for participants count.
     */
    private final JSpinner participantsSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 5, 1));

    /**
     * Panel that holds participants widgets.
     */
    private final JPanel participantsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));

    /**
     * Label for duration.
     */
    private final JLabel durationLabel = new JLabel("Duration (minutes):");

    /**
     * Spinner for duration value.
     */
    private final JSpinner durationSpinner = new JSpinner(new SpinnerNumberModel(30, 1, 60, 1));

    /**
     * Panel holding duration row.
     */
    private final JPanel durationRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));

    /**
     * Label for group size.
     */
   // private final JLabel groupSizeLabel = new JLabel("Group size (1 - 5):");

    /**
     * Spinner for group size value.
     */
    private final JSpinner groupSizeSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 5, 1));

    /**
     * Panel holding group size.
     */
    private final JPanel groupRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));

    /**
     * Formatter for date/time values.
     */
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * Inner class for display and selection of available time slots.
     */
    private static class SlotChoice {
        final TimeSlot slot;

        /**
         * Constructs a new SlotChoice from a slot.
         * @param slot the time slot to wrap
         */
        SlotChoice(TimeSlot slot) {
            this.slot = slot;
        }

        /**
         * Returns label representation for the combo box.
         * @return slot start date/time or "N/A"
         */
        @Override
        public String toString() {
            if (slot == null || slot.getStartDateTime() == null) return "N/A";
            return slot.getStartDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        }
    }

    /**
     * Creates the Appointment Options dialog for a given appointment.
     *
     * @param parent       parent frame
     * @param repo         repository reference
     * @param appointment  the appointment to base on
     * @param typeService  the appointment-type service for notifications
     */
    public AppointmentOptionsFrame(JFrame parent,
                                   DataRepository repo,
                                   Appointment appointment,
                                   AppointmentTypeService typeService) {

        this.repo = repo;
        this.appointment = appointment;
        this.typeService = typeService;

        this.additionalAppointmentService = new AdditionalAppointmentService(repo);

        setTitle("Appointment Options");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(true);

        getContentPane().setBackground(UITheme.BG);
        setLayout(new BorderLayout(12, 12));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildCard(), BorderLayout.CENTER);
        add(buildActions(), BorderLayout.SOUTH);

        rbNew.setSelected(true);

        loadAvailableSlotsSameCategory();
        updateUiForType();
        updateDurationMaxFromSelectedSlot();

        attachListeners();

        pack();
        setMinimumSize(new Dimension(720, 520));
        setSize(740, 540);

        setLocationRelativeTo(parent);
    }

    /**
     * Constructs the header (title + description).
     * @return header panel
     */
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel title = new JLabel("Appointment Options");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        header.add(title, BorderLayout.NORTH);

        JLabel subtitle = new JLabel("Pick a future available slot (same category). Saving will create a NEW appointment.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(90, 100, 115));
        header.add(subtitle, BorderLayout.SOUTH);

        return header;
    }

    /**
     * Builds content panel for appointment type and options.
     * @return card panel
     */
    private JPanel buildCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 230, 245), 2, true),
                new EmptyBorder(14, 14, 14, 14)
        ));

        ButtonGroup bg = new ButtonGroup();
        bg.add(rbNew);
        bg.add(rbEmergency);
        bg.add(rbIndividual);
        bg.add(rbGroup);

        for (JRadioButton rb : new JRadioButton[]{rbNew, rbEmergency, rbIndividual, rbGroup}) {
            rb.setOpaque(false);
            rb.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            rb.setBorder(new EmptyBorder(6, 6, 6, 6));
        }

        slotRow.setOpaque(false);
        slotRow.add(slotLabel);
        slotRow.add(slotsBox);

        participantsRow.setOpaque(false);
        participantsRow.add(participantsLabel);
        participantsRow.add(participantsSpinner);

        durationRow.setOpaque(false);
        durationRow.add(durationLabel);
        durationRow.add(durationSpinner);

        groupRow.setOpaque(false);
       // groupRow.add(groupSizeLabel);
      //  groupRow.add(groupSizeSpinner);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        content.add(rbNew);
        content.add(rbEmergency);
        content.add(rbIndividual);
        content.add(rbGroup);

        content.add(Box.createVerticalStrut(14));

        content.add(slotRow);
        content.add(Box.createVerticalStrut(10));
        content.add(participantsRow);
        content.add(Box.createVerticalStrut(10));
        content.add(durationRow);
        content.add(Box.createVerticalStrut(10));
        content.add(groupRow);

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    /**
     * Builds the bottom bar containing Cancel and Save buttons.
     * @return the actions panel
     */
    private JPanel buildActions() {
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        actions.setBackground(UITheme.BG);

        JButton btnCancel = UITheme.secondaryButton("Cancel");
        JButton btnSave = UITheme.primaryButton("Save");

        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> onSaveCreateNewAppointment());

        actions.add(btnCancel);
        actions.add(btnSave);
        return actions;
    }

    /**
     * Attaches action listeners to interactive UI elements.
     */
    private void attachListeners() {
        rbNew.addActionListener(e -> updateUiForType());
        rbEmergency.addActionListener(e -> updateUiForType());
        rbIndividual.addActionListener(e -> updateUiForType());
        rbGroup.addActionListener(e -> updateUiForType());

        slotsBox.addActionListener(e -> updateDurationMaxFromSelectedSlot());
    }

    /**
     * Gets the currently selected appointment type from the UI.
     * @return selected type
     */
    private AppointmentType selectedType() {
        if (rbNew.isSelected()) return AppointmentType.NEW_APPOINTMENT;
        if (rbEmergency.isSelected()) return AppointmentType.EMERGENCY;
        if (rbIndividual.isSelected()) return AppointmentType.INDIVIDUAL;
        return AppointmentType.GROUP;
    }

    /**
     * Updates UI to reflect selected appointment type, hiding/showing fields as appropriate.
     */
    private void updateUiForType() {
        AppointmentType type = selectedType();

        boolean isIndividual = (type == AppointmentType.INDIVIDUAL);

        participantsRow.setVisible(!isIndividual);
        if (isIndividual) participantsSpinner.setValue(1);

        groupRow.setVisible(type == AppointmentType.GROUP);

        revalidate();
        repaint();
    }

    /**
     * Loads all available future slots for the same category as the current appointment.
     */
    private void loadAvailableSlotsSameCategory() {
        slotsBox.removeAllItems();

        String categoryName = (appointment.getSlot() != null && appointment.getSlot().getCategory() != null)
                ? appointment.getSlot().getCategory().getName()
                : null;

        if (categoryName == null || categoryName.trim().isEmpty()) return;

        LocalDateTime now = LocalDateTime.now();

        List<TimeSlot> options = new ArrayList<>();
        for (TimeSlot s : repo.getSlots()) {
            if (s == null || s.getStartDateTime() == null) continue;
            if (s.getCategory() == null || s.getCategory().getName() == null) continue;

            if (!s.getCategory().getName().equalsIgnoreCase(categoryName)) continue;
            if (!s.isAvailable()) continue;
            if (!s.getStartDateTime().isAfter(now)) continue;

            options.add(s);
        }

        for (TimeSlot s : options) {
            slotsBox.addItem(new SlotChoice(s));
        }
    }

    /**
     * Updates the maximum allowed value for duration based on the slot selected.
     */
    private void updateDurationMaxFromSelectedSlot() {
        SlotChoice ch = (SlotChoice) slotsBox.getSelectedItem();
        if (ch == null || ch.slot == null || ch.slot.getStartDateTime() == null || ch.slot.getEndDateTime() == null) {
            setDurationSpinnerMax(60);
            return;
        }

        long mins = Duration.between(ch.slot.getStartDateTime(), ch.slot.getEndDateTime()).toMinutes();
        int max = (int) Math.max(1, mins);
        setDurationSpinnerMax(max);
    }

    /**
     * Sets the spinner's max value for duration and updates selected value safely.
     * @param maxMinutes maximum allowed minutes
     */
    private void setDurationSpinnerMax(int maxMinutes) {
        int current = (Integer) durationSpinner.getValue();
        int safeCurrent = Math.max(1, Math.min(current, maxMinutes));
        durationSpinner.setModel(new SpinnerNumberModel(safeCurrent, 1, maxMinutes, 1));
    }

    /**
     * Handles "Save" logic: creates a new appointment in the selected available slot with options.
     * Does NOT modify the original appointment.
     */
    private void onSaveCreateNewAppointment() {
        if (appointment == null) {
            DialogUtil.show(this, "Error", "Invalid appointment.", DialogUtil.Type.ERROR);
            return;
        }

        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            DialogUtil.show(this, "Not Allowed", "Only CONFIRMED appointments can be used here.", DialogUtil.Type.WARNING);
            return;
        }

        SlotChoice ch = (SlotChoice) slotsBox.getSelectedItem();
        TimeSlot newSlot = (ch != null) ? ch.slot : null;

        if (newSlot == null) {
            DialogUtil.show(this, "Missing Slot", "Please select an available slot.", DialogUtil.Type.WARNING);
            return;
        }

        AppointmentType type = selectedType();

        int duration = (Integer) durationSpinner.getValue();
        int participants = (type == AppointmentType.INDIVIDUAL) ? 1 : (Integer) participantsSpinner.getValue();
        Integer groupSize = (type == AppointmentType.GROUP) ? (Integer) groupSizeSpinner.getValue() : null;

        String uiRules = AppointmentTypeRules.validate(type, duration, participants, groupSize);
        if (!"OK".equals(uiRules)) {
            DialogUtil.show(this, "Validation", uiRules, DialogUtil.Type.WARNING);
            return;
        }

        LocalDateTime chosenStart = newSlot.getStartDateTime();

        String msg = additionalAppointmentService.createNewAppointment(
                appointment.getUser(),
                newSlot,
                duration,
                participants,
                type,
                groupSize,
                (type == AppointmentType.EMERGENCY) ? chosenStart : null
        );

        if (!"Saved.".equals(msg)) {
            DialogUtil.show(this, "Result", msg, DialogUtil.Type.ERROR);
            return;
        }

        DialogUtil.show(this, "Saved", "New appointment created successfully.", DialogUtil.Type.SUCCESS);
        dispose();
    }
}