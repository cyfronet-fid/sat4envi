import { ModalWithReturnValue } from './../../modal/state/modal.model';
import { untilDestroyed } from 'ngx-take-until-destroy';
import { ModalQuery } from './../../modal/state/modal.query';
import { ParentInstitutionModal, PARENT_INSTITUTION_MODAL_ID, isParentInstitutionModal } from './manage-institutions/parent-institution-modal/parent-institution-modal.model';
import { ModalService } from './../../modal/state/modal.service';
import { InstitutionQuery } from './state/institution.query';
import { Component, OnInit, OnDestroy } from '@angular/core';
import {SessionService} from '../../state/session/session.service';
import {Observable} from 'rxjs';
import {ProfileQuery} from '../../state/profile/profile.query';
import { InstitutionService } from './state/institution.service';
import { map } from 'rxjs/operators';
import { Institution } from './state/institution.model';

@Component({
  selector: 's4e-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.scss']
})
export class SettingsComponent implements OnInit {
  showInstitutions$: Observable<boolean>;

  constructor(
    private sessionService: SessionService,
    private profileQuery: ProfileQuery,
    private _institutionQuery: InstitutionQuery,
    private _modalService: ModalService,
    private _modalQuery: ModalQuery
  ) { }

  ngOnInit() {
    this.showInstitutions$ = this.profileQuery.selectCanSeeInstitutions();
  }

  openParentInstitutionModal() {
    this._modalService.show<ParentInstitutionModal>({
      id: PARENT_INSTITUTION_MODAL_ID,
      size: 'lg',
      hasReturnValue: true
    });

    this._modalQuery.modalClosed$(PARENT_INSTITUTION_MODAL_ID)
      .pipe(
        map(modal => isParentInstitutionModal(modal) ? modal.returnValue : null)
      )
      .subscribe(institution => console.log(institution));
  }
}
