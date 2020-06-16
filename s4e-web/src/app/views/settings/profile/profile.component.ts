import { Component, OnInit } from '@angular/core';
import {SessionQuery} from '../../../state/session/session.query';
import {Observable} from 'rxjs';

@Component({
  selector: 's4e-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.scss']
})
export class ProfileComponent implements OnInit {
  public isLoggedIn$: Observable<boolean>;
  public userEmail$: Observable<string>;

  constructor(private _sessionQuery: SessionQuery) {}

  ngOnInit(): void {
    this.isLoggedIn$ = this._sessionQuery.isLoggedIn$();
    this.userEmail$ = this._sessionQuery.select(state => state.email);
  }
}
