import { Component, OnInit } from '@angular/core';

import {LoginService} from '../auth/login.service';

@Component({
  selector: 's4e-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  private email: string;
  private password: string;

  constructor(private loginService: LoginService) { }

  handleLogin() {
    console.log('handleLogin');
    this.loginService.login(this.email, this.password).subscribe(() => {
      location.reload();
    });
  }

  handleLogout() {
    console.log('handleLogout');
    this.loginService.logout();
  }

}
