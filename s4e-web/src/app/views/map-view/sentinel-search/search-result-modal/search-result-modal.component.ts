import {Component, OnDestroy, OnInit} from '@angular/core';
import {ModalComponent} from '../../../../modal/utils/modal/modal.component';
import {ModalService} from '../../../../modal/state/modal.service';
import {SENTINEL_SEARCH_RESULT_MODAL_ID} from './search-result-modal.model';
import {Observable} from 'rxjs';
import {SentinelSearchResult} from '../../state/sentinel-search/sentinel-search.model';
import {SentinelSearchQuery} from '../../state/sentinel-search/sentinel-search.query';
import {SentinelSearchService} from '../../state/sentinel-search/sentinel-search.service';
import { SentinelSearchStore } from '../../state/sentinel-search/sentinel-search.store';
import { ActivatedQueue } from 'src/app/utils/search/activated-queue.utils';

@Component({
  selector: 's4e-search-result-modal',
  templateUrl: './search-result-modal.component.html',
  styleUrls: ['./search-result-modal.component.scss']
})
export class SearchResultModalComponent extends ModalComponent implements OnInit, OnDestroy {
  searchResult$: Observable<SentinelSearchResult>;
  isFirst$: Observable<boolean>;
  isLast$: Observable<boolean>;

  constructor(
    modalService: ModalService,
    private _sentinelSearchService: SentinelSearchService,
    private _sentinelSearchQuery: SentinelSearchQuery
  ) {
    super(modalService, SENTINEL_SEARCH_RESULT_MODAL_ID);
  }

  ngOnInit(): void {
    this.searchResult$ = this._sentinelSearchQuery.selectActive();
    this.isFirst$ = this._sentinelSearchQuery.selectIsActiveFirst();
    this.isLast$ = this._sentinelSearchQuery.selectIsActiveLast();
  }

  download() {

  }

  previousResult() {
    this._sentinelSearchService.previousActive();
  }

  nextResult() {
    this._sentinelSearchService.nextActive();
  }

  ngOnDestroy(): void {}
}
