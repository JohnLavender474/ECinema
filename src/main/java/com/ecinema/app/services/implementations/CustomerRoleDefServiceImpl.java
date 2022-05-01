package com.ecinema.app.services.implementations;

import com.ecinema.app.domain.entities.CustomerRoleDef;
import com.ecinema.app.domain.entities.PaymentCard;
import com.ecinema.app.domain.entities.Review;
import com.ecinema.app.domain.entities.Ticket;
import com.ecinema.app.repositories.CustomerRoleDefRepository;
import com.ecinema.app.services.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class CustomerRoleDefServiceImpl extends UserRoleDefServiceImpl<CustomerRoleDef,
        CustomerRoleDefRepository> implements CustomerRoleDefService {

    private final ReviewService reviewService;
    private final TicketService ticketService;
    private final CouponService couponService;
    private final PaymentCardService paymentCardService;

    public CustomerRoleDefServiceImpl(CustomerRoleDefRepository repository, ReviewService reviewService,
                                      TicketService ticketService, PaymentCardService paymentCardService,
                                      CouponService couponService) {
        super(repository);
        this.reviewService = reviewService;
        this.ticketService = ticketService;
        this.couponService = couponService;
        this.paymentCardService = paymentCardService;
    }

    @Override
    protected void onDelete(CustomerRoleDef customerRoleDef) {
        // detach User
        super.onDelete(customerRoleDef);
        // cascade delete Reviews
        reviewService.deleteAll(customerRoleDef.getReviews());
        // cascade delete Tickets
        ticketService.deleteAll(customerRoleDef.getTickets());
        // cascade delete PaymentCards
        paymentCardService.deleteAll(customerRoleDef.getPaymentCards());
        // cascade delete Coupons
        couponService.deleteAll(customerRoleDef.getCoupons());
    }

    @Override
    public Optional<CustomerRoleDef> findByPaymentCardsContains(PaymentCard paymentCard) {
        return repository.findByPaymentCardsContains(paymentCard);
    }

    @Override
    public Optional<CustomerRoleDef> findByPaymentCardsContainsWithId(Long paymentCardId) {
        return repository.findByPaymentCardsContainsWithId(paymentCardId);
    }

    @Override
    public Optional<CustomerRoleDef> findByTicketsContains(Ticket ticket) {
        return repository.findByTicketsContains(ticket);
    }

    @Override
    public Optional<CustomerRoleDef> findByTicketsContainsWithId(Long ticketId) {
        return repository.findByTicketsContainsWithId(ticketId);
    }

    @Override
    public Optional<CustomerRoleDef> findByReviewsContains(Review review) {
        return repository.findByReviewsContains(review);
    }

    @Override
    public Optional<CustomerRoleDef> findByReviewsContainsWithId(Long reviewId) {
        return repository.findByReviewsContainsWithId(reviewId);
    }

}
