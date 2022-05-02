package com.ecinema.app.services;

import com.ecinema.app.domain.entities.*;
import com.ecinema.app.repositories.*;
import com.ecinema.app.services.implementations.*;
import com.ecinema.app.utils.UtilMethods;
import com.ecinema.app.utils.UserRole;
import com.ecinema.app.domain.dtos.UserDto;
import com.ecinema.app.exceptions.ClashException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * The type User service test.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private AddressService addressService;
    private ReviewService reviewService;
    private TicketService ticketService;
    private PaymentCardService paymentCardService;
    private CouponService couponService;
    private UserService userService;
    private CustomerRoleDefService customerRoleDefService;
    private ModeratorRoleDefService moderatorRoleDefService;
    private AdminRoleDefService adminRoleDefService;
    @Mock
    private AddressRepository addressRepository;
    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private PaymentCardRepository paymentCardRepository;
    @Mock
    private CouponRepository couponRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CustomerRoleDefRepository customerRoleDefRepository;
    @Mock
    private ModeratorRoleDefRepository moderatorRoleDefRepository;
    @Mock
    private AdminRoleDefRepository adminRoleDefRepository;

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() {
        addressService = new AddressServiceImpl(addressRepository);
        reviewService = new ReviewServiceImpl(reviewRepository);
        ticketService = new TicketServiceImpl(ticketRepository);
        couponService = new CouponServiceImpl(couponRepository);
        paymentCardService = new PaymentCardServiceImpl(paymentCardRepository, addressService);
        adminRoleDefService = new AdminRoleDefServiceImpl(adminRoleDefRepository);
        customerRoleDefService = new CustomerRoleDefServiceImpl(customerRoleDefRepository,
                                                                reviewService, ticketService,
                                                                paymentCardService,
                                                                couponService);
        moderatorRoleDefService = new ModeratorRoleDefServiceImpl(moderatorRoleDefRepository,
                                                                  customerRoleDefService);
        userService = new UserServiceImpl(
                userRepository, customerRoleDefService,
                moderatorRoleDefService, adminRoleDefService);
    }

    /**
     * Generate users list list.
     *
     * @param consumer the consumer
     * @return the list
     */
    List<User> generateUsersList(Consumer<User> consumer) {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            User user = new User();
            consumer.accept(user);
            users.add(user);
        }
        return users;
    }

    /**
     * Test find by email.
     */
    @Test
    void findByEmail() {
        // given
        String email = "test@gmail.com";
        User user = new User();
        user.setEmail(email);
        given(userRepository.findByEmail(email))
                .willReturn(Optional.of(user));
        // when
        Optional<User> userOptional = userService.findByEmail(email);
        // then
        assertTrue(userOptional.isPresent() && userOptional.get().equals(user));
        verify(userRepository, times(1)).findByEmail(email);
    }

    /**
     * Test find all by is account locked.
     */
    @Test
    void findAllByIsAccountLocked() {
        // given
        List<User> users = generateUsersList(user -> user.setIsAccountLocked(UtilMethods.getRandom().nextBoolean()));
        List<User> lockedUsers = users.stream()
                                      .filter(User::getIsAccountLocked)
                                      .collect(Collectors.toList());
        given(userRepository.findAllByIsAccountLocked(true))
                .willReturn(lockedUsers);
        // when
        List<User> testLockedUsers = userService.findAllByIsAccountLocked(true);
        // then
        assertEquals(lockedUsers, testLockedUsers);
        verify(userRepository, times(1)).findAllByIsAccountLocked(true);
    }

    /**
     * Test find all by is account enabled.
     */
    @Test
    void findAllByIsAccountEnabled() {
        // given
        List<User> users = generateUsersList(user -> user.setIsAccountEnabled(UtilMethods.getRandom().nextBoolean()));
        List<User> enabledUsers = users.stream()
                                       .filter(User::getIsAccountEnabled)
                                       .collect(Collectors.toList());
        given(userRepository.findAllByIsAccountEnabled(true))
                .willReturn(enabledUsers);
        // when
        List<User> testEnabledUsers = userService.findAllByIsAccountEnabled(true);
        // then
        assertEquals(enabledUsers, testEnabledUsers);
        verify(userRepository, times(1)).findAllByIsAccountEnabled(true);
    }

    /**
     * Test add user role def to user.
     */
    @Test
    void addUserRoleDefToUser() {
        // given
        User user = new User();
        userService.save(user);
        CustomerRoleDef customerRoleDef = new CustomerRoleDef();
        customerRoleDef.setUser(user);
        user.getUserRoleDefs().put(UserRole.CUSTOMER, customerRoleDef);
        customerRoleDefService.save(customerRoleDef);
        given(customerRoleDefRepository.findByUser(user))
                .willReturn(Optional.of(customerRoleDef));
        // when
        Optional<CustomerRoleDef> testCustomerRoleDefOptional
                = customerRoleDefService.findByUser(user);
        // then
        assertTrue(testCustomerRoleDefOptional.isPresent() &&
                           customerRoleDef.equals(testCustomerRoleDefOptional.get()) &&
                           user.getUserRoleDefs().get(UserRole.CUSTOMER).equals(testCustomerRoleDefOptional.get()));
    }

    /**
     * Test remove user role def from user.
     */
    @Test
    void removeUserRoleDefFromUser() {
        // given
        User user = new User();
        userService.save(user);
        CustomerRoleDef customerRoleDef = new CustomerRoleDef();
        customerRoleDef.setUser(user);
        user.getUserRoleDefs().put(UserRole.CUSTOMER, customerRoleDef);
        customerRoleDefService.save(customerRoleDef);
        user.getUserRoleDefs().remove(UserRole.CUSTOMER);
        customerRoleDef.setUser(null);
        customerRoleDefRepository.delete(customerRoleDef);
        userService.save(user);
        // when
        when(customerRoleDefRepository.findByUser(user))
                .thenReturn(Optional.ofNullable((CustomerRoleDef) user.getUserRoleDefs().get(UserRole.CUSTOMER)));
        // then
        assertTrue(customerRoleDefService.findByUser(user).isEmpty());
    }

    /**
     * Add user role defs to user.
     */
    @Test
    void addUserRoleDefsToUser() {
        // given
        User user = new User();
        user.setId(1L);
        userService.save(user);
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        // when
        userService.addUserRoleDefToUser(
                user, UserRole.CUSTOMER, UserRole.ADMIN, UserRole.MODERATOR);
        // then
        assertTrue(user.getUserRoleDefs().containsKey(UserRole.CUSTOMER));
        assertTrue(user.getUserRoleDefs().get(UserRole.CUSTOMER) instanceof CustomerRoleDef);
        assertTrue(user.getUserRoleDefs().containsKey(UserRole.ADMIN));
        assertTrue(user.getUserRoleDefs().get(UserRole.ADMIN) instanceof AdminRoleDef);
        assertTrue(user.getUserRoleDefs().containsKey(UserRole.MODERATOR));
        assertTrue(user.getUserRoleDefs().get(UserRole.MODERATOR) instanceof ModeratorRoleDef);
    }

    /**
     * Fail to add user role def to user.
     */
    @Test
    void failToAddUserRoleDefToUser() {
        // given
        User user = new User();
        user.setId(1L);
        userService.save(user);
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        userService.addUserRoleDefToUser(user, UserRole.CUSTOMER);
        // then
        assertThrows(ClashException.class,
                     () -> userService.addUserRoleDefToUser(user, UserRole.CUSTOMER));
    }


    /**
     * Delete user and cascade.
     */
    @Test
    void deleteUserAndCascade() {
        // given
        User user = new User();
        user.setId(1L);
        given(userRepository.findById(1L))
                .willReturn(Optional.of(user));
        userService.save(user);
        AdminRoleDef adminRoleDef = new AdminRoleDef();
        adminRoleDef.setId(2L);
        adminRoleDef.setUser(user);
        user.getUserRoleDefs().put(UserRole.ADMIN, adminRoleDef);
        given(adminRoleDefRepository.findById(2L))
                .willReturn(Optional.of(adminRoleDef));
        adminRoleDefService.save(adminRoleDef);
        CustomerRoleDef customerRoleDef = new CustomerRoleDef();
        customerRoleDef.setId(3L);
        customerRoleDef.setUser(user);
        user.getUserRoleDefs().put(UserRole.CUSTOMER, customerRoleDef);
        given(customerRoleDefRepository.findById(3L))
                .willReturn(Optional.of(customerRoleDef));
        customerRoleDefService.save(customerRoleDef);
        PaymentCard paymentCard = new PaymentCard();
        paymentCard.setId(4L);
        paymentCard.setCustomerRoleDef(customerRoleDef);
        customerRoleDef.getPaymentCards().add(paymentCard);
        given(paymentCardRepository.findById(4L))
                .willReturn(Optional.of(paymentCard));
        paymentCardService.save(paymentCard);
        // then
        assertNotNull(user.getUserRoleDefs().get(UserRole.ADMIN));
        assertNotNull(user.getUserRoleDefs().get(UserRole.CUSTOMER));
        assertNotNull(adminRoleDef.getUser());
        assertNotNull(customerRoleDef.getUser());
        assertNotNull(paymentCard.getCustomerRoleDef());
        // when
        userService.delete(user);
        // then
        assertNull(user.getUserRoleDefs().get(UserRole.ADMIN));
        assertNull(user.getUserRoleDefs().get(UserRole.CUSTOMER));
        assertNull(adminRoleDef.getUser());
        assertNull(customerRoleDef.getUser());
        assertNull(paymentCard.getCustomerRoleDef());
    }

    /**
     * User dto.
     */
    @Test
    void userDto() {
        // given
        User user = new User();
        user.setId(1L);
        user.setEmail("test@gmail.com");
        user.setUsername("test");
        user.setFirstName("John");
        user.setLastName("Lavender");
        user.getUserRoleDefs().put(UserRole.CUSTOMER, null);
        userService.save(user);
        given(userRepository.findById(1L))
                .willReturn(Optional.of(user));
        // when
        UserDto userDto = userService.convertToDto(1L);
        // then
        assertEquals(user.getId(), userDto.getId());
        assertEquals(user.getEmail(), userDto.getEmail());
        assertEquals(user.getUsername(), userDto.getUsername());
        assertEquals(user.getFirstName(), userDto.getFirstName());
        assertEquals(user.getLastName(), userDto.getLastName());
        assertTrue(userDto.getUserRoles().contains(UserRole.CUSTOMER));
    }

}