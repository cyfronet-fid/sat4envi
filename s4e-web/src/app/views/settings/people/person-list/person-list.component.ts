import { InvitationService } from './../state/invitation/invitation.service';
import { InvitationToPersonAdapter } from './../state/invitation/invitation.model';
import { InvitationQuery } from './../state/invitation/invitation.query';
import { InvitationFormModal } from './../invitation-form/invitation-form-modal.model';
import { Modal } from './../../../../modal/state/modal.model';
import { ModalQuery } from './../../../../modal/state/modal.query';
import { INVITATION_FORM_MODAL_ID } from '../invitation-form/invitation-form-modal.model';
import { ModalService } from './../../../../modal/state/modal.service';
import {Component, OnDestroy, OnInit} from '@angular/core';
import {Observable, merge} from 'rxjs';
import {Person} from '../state/person/person.model';
import {Institution} from '../../state/institution/institution.model';
import {InstitutionQuery} from '../../state/institution/institution.query';
import {InstitutionService} from '../../state/institution/institution.service';
import {ActivatedRoute, Router} from '@angular/router';
import {untilDestroyed} from 'ngx-take-until-destroy';
import { PersonQuery } from '../state/person/person.query';
import { PersonService } from '../state/person/person.service';
import { isInvitation } from '../state/invitation/invitation.model';
import { InstitutionsSearchResultsQuery } from '../../state/institutions-search/institutions-search-results.query';
import { reduce, mergeMap, map } from 'rxjs/operators';

@Component({
  selector: 's4e-people',
  templateUrl: './person-list.component.html',
  styleUrls: ['./person-list.component.scss']
})
export class PersonListComponent implements OnInit, OnDestroy {
  users$: Observable<Person[]>;

  private _institution: Institution;

  constructor(
    public personQuery: PersonQuery,
    public invitationQuery: InvitationQuery,

    private _personService: PersonService,
    private _institutionQuery: InstitutionQuery,
    private _invitationService: InvitationService,
    private _institutionService: InstitutionService,
    private _institutionsSearchResultsQuery: InstitutionsSearchResultsQuery,
    private _activatedRoute: ActivatedRoute,
    private _modalService: ModalService,
    private _modalQuery: ModalQuery
  ) {}

  ngOnInit() {
    const invitationsAsPersons$ = this.invitationQuery.selectAll()
      .pipe(map((invitations) => invitations
        .map((invitation) => new InvitationToPersonAdapter()
          .email(invitation.email)
          .status(invitation.status)
          .token(invitation.token)
          .build()
        )
      ));
    this.users$ = merge(
      this._institutionQuery.selectAll(),
      invitationsAsPersons$
    )
      .pipe(
        reduce((persons, person) => persons = [...persons, person], []),
        mergeMap(persons => persons)
      );

    this._loadPersons();
    this._loadActiveInstitution();
  }

  createInvitation() {
    const invitationModal = {
      id: INVITATION_FORM_MODAL_ID,
      size: 'lg',
      institution: this._institution,
      invitation: null
    } as InvitationFormModal;
    this._modalService.show<InvitationFormModal>(invitationModal);

    this._modalQuery.modalClosed$(INVITATION_FORM_MODAL_ID)
      .pipe(untilDestroyed(this))
      .subscribe(() => this._loadPersons());
  }

  resendTo(invitation: any) {
    this._invitationService.resend(invitation, this._institution);
  }

  updateInvitation(invitation: any) {
    const invitationModal = {
      id: INVITATION_FORM_MODAL_ID,
      size: 'lg',
      institution: this._institution,
      invitation,
    } as InvitationFormModal;
    this._modalService.show<InvitationFormModal>(invitationModal);

    this._modalQuery.modalClosed$(INVITATION_FORM_MODAL_ID)
      .pipe(untilDestroyed(this))
      .subscribe(() => this._loadPersons());
  }

  isInvitation(person: Person) {
    return isInvitation(person);
  }


  async delete(person: Person) {
    if (isInvitation(person)) {
      await this._handleInvitationDeletion();
      return;
    }

    await this._handleUserDeletion();
  }

  ngOnDestroy(): void {}

  protected async _handleUserDeletion() {
    const title = 'Usuń członka instytucji';
    const description = 'Czy na pewno chcesz usunąć tę osobę z instytucji? Operacja jest nieodwracalna.';
    const hasBeenConfirmed = await this._modalService.confirm(title, description);
    if (hasBeenConfirmed) {
      // this._personService.deleteMember(userId);
    }
  }

  protected async _handleInvitationDeletion() {
    const title = 'Usuń zaproszenie';
    const description = 'Czy na pewno chcesz usunąć zaproszenie? Operacja jest nieodwracalna.';
    const hasBeenConfirmed = await this._modalService.confirm(title, description);
    if (hasBeenConfirmed) {
      // this._personService.deleteMember(userId);
    }
  }

  protected _loadActiveInstitution() {
    this._institutionsSearchResultsQuery
      .selectActive$(this._activatedRoute)
      .pipe(untilDestroyed(this))
      .subscribe(institution => {
        this._institution = institution;
        this._invitationService.getBy(institution);
      });
  }

  protected _loadPersons() {
    this._institutionService
      .connectInstitutionToQuery$(this._activatedRoute)
      .pipe(untilDestroyed(this))
      .subscribe(instSlug => {
        this._personService.fetchAll(instSlug);
      });
  }
}
