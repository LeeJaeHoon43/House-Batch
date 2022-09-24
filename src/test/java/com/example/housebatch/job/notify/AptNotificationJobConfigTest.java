package com.example.housebatch.job.notify;

import com.example.housebatch.BatchTestConfig;
import com.example.housebatch.adapter.FakeSendService;
import com.example.housebatch.core.dto.AptDto;
import com.example.housebatch.core.entity.AptNotification;
import com.example.housebatch.core.entity.Lawd;
import com.example.housebatch.core.repository.AptNotificationRepository;
import com.example.housebatch.core.repository.LawdRepository;
import com.example.housebatch.core.service.AptDealService;
import org.assertj.core.util.Maps;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.*;

@SpringBatchTest
@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@ContextConfiguration(classes = {AptNotificationJobConfig.class, BatchTestConfig.class})
public class AptNotificationJobConfigTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private AptNotificationRepository aptNotificationRepository;

    @MockBean
    private AptDealService aptDealService;

    @MockBean
    private LawdRepository lawdRepository;

    @MockBean
    private FakeSendService fakeSendService;

    @AfterEach
    public void tearDown(){
        aptNotificationRepository.deleteAll();
    }

    @Test
    public void success() throws Exception{
        // given
        LocalDate dealDate = LocalDate.now().minusDays(1);
        givenAptNotification();
        givenLawdCd();
        givenAptDeal();

        // when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(
                new JobParameters(Maps.newHashMap("dealDate", new JobParameter(dealDate.toString())))
        );

        // then
        assertEquals(jobExecution.getExitStatus(), ExitStatus.COMPLETED);
        verify(fakeSendService, times(1)).send(anyString(), anyString());

    }

    private void givenAptNotification(){
        AptNotification notification = new AptNotification();
        notification.setEmail("abc@gmail.com");
        notification.setGuLawdCd("11110");
        notification.setEnabled(true);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setUpdatedAt(LocalDateTime.now());
        aptNotificationRepository.save(notification);
    }

    private void givenLawdCd(){
        Lawd lawd = new Lawd();
        lawd.setLawdCd("1111000000");
        lawd.setLawdDong("경기도 성남시 분당구");
        lawd.setExist(true);
        lawd.setCreatedAt(LocalDateTime.now());
        lawd.setUpdatedAt(LocalDateTime.now());
        when(lawdRepository.findByLawdCd("1111000000")).thenReturn(Optional.of(lawd));
    }

    private void givenAptDeal(){
        when(aptDealService.findByGuLawdCdAndDealDate("11110", LocalDate.now().minusDays(1)))
                .thenReturn(Arrays.asList(
                        new AptDto("IT 아파트", 2000000000L),
                        new AptDto("배치아파트", 1500000000L)
                ));
    }
}