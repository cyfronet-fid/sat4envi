import { Institution } from './../../state/institution/institution.model';
import { InvitationFormModal } from './../invitation-form/invitation-form-modal.model';
import { ModalQuery } from './../../../../modal/state/modal.query';
import { INVITATION_FORM_MODAL_ID } from '../invitation-form/invitation-form-modal.model';
import { ModalService } from './../../../../modal/state/modal.service';
import {Component, OnDestroy, OnInit} from '@angular/core';
import { Observable, merge, combineLatest, concat } from 'rxjs';
import {InstitutionQuery} from '../../state/institution/institution.query';
import {InstitutionService} from '../../state/institution/institution.service';
import {ActivatedRoute, Router} from '@angular/router';
import {untilDestroyed} from 'ngx-take-until-destroy';
import { InstitutionsSearchResultsQuery } from '../../state/institutions-search/institutions-search-results.query';
import { reduce, mergeMap, map, tap, switchMap, shareReplay, toArray, filter } from 'rxjs/operators';
import { isInvitation, invitationToPerson } from '../state/invitation/invitation.model';
import { Person } from '../state/person/person.model';
import { PersonQuery } from '../state/person/person.query';
import { InvitationQuery } from '../state/invitation/invitation.query';
import { PersonService } from '../state/person/person.service';
import { InvitationService } from '../state/invitation/invitation.service';

@Component({
  selector: 's4e-people',
  templateUrl: './person-list.component.html',
  styleUrls: ['./person-list.component.scss']
})
export class PersonListComponent implements OnInit, OnDestroy {
  public institution: Institution;

  public users$: Observable<Person[]>;
  public areLoading$ = merge(
    this._personQuery.selectLoading(),
    this._invitationQuery.selectLoading()
  )
    .pipe(reduce((acc, isLoading) => acc = acc || isLoading, false));
  public errors$ = merge(
    this._personQuery.selectError(),
    this._invitationQuery.selectError()
  );

  private _institution$ = this._institutionsSearchResultsQuery
    .selectActive$(this._activatedRoute)
    .pipe(
      untilDestroyed(this),
      tap((institution) => this._invitationService.getBy(institution)),
      shareReplay(1)
    );
  private _invitationsAsPersons$ = this._invitationQuery.selectAll()
    .pipe(map((invitations) => invitations
      .map((invitation) => invitationToPerson(invitation))
    ));
  private _loadPersons$ = this._institutionService
    .connectInstitutionToQuery$(this._activatedRoute)
    .pipe(
      untilDestroyed(this),
      tap((institutionSlug) => this._personService.fetchAll(institutionSlug))
    );

  constructor(
    private _personQuery: PersonQuery,
    private _invitationQuery: InvitationQuery,
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
    this.users$ = combineLatest(
      this._personQuery.selectAll(),
      this._invitationsAsPersons$
    )
      .pipe(map(([persons, invitations]) => [...persons, ...invitations]));


    this._institution$.subscribe((institution) => this.institution = institution);
    this._loadPersons$.subscribe();
  }

  sendInvitation(invitation: any | null = null) {
    const invitationModal = {
      id: INVITATION_FORM_MODAL_ID,
      size: 'lg',
      institution: this.institution,
      invitation: invitation
    } as InvitationFormModal;
    this._modalService.show<InvitationFormModal>(invitationModal);

    this._modalQuery.modalClosed$(INVITATION_FORM_MODAL_ID)
      .pipe(
        untilDestroyed(this),
        switchMap(() => this._loadPersons$)
      )
      .subscribe();
  }

  resendTo(invitation: any) {
    this._invitationService.resend(invitation, this.institution);
  }

  isInvitation(person: Person) {
    return isInvitation(person);
  }

  async delete(person: Person) {
    if (isInvitation(person)) {
      await this._handleInvitationDeletion(person);
      return;
    }

    await this._handleUserDeletion(person);
  }

  ngOnDestroy(): void {}

  protected async _handleUserDeletion(person: Person) {
    const title = 'Usuń członka instytucji';
    const description = 'Czy na pewno chcesz usunąć tę osobę z instytucji? Operacja jest nieodwracalna.';
    const hasBeenConfirmed = await this._modalService.confirm(title, description);
    if (hasBeenConfirmed) {
      // this._personService.deleteMember(userId);
    }
  }

  protected async _handleInvitationDeletion(invitation: any) {
    const title = 'Usuń zaproszenie';
    const description = 'Czy na pewno chcesz usunąć zaproszenie? Operacja jest nieodwracalna.';
    const hasBeenConfirmed = await this._modalService.confirm(title, description);
    if (hasBeenConfirmed) {
      this._invitationService.delete(invitation, this.institution);
    }
  }
}
