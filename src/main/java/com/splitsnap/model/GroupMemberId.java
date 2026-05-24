package com.splitsnap.model;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode
public class GroupMemberId implements Serializable {
    private UUID groupId;
    private UUID userId;
}
