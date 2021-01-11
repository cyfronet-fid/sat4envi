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

import { ActivatedQueue } from './../../utils/search/activated-queue.utils';
import {FormControl} from '@ng-stack/forms';
import {Component, Input, Output, EventEmitter, ContentChild, TemplateRef, OnInit, OnDestroy, NgZone} from '@angular/core';
import {debounceTime, distinctUntilChanged} from 'rxjs/operators';
import { untilDestroyed } from 'ngx-take-until-destroy';
import {LocationSearchResult} from '../../views/map-view/state/location-search-results/location-search-result.model';

@Component({
  selector: 's4e-search',
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.scss']
})
export class SearchComponent implements OnInit, OnDestroy {
  @Input() placeholder: string = '';
  @Input() store: any;
  @Input() query: any;

  @Input()
  set value(value: string) {
    if (!!value && value !== '' && !!this.searchFormControl.value) {
      this.hasBeenSelected = true;
      this.hasBeenSelectedChange.emit(true);
    }

    this.searchFormControl.setValue(value);
  }
  @Input()
  set hasBeenSelected(hasBeenSelected: boolean) {
    this._hasBeenSelected = hasBeenSelected;
  }
  @Output() hasBeenSelectedChange: EventEmitter<boolean> = new EventEmitter<boolean>();
  @Output() valueChange: EventEmitter<string> = new EventEmitter<string>();
  @Output() selectResult: EventEmitter<any> = new EventEmitter<any>();

  @ContentChild('result') resultTemplate: TemplateRef<any>;

  public searchFormControl: FormControl<string> = new FormControl<string>('');
  public areResultsOpen = false;

  public results: any[];
  public isLoading = false;
  public activatedQueue: ActivatedQueue;

  private _hasBeenSelected = false;

  constructor() {}

  ngOnInit() {
    this._handleSearchValueChange();
    this.query
      .selectAll()
      .pipe(untilDestroyed(this))
      .subscribe(results => {
        this.results = results;

        if (!this.query.getActive() && !!results && results.length > 0) {
          this.store.setActive(this.results[0].id);
        }
      });
    this.query
      .selectLoading()
      .pipe(untilDestroyed(this))
      .subscribe(isLoading => this.isLoading = isLoading);
    this.activatedQueue = new ActivatedQueue(this.query, this.store);
  }

  get hasSearchValue(): boolean {
    return !!this.searchFormControl.value
      && this.searchFormControl.value !== '';
  }

  get hasResults(): boolean {
    return this.results.length > 0;
  }

  get canSelectActiveResult(): boolean {
    return this.areResultsOpen
      && this.hasResults;
  }

  isActive(result: any) {
    const activeId = !!this.query.getActive()
      && (this.query.getActive() as LocationSearchResult).id || null;
    return result.id === activeId;
  }

  activateNextResult() {
    if (!this.areResultsOpen) {
      this.areResultsOpen = true;
      return;
    }

    this.activatedQueue.next();
  }

  activatePreviousResult() {
    if (!this.areResultsOpen) {
      this.areResultsOpen = true;
      return;
    }

    this.activatedQueue.previous();
  }

  activateHovered(result: any) {
    this.store.setActive(result.id);
  }

  selectActive() {
    if (
      !!this.searchFormControl.value
      && !(this.query.getActive() as LocationSearchResult).name
        .toLowerCase()
        .startsWith(
          this.searchFormControl.value
            .toLowerCase()
        )
    ) {
      return;
    }

    this.areResultsOpen = false;
    this.hasBeenSelected = true;
    this.hasBeenSelectedChange.emit(true);
    this.selectResult.emit(this.query.getActive());
    this.searchFormControl.setValue((this.query.getActive() as LocationSearchResult).name);
  }

  resetSearchValue(): void {
    this.searchFormControl.setValue('');
    this.results = [];

    this.valueChange.emit('');
    this.areResultsOpen = false;
  }

  ngOnDestroy() {}

  protected _handleSearchValueChange = () => this.searchFormControl.valueChanges
    .pipe(
      debounceTime(300),
      distinctUntilChanged(),
      untilDestroyed(this),
    ).subscribe((text: string) => {
      if (!this._hasBeenSelected) {
        this.areResultsOpen = true;
        this.valueChange.emit(text);
        this.store.setActive(null);
      }

      this.hasBeenSelected = false;
      this.hasBeenSelectedChange.emit(false);
    })
}
