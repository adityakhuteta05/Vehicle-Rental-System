package com.drivesense.event;

import com.drivesense.entity.Review;

public class ReviewSubmittedEvent {

    private final Review review;

    public ReviewSubmittedEvent(Review review) {
        this.review = review;
    }

    public Review getReview() {
        return review;
    }
}
