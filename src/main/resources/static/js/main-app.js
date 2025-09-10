/**
 * Date Range Filter Script
 *
 * Enhances a date range input using Flatpickr, manages hidden fields for "from" and "to" dates,
 * and automatically submits the form when a full date range is selected.
 * Includes a "Clear Filter" button that removes query parameters from the URL.
 *
 * Features:
 * - Initializes a Flatpickr date range picker on #rangePicker.
 * - Preloads dates from data attributes (data-from, data-to).
 * - Fills hidden inputs (#fromHidden, #toHidden) with selected dates.
 * - Automatically submits #dateForm when two dates are selected.
 * - Prevents duplicate form submissions with a flag.
 * - Clears all query parameters when #clearFilterBtn is clicked.
 *
 * Dependencies:
 * - Flatpickr (https://flatpickr.js.org/)
 *
 * Assumes the following HTML elements exist:
 * - #rangePicker: Input element with optional data-from and data-to attributes.
 * - #fromHidden, #toHidden: Hidden inputs to store selected date values.
 * - #dateForm: Form to be submitted on date selection.
 * - #clearFilterBtn: Button to reset filters by clearing the URL.
 */

document.addEventListener('DOMContentLoaded', () => {
    // Reference to the "Clear Filter" button
    const clearFilterBtn = document.getElementById("clearFilterBtn");

    // References to form and input elements
    const form       = document.getElementById('dateForm');
    const input      = document.getElementById('rangePicker');
    const fromHidden = document.getElementById('fromHidden');
    const toHidden   = document.getElementById('toHidden');

    // Prevents multiple form submissions
    let alreadySubmitting = false;

    // Initialize Flatpickr on the range picker input
    flatpickr(input, {
        mode: "range", // Enable date range selection
        dateFormat: "Y-m-d", // Format to match backend expectations
        defaultDate: [input.dataset.from, input.dataset.to].filter(Boolean), // Preload selected range, if available
        onChange(selectedDates, _dateStr, fp) {
            if (selectedDates.length === 2) {
                const [from, to] = selectedDates;

                // Set hidden fields without converting to UTC
                fromHidden.value = fp.formatDate(from, "Y-m-d");
                toHidden.value   = fp.formatDate(to,   "Y-m-d");

                // Submit the form only once after full selection
                if (!alreadySubmitting) {
                    alreadySubmitting = true;
                    form.requestSubmit?.() ?? form.submit(); // Use requestSubmit if supported
                }
            } else {
                // Clear hidden fields if range is incomplete
                fromHidden.value = "";
                toHidden.value   = "";
            }
        }
    });

    // Add click listener to "Clear Filter" button (if it exists)
    if (clearFilterBtn) {
        clearFilterBtn.addEventListener("click", function() {
            // Get the base URL (without query parameters)
            const baseUrl = window.location.origin + window.location.pathname + "#aboutSection";
            // Redirect to the base URL, effectively clearing filters
            window.location.href = baseUrl;
        });
    }
});

document.addEventListener("DOMContentLoaded", function () {
    const successAlert = document.getElementById("success-alert");
    if (successAlert) {
        setTimeout(() => {
            successAlert.style.transition = "opacity 0.5s";
            successAlert.style.opacity = 0;
            setTimeout(() => successAlert.remove(), 500); // fully remove it
        }, 3000); // 3 seconds
    }
});
