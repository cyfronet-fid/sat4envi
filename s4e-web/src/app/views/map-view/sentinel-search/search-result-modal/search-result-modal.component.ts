import {Component, OnDestroy, OnInit} from '@angular/core';
import {ModalComponent} from '../../../../modal/utils/modal/modal.component';
import {ModalService} from '../../../../modal/state/modal.service';
import {SENTINEL_SEARCH_RESULT_MODAL_ID} from './search-result-modal.model';
import {Observable} from 'rxjs';
import {SentinelSearchResult} from '../../state/sentinel-search/sentinel-search.model';
import {SentinelSearchQuery} from '../../state/sentinel-search/sentinel-search.query';
import {SentinelSearchService} from '../../state/sentinel-search/sentinel-search.service';

@Component({
  selector: 's4e-search-result-modal',
  templateUrl: './search-result-modal.component.html',
  styleUrls: ['./search-result-modal.component.scss']
})
export class SearchResultModalComponent extends ModalComponent implements OnInit, OnDestroy {
  searchResult$: Observable<SentinelSearchResult>
  isFirst$: Observable<boolean>
  isLast$: Observable<boolean>

  constructor(modalService: ModalService,
              private sentinelSearchService: SentinelSearchService,
              private sentinelSearchQuery: SentinelSearchQuery) {
    super(modalService, SENTINEL_SEARCH_RESULT_MODAL_ID);
  }

  ngOnDestroy(): void {
  }

  ngOnInit(): void {
    this.searchResult$ = this.sentinelSearchQuery.selectActive();
    this.isFirst$ = this.sentinelSearchQuery.selectIsActiveFirst();
    this.isLast$ = this.sentinelSearchQuery.selectIsActiveLast();
  }

  download() {

  }

  previousResult() {
    this.sentinelSearchService.previousActive();
  }

  nextResult() {
    this.sentinelSearchService.nextActive();
  }
}
