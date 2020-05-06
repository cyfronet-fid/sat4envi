import { SessionService } from './../../state/session/session.service';
import { Component, OnInit } from '@angular/core';

@Component({
  template: ''
})
export class LogoutComponent implements OnInit {

  constructor(private _sessionService: SessionService) {}

  ngOnInit() {
    this._sessionService.logout();
  }
}
