import { NotificationService } from 'notifications';
import {Component, ViewEncapsulation} from '@angular/core';
import {FormControl, FormGroup, Validators} from '@ng-stack/forms';
import {AkitaNgFormsManager} from '@datorama/akita-ng-forms-manager';
import {FormState} from '../../state/form/form.model';
import {validateAllFormFields} from '../../utils/miscellaneous/miscellaneous';
import {SessionQuery} from '../../state/session/session.query';
import {SessionService, BACK_LINK_QUERY_PARAM} from '../../state/session/session.service';
import {LoginFormState} from '../../state/session/session.model';
import { ActivatedRoute, Router, ParamMap } from '@angular/router';
import {GenericFormComponent} from '../../utils/miscellaneous/generic-form.component';
import {filter, map, switchMap, tap} from 'rxjs/operators';
import { InvitationService, TOKEN_QUERY_PARAMETER, REJECTION_QUERY_PARAMETER } from '../settings/people/state/invitation/invitation.service';
import {untilDestroyed} from 'ngx-take-until-destroy';

@Component({
  selector: 's4e-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class LoginComponent extends GenericFormComponent<SessionQuery, LoginFormState> {
  constructor(
    fm: AkitaNgFormsManager<FormState>,
    private _router: Router,
    private _sessionService: SessionService,
    private _sessionQuery: SessionQuery,
    private _activatedRoute: ActivatedRoute,
    private _invitationService: InvitationService,
    private _notificationService: NotificationService
  ) {
    super(fm, _router, _sessionQuery, 'login');
  }

  ngOnInit() {
    this._loadBackLink();
    this._handleInvitation();

    this.form = new FormGroup<LoginFormState>({
      email: new FormControl('', [Validators.required, Validators.email]),
      password: new FormControl('', [Validators.required])
    });

    super.ngOnInit();
  }

  login() {
    validateAllFormFields(this.form, {formKey: this.formKey, fm: this.fm});
    if (this.form.invalid) {
      return;
    }

    this._sessionService.login$(this.form.value)
      .pipe(
        untilDestroyed(this),
        switchMap(() => this._activatedRoute.queryParamMap),
        tap(params => params.has(TOKEN_QUERY_PARAMETER)
          ? this._invitationService.confirm(params.get(TOKEN_QUERY_PARAMETER))
          : null
        )
      )
      .subscribe(() => this._sessionService.goToLastUrl());
  }

  protected _loadBackLink() {
    this._activatedRoute.queryParamMap
      .pipe(
        filter((params) => params.has(BACK_LINK_QUERY_PARAM)),
        map((params) => params.get(BACK_LINK_QUERY_PARAM))
      )
      .subscribe((backLink) => !!backLink ? this._sessionService.setBackLink(backLink) : null);
  }

  protected _handleInvitation() {
    this._activatedRoute.queryParamMap
      .pipe(filter((params) => params.has(TOKEN_QUERY_PARAMETER)))
      .subscribe((params) => {
        if (params.has(REJECTION_QUERY_PARAMETER)) {
          const token = params.get(TOKEN_QUERY_PARAMETER);
          this._invitationService.reject(token);
          return;
        }

        const content = 'Zaloguj się lub zarejestruj, żeby dołączyć do instytucji';
        this._notificationService.addGeneral({
          content,
          type: 'info'
        });
      });
  }
}
