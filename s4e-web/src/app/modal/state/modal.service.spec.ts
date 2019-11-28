import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ModalService } from './modal.service';
import { ModalStore } from './modal.store';
import {DUMMY_MODAL_ID} from '../components/dummy-modal/dummy-modal.model';
import {ModalQuery} from './modal.query';
import * as akita from '@datorama/akita';
import {CONFIRM_MODAL_ID} from '../components/confirm-modal/confirm-modal.model';
import {ModalWithReturnValue} from './modal.model';
import {ALERT_MODAL_ID} from '../components/alert-modal/alert-modal.model';

describe('ModalService', () => {
  let service: ModalService;
  let store: ModalStore;
  let query: ModalQuery;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ModalService, ModalStore, ModalQuery],
      imports: [ HttpClientTestingModule ]
    });

    service = TestBed.get(ModalService);
    store = TestBed.get(ModalStore);
    query = TestBed.get(ModalQuery);
  });

  it('should be created', () => {
    expect(service).toBeDefined();
  });

  it('should show modal', () => {
    spyOn(akita, 'guid').and.returnValue('abcdefgh');
    service.show({id: DUMMY_MODAL_ID});
    expect(query.getAll()).toEqual([
      {
        id: DUMMY_MODAL_ID,
        size: "md",
        uuid: "abcdefgh",
      }
    ])
  });

  it('should show / hide modal', () => {
    service.show({id: DUMMY_MODAL_ID});
    service.hide(DUMMY_MODAL_ID);
    expect(query.getAll()).toEqual([])
  });

  it('should hide and return value beforehand', (done) => {
    service.show({id: CONFIRM_MODAL_ID, hasReturnValue: true} as ModalWithReturnValue<boolean>);
    const returnValue = query.modalClosed$(CONFIRM_MODAL_ID);
    returnValue.subscribe(modal => {
      expect((modal as ModalWithReturnValue<boolean>).returnValue).toBe(true);
      done();
    });

    service.hide(CONFIRM_MODAL_ID, true);
  });

  it('should alert work', (done) => {
    const content = 'content';
    const title = 'title';
    const spy = spyOn(service, 'show').and.callThrough();
    const r = service.alert(title, content);
    expect(spy).toHaveBeenCalledWith({id: ALERT_MODAL_ID, size: 'sm', content, title});
    service.hide(ALERT_MODAL_ID);
    r.then(() => done())
  });

  it('should confirm work', (done) => {
    const content = 'content';
    const title = 'title';
    const spy = spyOn(service, 'show').and.callThrough();
    const r = service.confirm(title, content);
    expect(spy).toHaveBeenCalledWith({id: CONFIRM_MODAL_ID, size: 'sm', content, title, hasReturnValue: true});
    service.hide(CONFIRM_MODAL_ID, true);
    r.then(val => {
      expect(val).toBe(true);
      done();
    })
  });
});
