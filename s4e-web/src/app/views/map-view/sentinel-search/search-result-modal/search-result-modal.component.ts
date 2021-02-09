/*
 * Copyright 2021 ACC Cyfronet AGH
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

import {Component, Inject, OnDestroy, OnInit} from '@angular/core';
import {ModalComponent} from '../../../../modal/utils/modal/modal.component';
import {ModalService} from '../../../../modal/state/modal.service';
import {
  DetailsModal,
  SENTINEL_SEARCH_RESULT_MODAL_ID
} from './search-result-modal.model';
import {Observable, of, ReplaySubject} from 'rxjs';
import {SentinelSearchResult} from '../../state/sentinel-search/sentinel-search.model';
import {SentinelSearchQuery} from '../../state/sentinel-search/sentinel-search.query';
import {SentinelSearchService} from '../../state/sentinel-search/sentinel-search.service';
import {SessionQuery} from '../../../../state/session/session.query';
import {MODAL_DEF} from '../../../../modal/modal.providers';

@Component({
  selector: 's4e-search-result-modal',
  templateUrl: './search-result-modal.component.html',
  styleUrls: ['./search-result-modal.component.scss']
})
export class SearchResultModalComponent
  extends ModalComponent
  implements OnInit, OnDestroy {
  searchResult$: Observable<SentinelSearchResult>;
  isFirst$: Observable<boolean>;
  isLast$: Observable<boolean>;
  xclicked = () => this.dismiss();

  constructor(
    @Inject(MODAL_DEF) public modal: DetailsModal,
    modalService: ModalService,
    private _sentinelSearchService: SentinelSearchService,
    private _sentinelSearchQuery: SentinelSearchQuery,
    private _sessionQuery: SessionQuery
  ) {
    super(modalService, SENTINEL_SEARCH_RESULT_MODAL_ID);
  }

  ngOnInit(): void {
    if (this.modal.mode === 'sentinel') {
      this.searchResult$ = this._sentinelSearchQuery.selectActive();
      this.isFirst$ = this._sentinelSearchQuery.selectIsActiveFirst();
      this.isLast$ = this._sentinelSearchQuery.selectIsActiveLast();
    } else if (this.modal.mode === 'scene') {
      this.searchResult$ = of(this.modal.entity);
      this.isFirst$ = this.isLast$ = of(false);
    }
  }

  previousResult() {
    this._sentinelSearchService.previousActive();
  }

  nextResult() {
    this._sentinelSearchService.nextActive();
  }

  ngOnDestroy(): void {}

  interceptDownload($event: MouseEvent, dismiss: boolean = true) {
    if (!this._sessionQuery.isLoggedIn()) {
      this.dismiss();
      $event.preventDefault();
      this._sentinelSearchService.redirectToLoginPage();
    } else if (dismiss) {
      this.dismiss();
    }
  }

  dismiss(returnValue?: void) {
    this._sentinelSearchService.openModalForResult(null);
    super.dismiss(returnValue);
  }
}
