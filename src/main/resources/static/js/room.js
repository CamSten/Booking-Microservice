const roomNumber = document.getElementById("room-number").value;
const seeReviewsButton = document.getElementById("see-reviews-button");
seeReviewsButton.addEventListener("click", () => showReviews())

async function showReviews() {
    const roomId = document.getElementById("room-id").value;
    const response = await getReviewsForRoom(roomId);
    const reviewCollection = await response.json();
    showReviewModal(reviewCollection);
}

function showReviewModal(reviewCollection){
    const modalElement = document.getElementById('myModal');
    const modal = new bootstrap.Modal(modalElement);
    const modalBody = document.getElementById('modalBody');
    const modalFooter = document.querySelector(".modal-footer");
    if (!reviewCollection || !reviewCollection.reviews || reviewCollection.reviews.length === 0){
        modalBody.innerHTML = `<p class="text-center">There are no reviews for this room yet. </p>`;
    }
    else {
        createModalContents(modalBody, reviewCollection);
    }
    modalFooter.innerHTML = `<button class="modal-btn modal-btn-primary" data-bs-dismiss="modal">Close</button>`;
    modal.show();
}

function createModalContents(modalBody, reviewCollection){
    const average = reviewCollection.average;
    const reviews = reviewCollection.reviews;
    const totalReviews = reviewCollection.totalReviews;
    const capInfo = totalReviews > reviews.length ? "Showing the latest 10 reviews" : "";
    modalBody.innerHTML = `<h2 class="reviews-title">Reviews for room ${roomNumber}</h2>
        <div class="average-rating">${createStars(average)}<span>${average.toFixed(1)}/5</span></div>
        <p class="review-count">Based on ${totalReviews} reviews</p>
        <p>${capInfo}</p>
    <div class="reviews-container"></div>`;
    const container = modalBody.querySelector(".reviews-container");
    reviews.forEach(review => {container.appendChild(getReviewCard(review));});
}

function getReviewCard(review){
    const card = document.createElement("div");
    card.classList.add("review-card");
    card.innerHTML = `<div class="review-rating">${createStars(review.stars)}</div>
        <p class="review-comment"></p>
        <div class="review-details">
            <div> <span class="review-label">Stayed: </span>${review.startdate} - ${review.enddate}</div>
            <div><span class="review-label">Submitted by: </span>${review.customer} on ${review.submitdate}</div>
        </div>`;
    card.querySelector(".review-comment").textContent = review.comments;
    return card;
}

function createStars(number) {
    let stars = "";
    for (let i = 1; i <= 5; i++) {
        stars += (i <= number) ? `<i class="fa-solid fa-star filled-star"></i>` : `<i class="fa-regular fa-star empty-star"></i>`;
    }
    return stars;
}

async function getReviewsForRoom(roomId) {
    return await fetch(`http://localhost:8082/reviews/room/${roomId}`);
}