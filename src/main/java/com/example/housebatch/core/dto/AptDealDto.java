package com.example.housebatch.core.dto;

import io.micrometer.core.instrument.util.StringUtils;
import lombok.Getter;
import lombok.ToString;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * 아파트 실거래가 API의 각각의 거래 정보를 담는 객체.
 */
@ToString
@Getter
@XmlRootElement(name = "item")
public class AptDealDto {

    @XmlElement(name = "거래금액")
    private String dealAmount;

    public Long getDealAmount(){
        return Long.parseLong(dealAmount.replaceAll(",", "").trim());
    };

    @XmlElement(name = "거래유형")
    private String reqGbn;

    @XmlElement(name = "건축년도")
    private Integer builtYear;

    @XmlElement(name = "년")
    private Integer year;

    @XmlElement(name = "법정동")
    private String dong;

    @XmlElement(name = "아파트")
    private String aptName;

    @XmlElement(name = "월")
    private Integer month;
    
    @XmlElement(name = "일")
    private Integer day;

    @XmlElement(name = "전용면적")
    private Double exclusiveArea;

    @XmlElement(name = "중개사소재지")
    private String dealerLawdnm;

    @XmlElement(name = "지번")
    private String jibun;

    // API 조회 시 jibun이 없을 경우를 대비.
    public String getJibun(){
        return Optional.ofNullable(jibun).orElse("");
    }

    @XmlElement(name = "지역코드")
    private String regionalCode;

    @XmlElement(name = "층")
    private Integer floor;

    @XmlElement(name = "해제사유발생일")
    private String dealCanceledDate; // 22.09.01

    public LocalDate getDealCanceledDate(){
        if (StringUtils.isBlank(dealCanceledDate)){
            return null;
        }
        return LocalDate.parse(dealCanceledDate.trim(), DateTimeFormatter.ofPattern("yy.MM.dd"));
    }

    @XmlElement(name = "해제여부")
    private String dealCanceled; // O

    public boolean isDealCanceled(){
        return "O".equals(dealCanceled.trim());
    }

    public LocalDate getDealDate(){
        return LocalDate.of(year, month, day);
    }
}
