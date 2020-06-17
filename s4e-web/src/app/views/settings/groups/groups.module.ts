import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ReactiveFormsModule} from '@angular/forms';
import {RouterModule} from '@angular/router';
import {S4EFormsModule} from '../../../form/form.module';
import {GenericListViewModule} from '../components/generic-list-view/generic-list-view.module';
import {GroupListComponent} from './group-list/group-list.component';
import {GroupFormComponent} from './group-form/group-form.component';
import {GroupStore} from './state/group.store';
import {GroupQuery} from './state/group.query';
import {GroupService} from './state/group.service';
import { makeModalProvider } from 'src/app/modal/modal.providers';
import { GROUP_FORM_MODAL_ID } from './group-form/group-form-modal.model';
import { ModalModule } from 'src/app/modal/modal.module';

@NgModule({
  declarations: [
    GroupListComponent,
    GroupFormComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    GenericListViewModule,
    S4EFormsModule,
    ModalModule
  ],
  exports: [
    GroupListComponent,
    GroupFormComponent
  ],
  providers: [
    GroupStore,
    GroupQuery,
    GroupService,
    makeModalProvider(GROUP_FORM_MODAL_ID, GroupFormComponent)
  ]
})
export class GroupsModule {
}
