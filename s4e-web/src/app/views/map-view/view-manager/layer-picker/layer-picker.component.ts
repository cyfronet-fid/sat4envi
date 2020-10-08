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

  @Output() itemSelected = new EventEmitter<ID>();
  @Output() isFavouriteSelected = new EventEmitter<{ID: number, isFavourite: boolean}>();
  @Output() collapse = new EventEmitter<void>();
}
