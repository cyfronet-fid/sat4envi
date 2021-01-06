/*
 * Copyright 2021 ACC Cyfronet AGH
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

import { untilDestroyed } from 'ngx-take-until-destroy';
import { ModalComponent } from './../../../../modal/utils/modal/modal.component';
import { Component, Inject, OnDestroy, OnInit } from '@angular/core';
import { ModalService } from 'src/app/modal/state/modal.service';
import { MODAL_DEF } from 'src/app/modal/modal.providers';
import { assertModalType } from 'src/app/modal/utils/modal/misc';
import {
  isParentInstitutionModal,
  ParentInstitutionModal,
  PARENT_INSTITUTION_MODAL_ID
} from './parent-institution-modal.model';
import { FormControl } from '@ng-stack/forms';
import { combineLatest } from 'rxjs';
import { Institution } from '../../state/institution/institution.model';
import { InstitutionQuery } from '../../state/institution/institution.query';
import { InstitutionService } from '../../state/institution/institution.service';

function isSearchedInstitution(institution: Institution, searchValue: string) {
  const normalizedInstitutionName = institution.name.trim().toLocaleLowerCase();
  const normalizedSearchValue = searchValue.trim().toLocaleLowerCase();

  return normalizedInstitutionName.indexOf(normalizedSearchValue) > -1
    || !normalizedSearchValue
    || normalizedSearchValue === '';
}

@Component({
  selector: 's4e-parent-institution-modal',
  templateUrl: './parent-institution-modal.component.html',
  styleUrls: ['./parent-institution-modal.component.scss']
})
export class ParentInstitutionModalComponent extends ModalComponent<Partial<Institution>> implements OnInit, OnDestroy {
  public searchedInstitutions: Institution[] = [];
  public selectedInstitution: Partial<Institution> | null = null;
  public institutionsSearch: FormControl<string> = new FormControl<string>('');

  constructor(
    private _modalService: ModalService,
    private _institutionQuery: InstitutionQuery,
    private _institutionService: InstitutionService,
    @Inject(MODAL_DEF) modal: ParentInstitutionModal
  ) {
    super(_modalService, PARENT_INSTITUTION_MODAL_ID);

    assertModalType(isParentInstitutionModal, modal);

    this.selectedInstitution = modal.selectedInstitution;
  }

  ngOnInit() {
    combineLatest(
      this._institutionQuery.selectAdministrationInstitutions$(),
      this.institutionsSearch.valueChanges
    )
    .pipe(untilDestroyed(this))
    .subscribe(([institutions, searchValue]) => {
      this.searchedInstitutions = !!searchValue && searchValue.trim() !== ''
        && institutions.filter(institution => isSearchedInstitution(institution, searchValue))
        || institutions;
    });

    this._institutionService.get();
    this.institutionsSearch.setValue('');
  }

  isSelected(institution: Institution) {
    return !!this.selectedInstitution
      && this.selectedInstitution.slug === institution.slug;
  }

  accept() {
    if (!this.selectedInstitution) {
      return;
    }

    this.dismiss(this.selectedInstitution);
  }

  ngOnDestroy() {}
}
