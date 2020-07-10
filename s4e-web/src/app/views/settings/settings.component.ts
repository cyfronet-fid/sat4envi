import {InstitutionService} from './state/institution/institution.service';
import {ModalQuery} from '../../modal/state/modal.query';
import {ModalService} from '../../modal/state/modal.service';
import {Component, HostListener, OnInit, ViewChild} from '@angular/core';
import {SessionService} from '../../state/session/session.service';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {InstitutionsSearchResultsQuery} from './state/institutions-search/institutions-search-results.query';
import {InstitutionsSearchResultsService} from './state/institutions-search/institutions-search-results.service';
import {ActivatedRoute, Router} from '@angular/router';
import {Institution} from './state/institution/institution.model';
import {environment} from 'src/environments/environment';
import {hasBeenClickedOutside} from 'src/app/utils';
import {SessionQuery} from '../../state/session/session.query';

@Component({
  selector: 's4e-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.scss']
})
export class SettingsComponent implements OnInit {
  showInstitutions$: Observable<boolean>;
  public institutions$: Observable<Institution[]>;
  public institutionsLoading$: Observable<boolean>;
  public areResultsOpen$: Observable<boolean>;

  public searchValue: string;
  public isInUse: boolean = false;

  public hasSelectedInstitution$: Observable<boolean>;

  @ViewChild('search') search;

  constructor(
    private _institutionsSearchResultsQuery: InstitutionsSearchResultsQuery,
    private _institutionsSearchResultsService: InstitutionsSearchResultsService,
    private _institutionService: InstitutionService,
    private sessionService: SessionService,
    private _sessionQuery: SessionQuery,
    private _router: Router,
    private _activatedRoute: ActivatedRoute,
    private _modalService: ModalService,
    private _modalQuery: ModalQuery
  ) {
  }

  @HostListener('document:click', ['$event.target'])
  onClick(target) {
    if (!!this.search && hasBeenClickedOutside(this.search, target)) {
      this.isInUse = false;
    }
  }

  ngOnInit() {
    this.showInstitutions$ = this._sessionQuery.selectCanSeeInstitutions();
    this.institutions$ = this._institutionsSearchResultsQuery.selectAll()
      .pipe(map(institutions => institutions.filter(institution => !!institution)));
    this.institutionsLoading$ = this._institutionsSearchResultsQuery.selectLoading();
    this.areResultsOpen$ = this._institutionsSearchResultsQuery.selectIsOpen();

    this.hasSelectedInstitution$ = this._institutionsSearchResultsQuery
      .hasInstitutionSlugIn$(this._activatedRoute);

    // TODO: set institution from storage
    if (environment.hmr) {
      const searchResult = this._institutionsSearchResultsQuery.getValue().searchResult;
      this.searchValue = !!searchResult ? searchResult.name : '';
    }

    this._institutionService.get();
  }

  searchForInstitutions(partialInstitutionName: string) {
    this._institutionsSearchResultsService.get(partialInstitutionName || '');
    this.isInUse = true;
  }

  selectFirstInstitution() {
    const firstSearchResult = this._institutionsSearchResultsQuery.getAll()[0];
    if (!!firstSearchResult) {
      this.selectInstitution(firstSearchResult);
    }
  }

  selectInstitution(institution: Institution | null) {
    this.searchValue = !!institution && institution.name || '';
    this.isInUse = false;
    this._institutionsSearchResultsService.setSelectedInstitution(institution);

    this._router.navigate(
      !!institution ? [] : ['/settings'],
      {
        relativeTo: this._activatedRoute,
        queryParams: {
          institution: !!institution && institution.slug || null
        },
        queryParamsHandling: 'merge'
      }
    );
  }

  resetSearch() {
    this.selectInstitution(null);
  }
}
