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

    public CodeGroup(String groupKey, String groupName) {
        this.groupKey = groupKey;
        this.groupName = groupName;
    }

    public static CodeGroup creatCodeGroup(String groupKey, String groupName){
        if (groupKey == null || groupKey.isBlank()){
            throw new IllegalArgumentException("GroupKey is required");
        }
        if (groupName == null || groupName.isBlank()){
            throw new IllegalArgumentException("GroupName is required");
        }
        return new CodeGroup(groupKey, groupName);
    }

    public void update(String groupKey, String groupName) {
        if (groupKey == null || groupKey.isBlank()) {
            throw new IllegalArgumentException("GroupKey is required");
        }
        if (groupName == null || groupName.isBlank()) {
            throw new IllegalArgumentException("GroupName is required");
        }
        this.groupKey = groupKey;
        this.groupName = groupName;
    }
}
