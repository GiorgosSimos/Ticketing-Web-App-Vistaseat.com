// function to switch between History tabs in Admin Dashboard
function openTab(button, tabName) {
    const contents = document.querySelectorAll(".tab-content");
    const buttons = document.querySelectorAll(".tab-button");

    contents.forEach(content => content.style.display = "none");
    buttons.forEach(btn => btn.classList.remove("active"));

    document.getElementById(tabName).style.display = "block"
    button.classList.add("active");
}

document.addEventListener('DOMContentLoaded',  () => {
    const avatar = document.getElementById('adminAvatar');
    const adminDropdown = document.getElementById('adminDropdown');

    avatar.addEventListener('click', e => {
        e.stopPropagation();
        adminDropdown.classList.toggle('open');
    });

    document.addEventListener('click', () => {
        adminDropdown.classList.remove('open');
    });

});

// Pop up alert for Actions buttons
document.addEventListener("DOMContentLoaded", function () {
    const actionForms = document.querySelectorAll('.confirm-form-submission');
    actionForms.forEach(form => {
        form.addEventListener('submit', function (event) {
            const message = form.getAttribute('data-message');
            if (!confirm(message)) {
                event.preventDefault();
            }
        });
    });
});

// Function to fade out and remove an alert.
// If the success alert is on the page, wait 3 seconds,
// then fade it out over 0.5 seconds, and finally remove it completely
function autoDismissAlert(element, delay = 3000, fadeDuration = 500) {
    if (element) {
        setTimeout(() => {
            element.style.transition = `opacity ${fadeDuration}ms`;
            element.style.opacity = 0;
            setTimeout(() => element.remove(), fadeDuration);
        }, delay);
    }
}

document.addEventListener('DOMContentLoaded', function () {
    const successAlert = document.getElementById('success-alert');
    const accessDeniedAlert = document.getElementById('access-denied-alert');
    autoDismissAlert(successAlert);
    autoDismissAlert(accessDeniedAlert);
});
