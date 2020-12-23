/*
 * Copyright 2020 ACC Cyfronet AGH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package pl.cyfronet.s4e.bean;

import lombok.*;
import org.hibernate.annotations.NaturalId;
import pl.cyfronet.s4e.bean.audit.CreationAndModificationAudited;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.HashSet;
import java.util.Set;

/**
 * If you delete Institution, you will also delete all Group entries
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@Table(name = "institution", uniqueConstraints = @UniqueConstraint(columnNames = {"name", "slug"}))
public class Institution extends CreationAndModificationAudited {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotEmpty
    @EqualsAndHashCode.Include
    private String name;

    private String address;

    private String postalCode;

    private String city;

    private String phone;

    private String secondaryPhone;

    @NotEmpty
    @NaturalId(mutable = true)
    @EqualsAndHashCode.Include
    private String slug;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @ToString.Exclude
    private Institution parent;

    @OneToMany(mappedBy = "parent")
    @ToString.Exclude
    private Set<Institution> children;

    @OneToMany(mappedBy = "institution", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private Set<UserRole> membersRoles = new HashSet<>();

    public void addMemberRole(UserRole role) {
        membersRoles.add(role);
    }

    public void removeMemberRole(UserRole role) {
        membersRoles.remove(role);
    }

    @OneToMany(mappedBy = "institution", cascade = CascadeType.ALL)
    @ToString.Exclude
    private Set<LicenseGrant> licenseGrants;

    private boolean eumetsatLicense;

    private boolean zk;

    private boolean pak;
}
