import {Component, OnDestroy, OnInit} from '@angular/core';
import {combineLatest, Observable} from 'rxjs';
import {SentinelSearchResult} from '../state/sentinel-search/sentinel-search.model';
import {SentinelSearchService} from '../state/sentinel-search/sentinel-search.service';
import {SentinelSearchQuery} from '../state/sentinel-search/sentinel-search.query';
import {FormControl as AngularFormControl, FormGroup as AngularFormGroup} from '@angular/forms';
import {untilDestroyed} from 'ngx-take-until-destroy';
import {disableEnableForm} from '../../../utils/miscellaneous/miscellaneous';
import {AkitaNgFormsManager} from '@datorama/akita-ng-forms-manager';
import {Router} from '@angular/router';
import {FormState} from '../../../state/form/form.model';
import {SentinelSearchMetadata} from '../state/sentinel-search/sentinel-search.metadata.model';
import {debounceTime, delay, map, switchMap} from 'rxjs/operators';
import {mapAllTrue, mapAnyTrue} from '../../../utils/rxjs/observable';
import {ModalService} from '../../../modal/state/modal.service';
import {SessionQuery} from '../../../state/session/session.query';

@Component({
  selector: 's4e-sentinel-search',
  templateUrl: './sentinel-search.component.html',
  styleUrls: ['./sentinel-search.component.scss']
})
export class SentinelSearchComponent implements OnInit, OnDestroy {
  searchResults$: Observable<SentinelSearchResult[]>;
  loading$: Observable<boolean>;
  sentinels$: Observable<SentinelSearchMetadata>;
  disableSearchBtn$: Observable<boolean>;
  showNoResults$: Observable<boolean>;

  form: AngularFormGroup = new AngularFormGroup({
    common: new AngularFormControl({})
  });

  loadingMetadata$: Observable<boolean>;
  showSearchResults$: Observable<boolean> = this.query.selectShowSearchResults();
  error$: Observable<any>;
  isLoggedIn$: Observable<boolean> = this.sessionQuery.isLoggedIn$();


  constructor(private fm: AkitaNgFormsManager<FormState>,
              private router: Router,
              private query: SentinelSearchQuery,
              private service: SentinelSearchService,
              private sessionQuery: SessionQuery,
              private modalService: ModalService) {
  }

  ngOnInit() {
    this.loading$ = this.query.selectLoading();
    this.error$ = this.query.selectError();
    this.loadingMetadata$ = this.query.selectMetadataLoading().pipe(delay(150));
    this.sentinels$ = this.query.selectSentinels();
    this.searchResults$ = this.query.selectAll();

    this.showNoResults$ = combineLatest([
      this.sentinels$.pipe(map(sentinels => sentinels.sections.length > 0)),
      this.searchResults$.pipe(map(results => results.length === 0)),
      this.query.selectLoaded()
    ]).pipe(mapAllTrue());

    this.disableSearchBtn$ = combineLatest([
      this.loading$,
      this.loadingMetadata$,
      this.query.selectSelectedSentinels().pipe(map(sentinels => sentinels.length === 0))
    ]).pipe(mapAnyTrue());

    this.service.getSentinels$()
      .pipe(
        map(metadata => this._makeFormFromMetadata(metadata)),
        switchMap((form) => this.service.connectQueryToForm(form)),
        switchMap(() => this.loading$),
        debounceTime(50),
        untilDestroyed(this)
      ).subscribe((loading) => disableEnableForm(loading, this.form));
  }

  ngOnDestroy(): void {
    this.service.setLoaded(false);
  }

  search() {
    if (this.form.invalid) {
      return;
    }

    this.service.search(this.form).subscribe();
  }

  openSearchResultModal(result: SentinelSearchResult) {
    this.service.openModalForResult(result.id);
  }

  clearResults() {
    this.service.clearResults();
  }

  redirectToLoginPage() {
    this.service.redirectToLoginPage();
  }

  private _makeFormFromMetadata(metadata: SentinelSearchMetadata): AngularFormGroup {
    const form = new AngularFormGroup({
      common: new AngularFormControl({})
    });
    metadata.sections.forEach(sentinel => form.setControl(sentinel.name, new AngularFormControl({})));
    this.form = form;
    return form;
  }
}
