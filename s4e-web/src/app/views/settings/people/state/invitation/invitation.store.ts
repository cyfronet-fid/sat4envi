import { Invitation } from './invitation.model';
import { Injectable } from '@angular/core';
import { EntityState, EntityStore, StoreConfig } from '@datorama/akita';

export interface InvitationState extends EntityState<Invitation> {}

@Injectable({ providedIn: 'root' })
@StoreConfig({ name: 'Invitation', idKey: 'email'})
export class InvitationStore extends EntityStore<InvitationState, Invitation, string> {
  constructor() {
    super();
  }
}

