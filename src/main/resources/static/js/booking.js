const extraBedOption = document.getElementById('extra-bed');
const params = new URLSearchParams(window.location.search);
const bookingId = params.get("bookingId");
let editMode = false;
let booking;
let calendar;
let guestcount;
const bookingState = {
    selectedDates: null,
    nights: 0,
    cost: 0,
};
document.addEventListener("DOMContentLoaded", async () => {
    document.getElementById('book-button').addEventListener('click', handleBookingClick);
    booking = await getBookingData();
    await initializeCalendar();
    showGuestSection();
    if (booking) {
        applyBookingInfo(booking);
    }
});
async function getBookingData() {
    const storedBooking = sessionStorage.getItem("ongoingBooking");
    if (storedBooking != null) {
        sessionStorage.removeItem("ongoingBooking");
        return JSON.parse(storedBooking);
    }
    if (!bookingId) {
        const prefillStart = document.getElementById("prefill-startdate")?.value;
        const prefillEnd = document.getElementById("prefill-enddate")?.value;
        if (prefillStart && prefillEnd) {
            return {
                startdate: prefillStart,
                enddate: prefillEnd,
            };
        }
        return null;
    }
    editMode = true;
    const response = await fetch(`/bookings/${bookingId}`);
    if (!response.ok) {
        throw new Error("Failed to fetch booking");
    }
    return await response.json();
}

async function initializeCalendar() {
    const roomId = document.getElementById("room-id").value;
    const response = await fetch(`/bookings/room/${roomId}/blocked-dates`);
    if (!response.ok) {
        throw new Error("API error");
    }
    const bookings = await response.json();
    let filteredBookings = bookings;
    if (editMode) {
        filteredBookings = bookings.filter(b => b.id !== Number(bookingId));
    }
    const disabledDates = filteredBookings.filter(b => b.startdate && b.enddate).map(b => ({
        from: b.startdate,
        to: b.enddate
    }));
    calendar = flatpickr("#calendar", {
        mode: "range",
        inline: true,
        minDate: "today",
        disable: disabledDates,
        onChange(selectedDates, dateStr) {
            if (selectedDates.length === 2) {
                bookingState.selectedDates = [
                    new Date(selectedDates[0]),
                    new Date(selectedDates[1])
                ];
                document.getElementById("booking-dates").textContent = dateStr;
                updateTotalPrice(bookingState.selectedDates);
                updateExtraBedCheckbox();
                showBookingButton();
            }
        }
    });
}

function updateExtraBedCheckbox() {
    const guestCount = Number(guestcount.value);
    if (!extraBedOption) return;
    if (guestCount === 3) {
        extraBedOption.checked = true;
        extraBedOption.disabled = true;
    } else {
        extraBedOption.checked = false;
        extraBedOption.disabled = false;
    }
}

function applyBookingInfo(booking) {
    const start = new Date(booking.startdate);
    const end = new Date(booking.enddate);
    calendar.setDate([start, end]);
    bookingState.selectedDates = [start, end];
    document.getElementById("booking-dates").textContent = `${booking.startdate} → ${booking.enddate}`;
    if (booking.guestcount != null) {
        guestcount.value = booking.guestcount;
    }
    if (extraBedOption) {
        extraBedOption.checked = booking.extrabed;
    }
    showBookingButton();
    document.getElementById("book-button").textContent = "Save changes";
    updateTotalPrice(bookingState.selectedDates)
    updateExtraBedCheckbox();
}

function showGuestSection() {
    const guestSection = document.getElementById('guest-section');
    guestSection.style.display = "block";
    let options = `<option value="1">1</option> <option value="2">2</option>`;
    if (extraBedOption) {
        options += `<option value="3">3</option>`;
    }
    guestSection.innerHTML += `<select id="guest-count">${options}</select>`;
    guestcount = document.getElementById('guest-count');
    guestcount.addEventListener('change', () => {
        if (bookingState.selectedDates) {
            updateTotalPrice(bookingState.selectedDates);
        }
        updateExtraBedCheckbox();
    });
    if (extraBedOption) {
        extraBedOption.addEventListener('change', () => {
            if (bookingState.selectedDates) {
                updateTotalPrice(bookingState.selectedDates);
            }
        });
    }
}

function showBookingButton() {
    document.getElementById('book-button').style.display = "block";
}

function updateTotalPrice(selectedDates) {
    const roomPrice = Number(document.getElementById('room-price').value);
    if (selectedDates.length > 1) {
        const firstDate = selectedDates[0];
        const lastDate = selectedDates[1];
        const millisecondsPerDay = 1000 * 60 * 60 * 24;
        bookingState.nights = Math.round((lastDate - firstDate) / millisecondsPerDay);
    }
    let extraBedFee = 0;
    if (extraBedOption && extraBedOption.checked) {
        extraBedFee = 250;
    }
    const totalPrice = bookingState.nights * (roomPrice + extraBedFee);
    document.getElementById("booking-total").textContent = `${totalPrice} SEK`;
    bookingState.cost = totalPrice;
}

function handleBookingClick() {
    const guestCount = Number(guestcount.value);
    if (guestCount < 1) {
        showErrorModal("Please select at least one guest.", document.getElementById("room-id").value);
        return;
    }
    const formatDate = (d) => {
        const year = d.getFullYear();
        const month = String(d.getMonth() + 1).padStart(2, "0");
        const day = String(d.getDate()).padStart(2, "0");
        return `${year}-${month}-${day}`;
    };
    const bookingRequest = {
        roomid: document.getElementById("room-id").value,
        startdate: formatDate(bookingState.selectedDates[0]),
        enddate: formatDate(bookingState.selectedDates[1]),
        guestcount: guestcount.value,
        cost: bookingState.cost,
        extrabed: extraBedOption ? extraBedOption.checked : false
    };
    let url = editMode ? `/bookings/${bookingId}` : "/bookings";
    let method = editMode ? "PUT" : "POST";
    fetch(url, {method: method, headers: {"Content-Type": "application/json"}, body: JSON.stringify(bookingRequest)})
        .then(response => {
            if (!response.ok) {
                throw new Error("Booking failed with status " + response.status);
            }
            return response.json();})
        .then(data => {showConfirmationModal(data);})
        .catch(error => {
            sessionStorage.setItem("ongoingBooking", JSON.stringify(bookingRequest));
            console.error(error);
            showErrorModal("You must be logged in to book a room. Please log in or create a free account to continue.",
                bookingRequest.roomid);});
}

function showErrorModal(message, roomId) {
    const modalElement = document.getElementById('myModal');
    const modalTitle = document.querySelector(".modal-title");
    const modalBody = document.getElementById('modalBody');
    const modalFooter = document.querySelector(".modal-footer");
    modalTitle.innerHTML = `${message}`;
    modalBody.innerHTML = `<p style="color: #5a514d; font-size: 1.1rem; text-align: center; margin-top: 15px;">${message}</p>`;
    modalFooter.innerHTML = `
        <a href="/customer?returnToBook=true&roomId=${roomId}" class="modal-btn modal-btn-primary text-decoration-none text-center" style="width: 100%; margin-bottom: 8px;">Log In / Sign Up</a>
        <button class="modal-btn modal-btn-secondary w-100" data-bs-dismiss="modal">Close</button>`;
    const newModalElement = modalElement.cloneNode(true);
    modalElement.parentNode.replaceChild(newModalElement, modalElement);
    const freshModal = new bootstrap.Modal(newModalElement);
    freshModal.show();
}

function showConfirmationModal(data) {
    const modalElement = document.getElementById('myModal');
    const modal = new bootstrap.Modal(modalElement);
    const modalBody = document.getElementById('modalBody');
    modalBody.innerHTML = ` <img src="/images/rooms/room_${data.roomid}_1.jpg" class="booking-thumbnail" alt="Room image">
        <h5>Room ${data.roomid}</h5>
        <p><strong>Dates:</strong><br> ${data.startdate} → ${data.enddate}</p>
        <p><strong>Guests:</strong>${data.guestcount}</p>${data.extrabed ? `
        <p><strong>Extra bed:</strong> Yes</p>` : ''}
        <p><strong>Total cost:</strong> ${data.cost} SEK</p>`;
    const modalTitle = document.querySelector(".modal-title");
    modalTitle.innerHTML = editMode ? "Your booking has been updated" : "Booking confirmed"
    const modalFooter = document.querySelector(".modal-footer");
    modalFooter.innerHTML = `<button class="modal-btn modal-btn-primary" data-bs-dismiss="modal"> Close</button>`;
    const reroute = editMode ? "/profile" : "/home";
    modalElement.addEventListener("hidden.bs.modal", () => {window.location.href = reroute;}, {once: true});
    modal.show();
}