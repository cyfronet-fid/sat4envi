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
  ],
  exports: [
    GroupListComponent,
    GroupFormComponent
  ],
  providers: [
    GroupStore,
    GroupQuery,
    GroupService
  ]
})
export class GroupsModule {
}
