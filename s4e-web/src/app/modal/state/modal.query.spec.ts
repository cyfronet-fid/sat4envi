import {ModalQuery} from './modal.query';
import {ModalStore} from './modal.store';
import {TestBed} from '@angular/core/testing';
import {ModalService} from './modal.service';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {DUMMY_MODAL_ID} from '../components/dummy-modal/dummy-modal.model';
import {createModal} from './modal.model';
import * as akita from '@datorama/akita';

describe('ModalQuery', () => {
  let service: ModalService;
  let store: ModalStore;
  let query: ModalQuery;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ModalService, ModalStore, ModalQuery],
      imports: [HttpClientTestingModule]
    });

    service = TestBed.get(ModalService);
    store = TestBed.get(ModalStore);
    query = TestBed.get(ModalQuery);
  });

  it('should create an instance', () => {
    expect(query).toBeTruthy();
  });

  it('should modalClosed$ work', (done) => {
    const uuid = '1f176345b9';
    spyOn(akita, 'guid').and.returnValue(uuid);
    query.modalClosed$(DUMMY_MODAL_ID).subscribe(modal => {
      expect(modal).toEqual({
        id: DUMMY_MODAL_ID,
        size: 'md',
        uuid: uuid
      });
      done();
    });

    service.show(createModal({id: DUMMY_MODAL_ID}));
    service.hide(DUMMY_MODAL_ID, true);
  });
});
