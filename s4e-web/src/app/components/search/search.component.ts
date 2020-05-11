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
  @Input() searchResults: any[] = [];
  @Input() isSearchLoading = true;
  @Input() isSearchOpen = false;

  @Input()
  set value(value: string) {
    if (!!value && value !== '' && !!this.searchFormControl.value) {
      this.hasBeenSelected = true;
    }

    this.searchFormControl.setValue(value);
  }
  @Input() placeholder: string = '';

  @Output() refreshResults: EventEmitter<string> = new EventEmitter<string>();
  @Output() selectResult: EventEmitter<any> = new EventEmitter<any>();
  @Output() resetSearch: EventEmitter<void> = new EventEmitter<void>();
  @Output() selectFirstResult: EventEmitter<void> = new EventEmitter<void>();

  @ContentChild('result') resultTemplate: TemplateRef<any>;

  public searchFormControl: FormControl<string> = new FormControl<string>('');
  public hasBeenSelected = false;

  public results: any[];

  ngOnInit() {
    this._handleSearchValueChange();
  }

  get hasSearchValue(): boolean {
    return !!this.searchFormControl.value
      && this.searchFormControl.value !== '';
  }

  get hasResults(): boolean {
    return this.searchResults.length > 0;
  }

  get canSearchFirstResult(): boolean {
    return this.hasSearchValue
      && this.isSearchOpen
      && this.hasResults;
  }

  select(result: any) {
    this.hasBeenSelected = true;
    this.selectResult.emit(result);
  }

  resetSearchValue(): void {
    this.searchFormControl.setValue('');
    this.searchResults = [];
    this.resetSearch.emit();
  }

  ngOnDestroy() {}

  protected _handleSearchValueChange = () => this.searchFormControl.valueChanges
    .pipe(
      debounceTime(300),
      distinctUntilChanged(),
      untilDestroyed(this),
    ).subscribe((text: string) => {
      if (!this.hasBeenSelected) {
        this.refreshResults.emit(text);
      }

      this.hasBeenSelected = false;
    })
}
