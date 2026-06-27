package net.vivans.dcim.module.common.infrastructure.persistence;

import net.vivans.dcim.module.common.domain.model.CodeGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CodeGroupSpringDataRepository extends JpaRepository<CodeGroup, Integer> {
}
