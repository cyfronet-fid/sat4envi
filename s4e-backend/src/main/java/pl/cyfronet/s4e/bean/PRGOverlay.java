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

import javax.persistence.*;

@Entity
@Table(name = "prg_overlay")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PRGOverlay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Must be set to match with the featureTypes in the PRG zip. It will also be the layer name
     */
    private String featureType;

    @ManyToOne
    private SldStyle sldStyle;

    private boolean created;

    @OneToOne(fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private WMSOverlay wmsOverlay;
}
