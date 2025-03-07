document.addEventListener('DOMContentLoaded', function() {
    const adminLogInForm = document.querySelector('section');
    adminLogInForm.style.opacity = 0;

    setTimeout(() => {
        adminLogInForm.style.transition = 'opacity 0.5s ease-in-out';
        adminLogInForm.style.opacity = 2;
    }, 500);
});