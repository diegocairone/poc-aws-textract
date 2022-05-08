package com.cairone.textract.ui.response;

import java.math.BigDecimal;
import java.util.Map;

import com.cairone.textract.aws.TextractValue;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class KeyValueResponse {

    private final BigDecimal worstAccuray;
    private final Map<String, TextractValue> data;
    
    public static KeyValueResponse fromResultMap(Map<String, TextractValue> textractValueMap) {
        TextractValue worstValue = textractValueMap.values()
                .stream()
                .min((c1,c2) -> c1.getAccuracy().compareTo(c2.getAccuracy()))
                .get();
        KeyValueResponse response = new KeyValueResponse(
                worstValue.getAccuracy(), textractValueMap); 
        return response;
    }
}
