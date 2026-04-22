package com.wecom.scrm.thirdparty.api.changdu.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ChangduPackageResponse extends ChangduResponse<List<ChangduPackageInfo>> {

    @JsonProperty("package_info_open_list")
    private List<ChangduPackageInfo> packages;

    @Override
    public List<ChangduPackageInfo> getResult() {
        return packages;
    }
}
