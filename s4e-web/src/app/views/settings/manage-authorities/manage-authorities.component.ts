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
