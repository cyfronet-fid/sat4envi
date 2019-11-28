import {Injectable} from '@angular/core';
import {QueryEntity} from '@datorama/akita';
import {ModalStore} from './modal.store';
import {Modal, ModalState} from './modal.model';
import {Observable} from 'rxjs';
import {filter, map, pairwise, take} from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class ModalQuery extends QueryEntity<ModalState, Modal> {
  constructor(protected store: ModalStore) {
    super(store);
  }

  modalClosed$(modalId: string): Observable<Modal> {
    return this.selectEntity(modalId)
      .pipe(
        pairwise(),
        filter(([prev, curr]) => curr == null && prev != null),
        map(([prev, curr]) => prev),
        take(1)
      );
  }
}
