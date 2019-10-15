import {OnDestroy, OnInit} from '@angular/core';
import {AkitaNgFormsManager} from '@datorama/akita-ng-forms-manager';
import {FormState} from '../../state/form/form.model';
import {NavigationEnd, Router} from '@angular/router';
import {devRestoreFormState} from './miscellaneous';
import {debounceTime, filter} from 'rxjs/operators';
import {untilDestroyed} from 'ngx-take-until-destroy';
import {connectErrorsToForm} from './forms';
import {environment} from '../../../environments/environment';
import {Observable} from 'rxjs';
import {Query} from '@datorama/akita';
import {FormGroup} from '@ng-stack/forms';

export class GenericFormComponent<Q extends Query<any>, FS extends object> implements OnInit, OnDestroy {
  public loading$: Observable<boolean>;
  public form: FormGroup<FS> = null;
  public error$: Observable<any>;

  constructor(protected fm: AkitaNgFormsManager<FormState>,
              protected router: Router,
              protected query: Q,
              protected formKey: keyof FormState) {}

  ngOnInit(): void {
    if (this.form == null)
      throw new Error('GenericFormComponent has no defined `form`, be sure to assign it before calling `super.ngOnInit()`');

    this.loading$ = this.query.selectLoading();
    this.error$ = this.query.selectError();

    devRestoreFormState(this.fm.query.getValue()[this.formKey], this.form);
    this.fm.upsert(this.formKey, this.form);

    // In order for dev error setting to work debounceTime(100) must be set
    this.query.selectError().pipe(debounceTime(100), untilDestroyed(this))
      .subscribe(errors => connectErrorsToForm(errors, this.form));

    if(environment.hmr) {
      this.router.events.pipe(filter(event => event instanceof NavigationEnd))
        .subscribe(() => this.fm.remove(this.formKey));
    }
  }

  ngOnDestroy(): void {
    this.fm.unsubscribe(this.formKey);
  }
}
