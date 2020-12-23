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

import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {ID} from '@datorama/akita';
import {IUILayer} from '../../state/common.model';
import {state, style, transition, trigger} from '@angular/animations';

@Component({
  selector: 's4e-items-picker',
  templateUrl: './layer-picker.component.html',
  styleUrls: ['./layer-picker.component.scss']
})
export class ItemsPickerComponent {
  @Input() items: IUILayer[] = [];
  @Input() selectedIds: number[] = [];
  @Input() loading: boolean = true;
  @Input() hasFavourite: boolean = false;
  @Input() collapsed: boolean = false;

  @Input() help: string;
  @Input() caption: string;

  @Output() itemSelected = new EventEmitter<number>();
  @Output() isFavouriteSelected = new EventEmitter<{ID: number, isFavourite: boolean}>();
  @Output() collapse = new EventEmitter<void>();
}
