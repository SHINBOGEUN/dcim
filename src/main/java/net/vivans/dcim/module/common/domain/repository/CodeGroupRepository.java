package net.vivans.dcim.module.common.domain.repository;

import net.vivans.dcim.module.common.domain.model.CodeGroup;

import java.util.List;
import java.util.Optional;

public interface CodeGroupRepository {

    List<CodeGroup> findAll();

    CodeGroup save(CodeGroup codeGroup);

    Optional<CodeGroup> findById(String id);
}
