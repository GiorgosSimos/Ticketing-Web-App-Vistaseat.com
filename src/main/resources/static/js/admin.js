function openTab(button, tabName) {
    const contents = document.querySelectorAll(".tab-content");
    const buttons = document.querySelectorAll(".tab-button");

    contents.forEach(content => content.style.display = "none");
    buttons.forEach(btn => btn.classList.remove("active"));

    document.getElementById(tabName).style.display = "block"
    button.currentTarget.classList.add("active");
}

document.addEventListener("DOMContentLoaded", function () {
    const dropdown = document.querySelector(".admin-dropdown");
    const menu = document.querySelector(".dropdown-menu");

    dropdown.addEventListener("click", function (e) {
        e.stopPropagation();
        menu.style.display = (menu.style.display === "block" ) ? "block" : "none";
    });

    document.addEventListener("click", function (e) {
        menu.style.display = "none";
    })
});