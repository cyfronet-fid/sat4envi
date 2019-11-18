import { Component, OnInit } from '@angular/core';
import {SessionService} from '../../state/session/session.service';

@Component({
  selector: 's4e-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.scss']
})
export class SettingsComponent implements OnInit {

  constructor(private sessionService: SessionService) { }

  ngOnInit() {
  }

  logout() {
    this.sessionService.logout();
  }
}
