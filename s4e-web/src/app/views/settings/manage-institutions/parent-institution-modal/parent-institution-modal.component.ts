import { InstitutionService } from './../../state/institution.service';
import { untilDestroyed } from 'ngx-take-until-destroy';
import { InstitutionQuery } from './../../state/institution.query';
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
import { Institution } from '../../state/institution.model';
import { FormControl } from '@ng-stack/forms';
import { combineLatest } from 'rxjs';

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
export class ParentInstitutionModalComponent extends ModalComponent<Institution> implements OnInit, OnDestroy {
  public searchedInstitutions: Institution[] = [];
  public selectedInstitution: Institution | null = null;
  public institutionsSearch: FormControl<string> = new FormControl<string>('');

  constructor(
    private _modalService: ModalService,
    private _institutionQuery: InstitutionQuery,
    private _institutionService: InstitutionService,
    @Inject(MODAL_DEF) modal: ParentInstitutionModal
  ) {
    super(_modalService, PARENT_INSTITUTION_MODAL_ID);

    assertModalType(isParentInstitutionModal, modal);
  }

  ngOnInit() {
    combineLatest(
      this._institutionQuery.selectAll(),
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

  accept() {
    if (!this.selectedInstitution) {
      return;
    }

    this.dismiss(this.selectedInstitution);
  }

  ngOnDestroy() {}
}
