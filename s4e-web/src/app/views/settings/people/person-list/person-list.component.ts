import { Modal } from './../../../../modal/state/modal.model';
import { ModalQuery } from './../../../../modal/state/modal.query';
import { INVITATION_FORM_MODAL_ID, InvitationFormModal } from '../invitation-form/invitation-form-modal.model';
import { ModalService } from './../../../../modal/state/modal.service';
import {Component, OnDestroy, OnInit} from '@angular/core';
import {Observable} from 'rxjs';
import {Person} from '../state/person.model';
import {PersonQuery} from '../state/person.query';
import {Institution} from '../../state/institution/institution.model';
import {InstitutionQuery} from '../../state/institution/institution.query';
import {PersonService} from '../state/person.service';
import {InstitutionService} from '../../state/institution/institution.service';
import {ActivatedRoute, Router} from '@angular/router';
import {untilDestroyed} from 'ngx-take-until-destroy';
import { InstitutionsSearchResultsQuery } from '../../state/institutions-search/institutions-search-results.query';

@Component({
  selector: 's4e-people',
  templateUrl: './person-list.component.html',
  styleUrls: ['./person-list.component.scss']
})
export class PersonListComponent implements OnInit, OnDestroy {
  error$: Observable<any>;
  users$: Observable<Person[]>;
  loading$: Observable<boolean>;
  institutions$: Observable<Institution[]>;
  institutionsLoading$: Observable<boolean>;

  private _institution: Institution;

  constructor(
    private _personQuery: PersonQuery,
    private _personService: PersonService,
    private _institutionQuery: InstitutionQuery,
    private _institutionService: InstitutionService,
    private _institutionsSearchResultsQuery: InstitutionsSearchResultsQuery,
    private _activatedRoute: ActivatedRoute,
    private _modalService: ModalService,
    private _modalQuery: ModalQuery
  ) {}

  ngOnInit() {
    this.error$ = this._personQuery.selectError();
    this.loading$ = this._personQuery.selectLoading();
    this.users$ = this._personQuery.selectAll();
    this.institutions$ = this._institutionQuery.selectAll();
    this.institutionsLoading$ = this._institutionQuery.selectLoading();

    this._loadPersons();

    this._institutionsSearchResultsQuery
      .selectActive$(this._activatedRoute)
      .pipe(untilDestroyed(this))
      .subscribe(institution => this._institution = institution);
  }

  openPersonForm(person: Person | null) {
    this._modalService.show<InvitationFormModal>({
      id: INVITATION_FORM_MODAL_ID,
      size: 'lg',
      institution: this._institution
    });

    this._modalQuery.modalClosed$(INVITATION_FORM_MODAL_ID)
      .pipe(untilDestroyed(this))
      .subscribe(() => this._loadPersons());
  }

  delete(person: Person) {
    // TODO: Group member remove
  }

  ngOnDestroy(): void {
  }

  async deleteMember(userId) {
    if (await this._modalService.confirm('Usuń członka instytucji',
      'Czy na pewno chcesz usunąć tę osobę z instytucji? Operacja jest nieodwracalna.')) {
      this._personService.deleteMember(userId);
    }
  }

  protected _loadPersons() {
    this._institutionService
      .connectInstitutionToQuery$(this._activatedRoute)
      .pipe(untilDestroyed(this))
      .subscribe(
        instSlug => this._personService.fetchAll(instSlug)
      );
  }
}
