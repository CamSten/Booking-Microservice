let selectedRating = 0;
let submitButton = null;

export function leaveReview(booking, modalBody, modalFooter) {
    selectedRating = 0;
    submitButton = null;
    const customerId = document.getElementById("customer-id").value;
    modalBody.innerHTML = getReviewModalBody();
    modalFooter.innerHTML = `<button type="button" class="review-submit-button" disabled>Leave review </button>`;
    const stars = modalBody.querySelectorAll(".star");
    const commentSection = modalBody.querySelector(".comment-section");
    submitButton = modalFooter.querySelector(".review-submit-button");
    stars.forEach(star => {
        star.addEventListener("click", () => {
            selectedRating = Number(star.dataset.rating);
            updateStars(stars);
            updateSubmitButton();
        });
    });
    commentSection.addEventListener("input", () => {updateSubmitButton();});
    submitButton.addEventListener("click", async () => {
        const review = {
            roomId: booking.roomid,
            customerId: customerId,
            startDate: booking.startdate,
            endDate: booking.enddate,
            rating: selectedRating,
            comment: commentSection.value
        };
        const response = await sendReview(review);
        modalBody.innerHTML = getReviewFeedback(response);
        modalFooter.innerHTML = `<button class="modal-btn modal-btn-primary" data-bs-dismiss="modal">Close</button>`;
    });
}

function getReviewModalBody(){
    return `<div class="review-card">
            <h2 class="text-center review-title">Add review</h2>
            <div class="star-container">
                <span class="star" data-rating="1">☆</span>
                <span class="star" data-rating="2">☆</span>
                <span class="star" data-rating="3">☆</span>
                <span class="star" data-rating="4">☆</span>
                <span class="star" data-rating="5">☆</span>
            </div>
            <div class="mb-3">
                <label class="form-label review-label">Share your thoughts</label>
                <textarea 
                    id="comment-section" class="comment-section" placeholder="Add a comment..." rows="4" required>
                </textarea>
            </div>
        </div>`;
}

function getReviewFeedback(response){
    return (response.ok)
        ? `<div class="text-center"><h3>Thank you!</h3><p>Your review has been submitted.</p></div>`
        : `<div class="text-center"><h3>Something went wrong</h3><p>Your review could not be submitted.</p></div>`;
}

function updateStars(stars) {
    stars.forEach(star => {
        const rating = Number(star.dataset.rating);
        star.textContent = rating <= selectedRating ? "★" : "☆";
    });
}

function updateSubmitButton() {
    const comment = document.getElementById("comment-section").value.trim();
    submitButton.disabled = !(selectedRating > 0 && comment.length > 0);
}

async function sendReview(review) {
    return await fetch("http://localhost:8082/reviews", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(review)
    });
}