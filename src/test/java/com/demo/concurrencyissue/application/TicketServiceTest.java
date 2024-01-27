package com.demo.concurrencyissue.application;

import com.demo.concurrencyissue.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Sql("classpath:/initialize.sql")
class TicketServiceTest {

    private static Ticket TICKET;
    private static User USER1;
    private static User USER2;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private TransactionTemplate transactionTemplate;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TicketHistoryRepository historyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketService ticketService;

    @BeforeEach
    void setUp() {
        TICKET = ticketRepository.getById(1L);
        USER1 = userRepository.getById(1L);
        USER2 = userRepository.getById(2L);
        transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @DisplayName("2 사용자가 동시에 티켓을 구매하면 동시성 문제가 해결된다. - synchronized")
    @Test
    void sellTicketBySynchronized() throws InterruptedException {
        // given

        // when
        ExecutorService executorService = Executors.newFixedThreadPool(2); // 2개의 스레드를 가지는 스레드풀 생성
        CountDownLatch latch = new CountDownLatch(2); // 2번의 작업 실행

        executorService.execute(() -> // 하나의 스레드가 작업을 실행하게 한다.
                transactionTemplate.execute((status -> {
                    ticketService.sellTicketBySynchronized(USER1, TICKET.getId());
                    latch.countDown();
                    return null;
                }))
        );
        executorService.execute(() -> // 두 번째 스레드가 작업을 실행하게 한다.
                transactionTemplate.execute((status -> {
                    ticketService.sellTicketBySynchronized(USER2, TICKET.getId());
                    latch.countDown();
                    return null;
                }))
        );
        latch.await(); // 두 스레드가 모두 작업을 마칠 때 까지 기다린다.
        Thread.sleep(1000);

        // then
        assertThat(ticketService.findByUser(USER1)).isPresent(); // USER1은 티켓을 발급받는다.
        assertThat(ticketService.findByUser(USER2)).isEmpty(); // USER2는 티켓을 발급받지 못한다. (이미 USER1이 발급 받았으므로)
    }

    @DisplayName("2 사용자가 동시에 티켓을 구매하면 동시성 문제가 발생한다. - 더티체킹")
    @Test
    void sellTicketByDirtyChecking() throws InterruptedException {
        // given

        // when
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);

        executorService.execute(() ->
                transactionTemplate.execute((status -> {
                    ticketService.sellTicketByDirtyChecking(USER1, TICKET.getId());
                    latch.countDown();
                    return null;
                }))
        );
        executorService.execute(() ->
                transactionTemplate.execute((status -> {
                    ticketService.sellTicketByDirtyChecking(USER2, TICKET.getId());
                    latch.countDown();
                    return null;
                }))
        );
        latch.await();
        Thread.sleep(1000);

        // then
        assertThat(historyRepository.findAll()).hasSize(1);
    }

    @DisplayName("2 사용자가 동시에 티켓을 구매하면 첫 번째 사용자만 구매한다. - select for update")
    @Test
    void sellTicketBySelectForUpdate() throws InterruptedException {
        // given

        // when
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);

        System.out.println("로직 시작");

        executorService.execute(() ->
                transactionTemplate.execute((status -> {
                    ticketService.sellTicketBySelectForUpdate(USER1, TICKET.getId());
                    latch.countDown();
                    return null;
                }))
        );
        executorService.execute(() ->
                transactionTemplate.execute((status -> {
                    ticketService.sellTicketBySelectForUpdate(USER2, TICKET.getId());
                    latch.countDown();
                    return null;
                }))
        );
        latch.await();
        Thread.sleep(1000);

        // then
        assertThat(historyRepository.findAll()).hasSize(1);
    }

    @DisplayName("2 사용자가 동시에 티켓을 구매하면 한 명만 구매한다. - update - 1")
    @Test
    void sellTicketByReadAndUpdate() throws InterruptedException {
        // given

        // when
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);

        System.out.println("로직 시작");

        executorService.execute(() ->
                transactionTemplate.execute((status -> {
                    ticketService.sellTicketByReadAndUpdate(USER1, TICKET.getId());
                    latch.countDown();
                    return null;
                }))
        );
        executorService.execute(() ->
                transactionTemplate.execute((status -> {
                    ticketService.sellTicketByReadAndUpdate(USER2, TICKET.getId());
                    latch.countDown();
                    return null;
                }))
        );
        latch.await();
        Thread.sleep(1000);

        // then
        assertThat(historyRepository.findAll()).hasSize(1);
    }

    @Disabled
    @DisplayName("2 사용자가 동시에 티켓을 구매하면 첫 번째 사용자만 구매한다. - 낙관적")
    @Test
    void sellTicketByOptimisticLock() throws InterruptedException {
        // given

        // when
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);

        System.out.println("로직 시작");

        executorService.execute(() ->
                transactionTemplate.execute((status -> {
                    ticketService.sellTicketByOptimisticLock(USER1, TICKET.getId());
                    latch.countDown();
                    return null;
                }))
        );
        executorService.execute(() ->
                transactionTemplate.execute((status -> {
                    ticketService.sellTicketByOptimisticLock(USER2, TICKET.getId());
                    latch.countDown();
                    return null;
                }))
        ); // exception 발생
        latch.await();
        Thread.sleep(1000);

        // then
        assertThat(historyRepository.findAll()).hasSize(1);
    }
}