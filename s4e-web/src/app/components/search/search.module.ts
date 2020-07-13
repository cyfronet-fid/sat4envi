import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { SearchComponent } from './search.component';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { S4EFormsModule } from 'src/app/form/form.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { EventsModule } from 'src/app/utils/dropdown/events.module';

@NgModule({
  declarations: [
    SearchComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    S4EFormsModule,
    ReactiveFormsModule,
    FontAwesomeModule,
    EventsModule
  ],
  exports: [
    SearchComponent
  ]
})
export class SearchModule { }
