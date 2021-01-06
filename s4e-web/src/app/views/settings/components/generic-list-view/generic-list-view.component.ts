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

import {Component, ContentChild, EventEmitter, Input, OnDestroy, OnInit, Output, TemplateRef} from '@angular/core';
import {FormArray, FormControl} from '@ng-stack/forms';
import {untilDestroyed} from 'ngx-take-until-destroy';
import {ID} from '@datorama/akita';
import {Subscription} from 'rxjs';
import {debounceTime} from 'rxjs/operators';

@Component({
  selector: 's4e-generic-list-view',
  templateUrl: './generic-list-view.component.html',
  styleUrls: ['./generic-list-view.component.scss']
})
export class GenericListViewComponent implements OnInit, OnDestroy {
  @ContentChild(TemplateRef) tableRow: TemplateRef<any>;
  @Input() error: any|null = null;
  @Input() loading: boolean = true;
  @Input() itemIdFunction: (any) => ID = (item: any) => item.id;
  @Input() itemSearchFunction: (item, query) => boolean = (item: any, query: string) => Object.values(item)
    .filter(fieldVal => typeof fieldVal == 'string')
    .map((fieldVal: string) => fieldVal.toLocaleLowerCase())
    .join(';').indexOf(query.toLocaleLowerCase()) >= 0;

  protected _originalItems: any[] = [];
  items: any[] = [];
  @Input('items') set _items(val: any[]) {
    this._originalItems = val;
    this.items = this._originalItems;

    if(this.searchable && this.offlineSearch && this.queryFc.value.length > 0) {
      this.items = this._originalItems.filter(item => this.itemSearchFunction(item, this.queryFc.value));
    }

    if(this.selectable) {
      if (this._itemSelectSub) {
        this._itemSelectSub.unsubscribe();
      }

      this.itemSelectFc = new FormArray(val.map(item => new FormControl<boolean>(false)));
      this._itemSelectSub = this.itemSelectFc.valueChanges.subscribe((selectedItems: boolean[]) => {
        const selected = selectedItems.map((val, i) => val && this.itemIdFunction(this.items[i])).filter(v => !!v);
        this.onselect.emit(selected);
        this.selectAllFc.setValue(selected.length > 0, {emitEvent: false});
      });
    }
  }

  @Input() selectable: boolean = false;
  @Input() offlineSearch: boolean = true;
  @Input() searchable: boolean = false;
  @Output() onquery = new EventEmitter<string>();
  @Output() onselect = new EventEmitter<ID[]>();

  queryFc: FormControl<string> = new FormControl<string>('');
  selectAllFc: FormControl<boolean> = new FormControl<boolean>(false);
  itemSelectFc: FormArray<boolean>;

  private _itemSelectSub: Subscription = null;

  constructor() { }

  ngOnInit() {
    this.selectAllFc.valueChanges.pipe(untilDestroyed(this)).subscribe(value => {
      this.itemSelectFc.setValue(this.itemSelectFc.controls.map(() => value));
    });

    this.queryFc.valueChanges.pipe(debounceTime(250), untilDestroyed(this)).subscribe(value => {
      if(this.offlineSearch) {
        this.items = value.length > 0 ? this._originalItems.filter(item => this.itemSearchFunction(item, value)) : this._originalItems;
      } else {
        this.onquery.emit(value);
      }
    });
  }

  ngOnDestroy(): void {}
}
