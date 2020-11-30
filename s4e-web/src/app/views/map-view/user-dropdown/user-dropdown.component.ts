import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Scene} from '../state/scene/scene.model';

@Component({
  selector: 's4e-user-dropdown',
  templateUrl: './user-dropdown.component.html',
  styleUrls: ['./user-dropdown.component.scss']
})
export class UserDropdownComponent implements OnInit {
  @Output() toggleLoginOptions = new EventEmitter<boolean>();
  @Output() openShareViewModal = new EventEmitter<void>();
  @Output() openSaveViewModal = new EventEmitter<void>();
  @Output() openListViewModal = new EventEmitter<void>();
  @Output() openExpertHelpModal = new EventEmitter<void>();
  @Output() openReportModal = new EventEmitter<void>();
  @Output() openReportTemplatesModal = new EventEmitter<void>();
  @Output() openSceneDetailsModal = new EventEmitter<void>();
  @Output() downloadMapImage = new EventEmitter<void>();
  @Output() openJwtTokenModal = new EventEmitter<void>();
  @Output() toggleHighContrast = new EventEmitter<void>();
  @Output() toggleLargeFont = new EventEmitter<void>();

  @Input() activeScene: Scene|null = null;
  @Input() hasHeightContrast: boolean = false;
  @Input() hasLargeFont: boolean = false;
  @Input() userLoggedIn: boolean = false;
  @Input() showAdvanced: boolean = false;

  constructor() { }

  ngOnInit() {
  }

}
