/*
 * Copyright 2020 ACC Cyfronet AGH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
    query.modalClosed$(DUMMY_MODAL_ID)
      .subscribe(modal => {
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
