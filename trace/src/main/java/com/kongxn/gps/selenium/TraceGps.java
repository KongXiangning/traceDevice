package com.kongxn.gps.selenium;

import com.kongxn.gps.entity.AccountEntity;
import com.kongxn.gps.repository.AccountRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.util.List;
import java.util.concurrent.*;

@Component
@Log4j2
public class TraceGps {

    private final AccountRepository accountRepository;
    private final SeleniumAppFactory seleniumAppFactory;

    private static final ExecutorService executorService = new ThreadPoolExecutor(2, 2, 1, TimeUnit.MINUTES,new SynchronousQueue<Runnable>());
    private static final SynchronousQueue<AccountEntity> queue = new SynchronousQueue<>();
    public static Semaphore startThread = new Semaphore(1);

    public TraceGps(AccountRepository accountRepository, SeleniumAppFactory seleniumAppFactory) {
        this.accountRepository = accountRepository;
        this.seleniumAppFactory = seleniumAppFactory;
    }

    public void start(){
        List<AccountEntity> accountEntities = accountRepository.findAll();
        executorService.execute(this::appStart);
        //todo try优化
        for (AccountEntity account : accountEntities) {
            try {
                if (account.getStatus() == 1){
                    queue.put(account);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void appStart(){
        while (true){
            try {
                startThread.tryAcquire(10, TimeUnit.MINUTES);
                AccountEntity account = queue.take();
                executorService.execute(() -> {
                    try {
                        log.info("{} 平台 {} 用户的设备：{} 开始查询。",account.getPlatform(),account.getUsername(),account.getDeviceId());
                        seleniumAppFactory.getSelenium(account).start();
                    } catch (MalformedURLException e) {
                        log.error(e);
                    }
                });
            } catch (InterruptedException e) {
                log.error(e);
            }
        }
    }

    public static void restart(AccountEntity accountEntity){
        try {
            queue.put(accountEntity);
        } catch (InterruptedException e) {
            log.error(e);
        }
    }


}
