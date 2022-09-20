package com.example.housebatch.job.apt;

import com.example.housebatch.adapter.ApartmentApiResource;
import com.example.housebatch.core.dto.AptDealDto;
import com.example.housebatch.core.repository.LawdRepository;
import com.example.housebatch.job.validator.LawdCdParameterValidator;
import com.example.housebatch.job.validator.YearMonthParameterValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.CompositeJobParametersValidator;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.batch.item.xml.builder.StaxEventItemReaderBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class AptDealInsertJobConfig {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final ApartmentApiResource apartmentApiResource;

    @Bean
    public Job aptDealInsertJob(
            Step guLawdCdStep,
            Step contextPrintStep
            // Step aptDealInsertStep
    ){
        return jobBuilderFactory.get("aptDealInsertJob")
                .incrementer(new RunIdIncrementer())
                .validator(new YearMonthParameterValidator())
                .start(guLawdCdStep)
                .on("CONTINUABLE").to(contextPrintStep).next(guLawdCdStep)
                .from(guLawdCdStep)
                .on("*").end()
                .end()
                .build();
    }

//    private JobParametersValidator aptDealJobParameterValidator(){
//        CompositeJobParametersValidator validator = new CompositeJobParametersValidator();
//        validator.setValidators(Arrays.asList(
//                new YearMonthParameterValidator()
//        ));
//        return validator;
//    }

    @JobScope
    @Bean
    public Step guLawdCdStep(Tasklet guLawdCdTasklet){
        return stepBuilderFactory.get("guLawdCdStep")
                .tasklet(guLawdCdTasklet)
                .build();
    }

    /**
     * ExecutionContext에 저장할 데이터.
     * 1. guLawdCd = 구 코드 -> 다음 스텝에서 활용할 값.
     * 2. guLawdCdList = 구 코드 리스트.
     * 3. itemCount = 남아있는 구 코드의 갯수.
     */
    @StepScope
    @Bean
    public Tasklet guLawdCdTasklet(LawdRepository lawdRepository){
        return new GuLawdTasklet(lawdRepository);
    }

    @JobScope
    @Bean
    public Step contextPrintStep(Tasklet contextPrintTasklet){
        return stepBuilderFactory.get("contextPrintStep")
                .tasklet(contextPrintTasklet)
                .build();
    }

    @StepScope
    @Bean
    public Tasklet contextPrintTasklet(@Value("#{jobExecutionContext['guLawdCd']}") String guLawdCd){
        return ((contribution, chunkContext) -> {
            System.out.println("[contextPrintStep] guLawdCd = " + guLawdCd);
            return RepeatStatus.FINISHED;
        });
    }


    @JobScope
    @Bean
    public Step aptDealInsertStep(StaxEventItemReader<AptDealDto> aptDealResourceReader, ItemWriter<AptDealDto> aptDealWriter){
        return stepBuilderFactory.get("aptDealInsertStep")
                .<AptDealDto, AptDealDto>chunk(10)
                .reader(aptDealResourceReader)
                .writer(aptDealWriter)
                .build();
    }

    @StepScope
    @Bean
    public StaxEventItemReader<AptDealDto> aptDealResourceReader(
            @Value("#{jobParameters['yearMonth']}") String yearMonth,
            @Value("#{jobExecutionContext['guLawdCd']}") String guLawdCd,
            Jaxb2Marshaller aptDealDtoMarshaller
    ){
        return new StaxEventItemReaderBuilder<AptDealDto>()
                .name("aptDealResourceReader")
                .resource(apartmentApiResource.getResource(guLawdCd, YearMonth.parse(yearMonth)))
                .addFragmentRootElements("item")
                .unmarshaller(aptDealDtoMarshaller)
                .build();
    }

    @StepScope
    @Bean
    public Jaxb2Marshaller aptDealDtoMarshaller(){
        Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
        jaxb2Marshaller.setClassesToBeBound(AptDealDto.class);
        return jaxb2Marshaller;
    }

    @StepScope
    @Bean
    public ItemWriter<AptDealDto> aptDealWriter(){
        return items -> items.forEach(System.out::println);
    }
}
