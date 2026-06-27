package net.vivans.dcim.module.common.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.vivans.dcim.shared.persistence.BaseEntity;

@Getter
@Entity
@Table(name = "code_group")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CodeGroup extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "group_key", unique = true, nullable = false)
    private String groupKey;

    @Column(name = "group_name", nullable = false)
    private String groupName;

}
