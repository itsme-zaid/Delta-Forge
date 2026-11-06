package com.zaid.Delta_Forge.features.vcs_core.converter.raw_to_generic;

import com.zaid.Delta_Forge.features.vcs_core.branch.BranchName;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class UUIDtoBranchNameConverter implements Converter<String, BranchName> {
    @Override public BranchName convert(String source) { return new BranchName(source); }
}