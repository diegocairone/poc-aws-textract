package com.cairone.textract.aws;

import java.math.BigDecimal;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TextractValue {

    @EqualsAndHashCode.Include
    private final String value;
    
    private final BigDecimal accuracy;
    
}
