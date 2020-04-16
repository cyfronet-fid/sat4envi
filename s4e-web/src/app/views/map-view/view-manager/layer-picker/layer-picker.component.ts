import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {ID} from '@datorama/akita';
import {IUILayer} from '../../state/common.model';

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

  @Input() help: string;
  @Input() caption: string;

  @Output() itemSelected = new EventEmitter<ID>();
  @Output() isFavoriteSelected = new EventEmitter<{ID: number, isFavorite: boolean}>();
}
