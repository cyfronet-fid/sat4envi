import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { EventsDirective } from './events.directive';

@NgModule({
  declarations: [EventsDirective],
  imports: [
    CommonModule
  ],
  exports: [EventsDirective]
})
export class EventsModule { }
