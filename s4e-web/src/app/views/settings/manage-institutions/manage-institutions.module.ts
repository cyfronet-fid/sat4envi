import { AddInstitutionComponent } from './add-institution/add-institution.component';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ModalModule } from 'src/app/modal/modal.module';
import { makeModalProvider } from 'src/app/modal/modal.providers';
import { PARENT_INSTITUTION_MODAL_ID } from './parent-institution-modal/parent-institution-modal.model';
import { ParentInstitutionModalComponent } from './parent-institution-modal/parent-institution-modal.component';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { UtilsModule } from 'src/app/utils/utils.module';
import { FormErrorModule } from 'src/app/components/form-error/form-error.module';

@NgModule({
  declarations: [
    ParentInstitutionModalComponent,
    AddInstitutionComponent
  ],
  imports: [
    CommonModule,
    ModalModule,
    ReactiveFormsModule,
    RouterModule,
    UtilsModule,
    FormErrorModule
  ],
  exports: [
    AddInstitutionComponent
  ],
  providers: [
    makeModalProvider(PARENT_INSTITUTION_MODAL_ID, ParentInstitutionModalComponent)
  ],
  entryComponents: [
    ParentInstitutionModalComponent
  ]
})
export class ManageInstitutionsModalModule { }
