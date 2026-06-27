package net.vivans.dcim.module.common.domain.repository;

import net.vivans.dcim.module.common.domain.model.CodeGroup;

import java.util.List;

public interface CodeGroupRepository {

    List<CodeGroup> findAll();

    CodeGroup save(CodeGroup codeGroup);
}
