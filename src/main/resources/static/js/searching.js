document.addEventListener("DOMContentLoaded", () => {
    initializeCalendar();
});
const bookingState = {
    selectedDates: null,
};
function formatDate(date) {
    return date.toISOString().split("T")[0];
}
function initializeCalendar() {
    flatpickr("#calendar", {
        mode: "range",
        inline: true,
        minDate: "today",
        onChange: function (selectedDates) {
            bookingState.selectedDates = selectedDates;
            if (selectedDates.length === 2) {
                showSearchButton();
            } else {
                hideSearchButton();
            }
        }
    });
}
function showSearchButton(){
    document.getElementById('search-button').style.display = "block";
}

function searchRooms(){
    const startdate = formatDate(bookingState.selectedDates[0]);
    const enddate = formatDate(bookingState.selectedDates[1]);
    window.location.href = `/home?startdate=${startdate}&enddate=${enddate}`;
}

function hideSearchButton(){
    document.getElementById('search-button').style.display = "none";
}
document.getElementById('search-button').addEventListener('click', searchRooms);