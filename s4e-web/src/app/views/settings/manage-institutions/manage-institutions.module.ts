import {InstitutionFormComponent} from './institution-form/institution-form.component';
import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ModalModule} from 'src/app/modal/modal.module';
import {makeModalProvider} from 'src/app/modal/modal.providers';
import {PARENT_INSTITUTION_MODAL_ID} from './parent-institution-modal/parent-institution-modal.model';
import {ParentInstitutionModalComponent} from './parent-institution-modal/parent-institution-modal.component';
import {ReactiveFormsModule} from '@angular/forms';
import {RouterModule} from '@angular/router';
import {UtilsModule} from 'src/app/utils/utils.module';
import {FormErrorModule} from 'src/app/components/form-error/form-error.module';
import {S4EFormsModule} from 'src/app/form/form.module';
import {InstitutionListComponent} from './institution-list/institution-list.component';
import {GenericListViewModule} from '../components/generic-list-view/generic-list-view.module';
import {OverlayListModule} from '../../../components/overlay-list/overlay-list.module';

@NgModule({
  declarations: [
    ParentInstitutionModalComponent,
    InstitutionFormComponent,
    InstitutionListComponent
  ],
  imports: [
    CommonModule,
    ModalModule,
    ReactiveFormsModule,
    RouterModule,
    UtilsModule,
    FormErrorModule,
    S4EFormsModule,
    GenericListViewModule,
    OverlayListModule
  ],
  exports: [
    InstitutionFormComponent,
    InstitutionListComponent
  ],
  providers: [
    makeModalProvider(PARENT_INSTITUTION_MODAL_ID, ParentInstitutionModalComponent)
  ],
  entryComponents: [
    ParentInstitutionModalComponent
  ]
})
export class ManageInstitutionsModule {
}
