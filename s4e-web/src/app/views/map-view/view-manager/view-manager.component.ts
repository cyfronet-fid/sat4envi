import {Component, EventEmitter, Input, OnDestroy, OnInit, Output} from '@angular/core';
import {IUILayer} from '../state/common.model';
import {FormControl} from '@ng-stack/forms';
import {SearchResult} from '../state/search-results/search-result.model';
import {SearchResultsQuery} from '../state/search-results/search-results.query';
import {environment} from '../../../../environments/environment';
import {debounceTime, distinctUntilChanged} from 'rxjs/operators';
import {untilDestroyed} from 'ngx-take-until-destroy';

@Component({
  selector: 's4e-view-manager',
  templateUrl: './view-manager.component.html',
  styleUrls: ['./view-manager.component.scss'],
})
export class ViewManagerComponent implements OnInit, OnDestroy{
  @Input() loading = true;
  @Input() userLoggedIn: boolean = false;
  @Input() products: IUILayer[] = [];
  @Input() productsTypes: IUILayer[] = [];
  @Input() productTypeLoading: boolean = true;
  @Output() selectProductType = new EventEmitter<number>();
  @Output() changedSearchFocus = new EventEmitter<boolean>();

  @Input() overlays: IUILayer[] = [];
  @Input() overlaysLoading: boolean = true;
  @Output() selectOverlay = new EventEmitter<string>();
  @Output() logout = new EventEmitter<void>();

  @Output() searchForPlaces = new EventEmitter<string>();
  @Input() searchResults: SearchResult[];
  @Input() searchResultsLoading: boolean;
  @Input() searchResultsOpen: boolean;
  @Output() searchResultClicked = new EventEmitter<SearchResult>();
  searchFc: FormControl<string> = new FormControl<string>('');

  constructor(private searchResultQuery: SearchResultsQuery) {
  }

  ngOnInit(): void {
    if (environment.hmr) {
      this.searchFc.setValue(this.searchResultQuery.getValue().queryString);
    }

    this.searchFc.valueChanges.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      untilDestroyed(this),
    ).subscribe((text: string) => this.searchForPlaces.emit(text));
  }

  resultsClicked(result: SearchResult) {
    this.searchResultClicked.emit(result);
  }

  resetSelectedLocation() {
    this.searchResultClicked.emit(null);
    this.searchForPlaces.emit('');
    this.searchFc.setValue('');
  }

  ngOnDestroy(): void {
  }
}
