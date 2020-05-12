import { Component, OnInit, OnDestroy } from '@angular/core';
import {Observable} from 'rxjs';
import {Sentinel, SentinelSearchForm, SentinelSearchResult} from '../state/sentinel-search/sentinel-search.model';
import {SentinelSearchService} from '../state/sentinel-search/sentinel-search.service';
import {SentinelSearchQuery} from '../state/sentinel-search/sentinel-search.query';
import {FormControl, FormGroup, Validators} from '@ng-stack/forms';
import {untilDestroyed} from 'ngx-take-until-destroy';
import {disableEnableForm, validateAllFormFields} from '../../../utils/miscellaneous/miscellaneous';
import {GenericFormComponent} from '../../../utils/miscellaneous/generic-form.component';
import {AkitaNgFormsManager} from '@datorama/akita-ng-forms-manager';
import {Router} from '@angular/router';
import {FormState} from '../../../state/form/form.model';

@Component({
  selector: 's4e-sentinel-search',
  templateUrl: './sentinel-search.component.html',
  styleUrls: ['./sentinel-search.component.scss']
})
export class SentinelSearchComponent extends GenericFormComponent<SentinelSearchQuery, SentinelSearchForm> {
  searchResults$: Observable<SentinelSearchResult[]>;
  loading$: Observable<boolean>;
  sentinels$: Observable<Sentinel[]>;
  form: FormGroup<SentinelSearchForm>;


  constructor(fm: AkitaNgFormsManager<FormState>,
              router: Router,
              query: SentinelSearchQuery,
              private service: SentinelSearchService) {
    super(fm, router, query, 'sentinelSearch');
  }

  ngOnInit() {
    this.form = new FormGroup<SentinelSearchForm>({
      sentinelId: new FormControl<string>(null, Validators.required),
      clouds: new FormControl<number>(null),
      dateEnd: new FormControl<string>(),
      dateStart: new FormControl<string>(),
    });

    this.loading$ = this.query.selectLoading();
    this.loading$
      .pipe(untilDestroyed(this))
      .subscribe(loading => disableEnableForm(loading, this.form));

    this.sentinels$ = this.query.selectSentinels();
    this.searchResults$ = this.query.selectAll();
    this.service.getSentinels();

    super.ngOnInit();
  }

  search() {
    validateAllFormFields(this.form, {formKey: this.formKey, fm: this.fm});

    if(this.form.invalid) {
      return;
    }

    this.service.search();
  }
}
