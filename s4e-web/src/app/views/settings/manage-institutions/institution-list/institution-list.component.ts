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

import {SessionQuery} from 'src/app/state/session/session.query';
import {Component, OnDestroy, OnInit} from '@angular/core';
import {Institution} from '../../state/institution/institution.model';
import {InstitutionQuery} from '../../state/institution/institution.query';
import {InstitutionService} from '../../state/institution/institution.service';
import {ModalService} from '../../../../modal/state/modal.service';
import {UntilDestroy, untilDestroyed} from '@ngneat/until-destroy';

@UntilDestroy()
@Component({
  selector: 's4e-institution-list',
  templateUrl: './institution-list.component.html',
  styleUrls: ['./institution-list.component.scss']
})
export class InstitutionListComponent implements OnInit, OnDestroy {
  isAdmin = false;
  hasDeleteAuthorities = false;

  isLoading$ = this._institutionQuery.selectLoading();
  institutions$ = this._institutionQuery.selectAll();
  error$ = this._institutionQuery.selectError();

  constructor(
    private _institutionQuery: InstitutionQuery,
    private _institutionService: InstitutionService,
    private _sessionQuery: SessionQuery,
    private _modalService: ModalService
  ) {}

  ngOnInit() {
    this.isAdmin = this._sessionQuery.isAdmin();
    this.hasDeleteAuthorities = this._sessionQuery.canDeleteInstitution();
  }

  isManagerOf(institution: Institution) {
    return this._institutionQuery.isManagerOf(institution);
  }

  async deleteInstitution(slug: string) {
    if (
      await this._modalService.confirm(
        'Usuń instytucję',
        'Czy na pewno chcesz usunąć tą instytucję? Operacja jest nieodwracalna.'
      )
    ) {
      this._institutionService.delete(slug).pipe(untilDestroyed(this)).subscribe();
    }
  }

  ngOnDestroy() {}
}
