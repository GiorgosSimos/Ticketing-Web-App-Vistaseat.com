function openTab(button, tabName) {
    const contents = document.querySelectorAll(".tab-content");
    const buttons = document.querySelectorAll(".tab-button");

    contents.forEach(content => content.style.display = "none");
    buttons.forEach(btn => btn.classList.remove("active"));

    document.getElementById(tabName).style.display = "block"
    button.currentTarget.classList.add("active");
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