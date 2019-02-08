import {Component} from '@angular/core';

import {LoginService} from '../auth/login.service';
import {Router} from '@angular/router';

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
    this.loginService.login(this.email, this.password).subscribe(() => {
      location.reload();
    });
  }

  handleLogout() {
    this.loginService.logout();
  }

}
