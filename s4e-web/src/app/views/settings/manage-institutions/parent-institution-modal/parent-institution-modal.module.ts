import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ModalModule } from 'src/app/modal/modal.module';
import { makeModalProvider } from 'src/app/modal/modal.providers';
import { PARENT_INSTITUTION_MODAL_ID } from './parent-institution-modal.model';
import { ParentInstitutionModalComponent } from './parent-institution-modal.component';
import { ReactiveFormsModule } from '@angular/forms';

@NgModule({
  declarations: [
    ParentInstitutionModalComponent
  ],
  imports: [
    CommonModule,
    ModalModule,
    ReactiveFormsModule
  ],
  providers: [
    makeModalProvider(PARENT_INSTITUTION_MODAL_ID, ParentInstitutionModalComponent)
  ],
  entryComponents: [
    ParentInstitutionModalComponent
  ]
})
export class ParentInstitutionModalModule { }
