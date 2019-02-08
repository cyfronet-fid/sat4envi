import {Component} from '@angular/core';
import {LoginService} from '../auth/login.service';

@Component({
  selector: 's4e-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.scss']
})
export class ProfileComponent {

  constructor(private loginService: LoginService) { }

}
