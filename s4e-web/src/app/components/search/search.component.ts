import { ActivatedQueue } from './../../utils/search/activated-queue.utils';
import { QueryEntity, Store, EntityStore } from '@datorama/akita';
import {FormControl} from '@ng-stack/forms';
import { Component, Input, Output, EventEmitter, ContentChild, TemplateRef, OnInit, OnDestroy } from '@angular/core';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { untilDestroyed } from 'ngx-take-until-destroy';

@Component({
  selector: 's4e-search',
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.scss']
})
export class SearchComponent implements OnInit, OnDestroy {
  @Input() placeholder: string = '';

  @Input() query: QueryEntity<any, any>;
  @Input() store: EntityStore<any, any>;

  @Input()
  set value(value: string) {
    if (!!value && value !== '' && !!this.searchFormControl.value) {
      this.hasBeenSelected = true;
    }

    this.searchFormControl.setValue(value);
  }
  @Output() valueChange: EventEmitter<string> = new EventEmitter<string>();
  @Output() selectResult: EventEmitter<any> = new EventEmitter<any>();

  @ContentChild('result') resultTemplate: TemplateRef<any>;

  public searchFormControl: FormControl<string> = new FormControl<string>('');
  public hasBeenSelected = false;
  public areResultsOpen = false;

  public results: any[];
  public isLoading = false;
  public activatedQueue: ActivatedQueue;

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
    return this.hasSearchValue
      && this.areResultsOpen
      && this.hasResults;
  }

  isActive(result: any) {
    const activeId = !!this.query.getActive() && this.query.getActive().id || null;
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

  select(result: any) {
    this.hasBeenSelected = true;
    this.selectResult.emit(result);
    this.areResultsOpen = false;
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
      if (!this.hasBeenSelected) {
        this.areResultsOpen = true;
        this.valueChange.emit(text);
      }

      this.hasBeenSelected = false;
    })
}
