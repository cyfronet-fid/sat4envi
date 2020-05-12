import { CommonStateModule } from './../../state/common-state.module';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LogoutComponent } from './logout.component';
import { SessionService } from 'src/app/state/session/session.service';

@NgModule({
  declarations: [LogoutComponent],
  imports: [
    CommonModule,
    CommonStateModule
  ],
  exports: [LogoutComponent]
})
export class LogoutModule { }
