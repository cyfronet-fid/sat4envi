import { Component, OnInit } from '@angular/core';
import {SessionService} from '../../state/session/session.service';
import {Observable} from 'rxjs';
import {ProfileQuery} from '../../state/profile/profile.query';

@Component({
  selector: 's4e-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.scss']
})
export class SettingsComponent implements OnInit {
  showInstitutions$: Observable<boolean>;

  constructor(private sessionService: SessionService, private profileQuery: ProfileQuery) { }

  ngOnInit() {
    this.showInstitutions$ = this.profileQuery.selectCanSeeInstitutions();
  }
}
