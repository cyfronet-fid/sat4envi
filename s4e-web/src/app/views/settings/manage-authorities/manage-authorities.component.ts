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

import {Component, OnDestroy, OnInit} from '@angular/core';
import {PersonQuery} from '../people/state/person/person.query';
import {PersonService} from '../people/state/person/person.service';
import {SessionQuery} from '../../../state/session/session.query';
import {untilDestroyed} from 'ngx-take-until-destroy';
import {Person} from '../people/state/person/person.model';

@Component({
  selector: 's4e-people',
  templateUrl: './manage-authorities.component.html',
  styleUrls: ['./manage-authorities.component.scss']
})
export class ManageAuthoritiesComponent implements OnInit, OnDestroy {
  public areLoading$ = this._personQuery.selectLoading();
  public errors$ = this._personQuery.selectError();
  public users$ = this._personQuery.selectAll();

  public canGrantDeleteAuthority: boolean;
  public currentUserEmail: string | null;

  constructor(
    private _personQuery: PersonQuery,
    private _personService: PersonService,

    private _sessionQuery: SessionQuery
  ) {}

  ngOnInit() {
    this._personService.fetchAllAdmins$()
      .pipe(untilDestroyed(this))
      .subscribe();
    this.canGrantDeleteAuthority = this._sessionQuery.canGrantInstitutionDeleteAuthority();
    this._sessionQuery.select('email').pipe(untilDestroyed(this)).subscribe(email => this.currentUserEmail = email);
  }

  toggleDeleteAuthority(person: Person) {
    this._personService.toggleDeletePrivilegeFor$(person).subscribe();
  }

  ngOnDestroy() {}
}
