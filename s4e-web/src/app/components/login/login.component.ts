import {Component, OnDestroy, OnInit} from '@angular/core';
import {SessionService} from '../../state/session/session.service';
import {SessionQuery} from '../../state/session/session.query';
import {Observable} from 'rxjs';
import {FormBuilder, FormGroup} from '@angular/forms';
import {AkitaNgFormsManager} from '@datorama/akita-ng-forms-manager';
import {FormState} from '../../state/form/form.model';
import {untilDestroyed} from 'ngx-take-until-destroy';
import {disableEnableForm} from '../../utils/miscellaneous/miscellaneous';

@Component({
  selector: '[s4e-login]',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit, OnDestroy {
  private email: string;
  private password: string;
  public userEmail$: Observable<string>;
  public isLoggedIn$: Observable<boolean>;
  public form: FormGroup;
  $loading: Observable<boolean>;

  constructor(private sessionService: SessionService,
              private sessionQuery: SessionQuery,
              private fb: FormBuilder,
              private akitaNgFormsManager: AkitaNgFormsManager<FormState>) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      email: '',
      password: ''
    });

    this.akitaNgFormsManager.upsert('login', this.form);

    this.userEmail$ = this.sessionQuery.select(state => state.email);
    this.isLoggedIn$ = this.sessionQuery.isLoggedIn$();
    this.$loading = this.sessionQuery.selectLoading();

    this.$loading.pipe(untilDestroyed(this)).subscribe(isLoading => disableEnableForm(isLoading, this.form));
  }

  ngOnDestroy(): void {
  }

  login() {
    this.sessionService.login(this.email, this.password);
  }

  logout() {
    this.sessionService.logout();
  }

}
