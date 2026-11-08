package com.zaid.Delta_Forge.features.vcs_core.converter.generic_to_raw;

import com.zaid.Delta_Forge.features.vcs_core.branch.BranchName;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class BranchNameToStringConverter implements Converter<BranchName,String> {
    @Override public String convert(BranchName name) { return name.branch_name(); }
}