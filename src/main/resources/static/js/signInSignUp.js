document.addEventListener('DOMContentLoaded', function() {
    const adminLogInForm = document.querySelector('section');
    adminLogInForm.style.opacity = 0;

    setTimeout(() => {
        adminLogInForm.style.transition = 'opacity 0.5s ease-in-out';
        adminLogInForm.style.opacity = 1;
    }, 500);
});

// Toggle password visibility
function togglePassword(fieldId, icon) {
    const field = document.getElementById(fieldId);
    if (field.type === "password") {
        field.type = "text";
        icon.name = "eye-outline";
    } else {
        field.type = "password";
        icon.name = "eye-off-outline";
    }
}