import { ModalQuery } from './../../../../modal/state/modal.query';
import { PersonFormModal, PERSON_FORM_MODAL_ID } from './../person-form/person-form-modal.model';
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

  constructor(
    private personQuery: PersonQuery,
    private personService: PersonService,
    private institutionQuery: InstitutionQuery,
    private institutionService: InstitutionService,
    private router: Router,
    private route: ActivatedRoute,
    private _modalService: ModalService,
    private _modalQuery: ModalQuery
  ) {}

  ngOnInit() {
    this.error$ = this.personQuery.selectError();
    this.loading$ = this.personQuery.selectLoading();
    this.users$ = this.personQuery.selectAll();
    this.institutions$ = this.institutionQuery.selectAll();
    this.institutionsLoading$ = this.institutionQuery.selectLoading();

    this._loadPersons();
  }

  openPersonForm(person: Person | null) {
    this._modalService.show<PersonFormModal>({
      id: PERSON_FORM_MODAL_ID,
      size: 'lg',
      person: !!person && person || null
    });

    this._modalQuery.modalClosed$(PERSON_FORM_MODAL_ID)
      .pipe(untilDestroyed(this))
      .subscribe(() => this._loadPersons());
  }

  delete(person: Person) {
    // TODO: Group member remove
  }

  ngOnDestroy(): void {
  }

  async deleteMember(userId) {
    if(await this._modalService.confirm('Usuń członka instytucji',
      'Czy na pewno chcesz usunąć tę osobę z instytucji? Operacja jest nieodwracalna.')) {
      this.personService.deleteMember(userId);
    }
  }

  protected _loadPersons() {
    this.institutionService.connectInstitutionToQuery$(this.route)
      .pipe(untilDestroyed(this))
      .subscribe(
        instSlug => this.personService.fetchAll(instSlug)
      );
  }
}
