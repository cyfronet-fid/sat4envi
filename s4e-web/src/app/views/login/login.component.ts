import { NotificationService } from './../../../../projects/notifications/src/lib/state/notification.service';
import { InvitationService, REJECTION_QUERY_PARAMETER, TOKEN_QUERY_PARAMETER } from './../settings/people/state/invitation.service';
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
import { filter, map } from 'rxjs/operators';

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
    if (!this.form.valid) {
      return;
    }

    this._sessionService.login(this.form.value, this._activatedRoute);
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
        }

        const content = 'Zaloguj się lub zarejestruj, żeby dołączyć do instytucji';
        this._notificationService.addGeneral({
          content,
          type: 'info'
        });
      });
  }
}
