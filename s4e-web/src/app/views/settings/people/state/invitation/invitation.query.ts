import { Invitation } from './invitation.model';
import { InvitationState, InvitationStore } from './invitation.store';
import { Injectable } from '@angular/core';
import { QueryEntity } from '@datorama/akita';

@Injectable({
  providedIn: 'root'
})
export class InvitationQuery extends QueryEntity<InvitationState, Invitation> {

  constructor(protected store: InvitationStore) {
    super(store);
  }

}
