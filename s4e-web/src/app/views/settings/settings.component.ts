import { settingsRoutes } from './settings.routes';
import { BreadcrumbService } from './breadcrumb/breadcrumb.service';
import { InstitutionService } from './state/institution/institution.service';
import { ModalQuery } from './../../modal/state/modal.query';
import { ModalService } from './../../modal/state/modal.service';
import { Component, OnInit, ViewChild, HostListener } from '@angular/core';
import {SessionService} from '../../state/session/session.service';
import {Observable} from 'rxjs';
import {ProfileQuery} from '../../state/profile/profile.query';
import { map } from 'rxjs/operators';
import { InstitutionsSearchResultsQuery } from './state/institutions-search/institutions-search-results.query';
import { InstitutionsSearchResultsService } from './state/institutions-search/institutions-search-results.service';
import { Router, ActivatedRoute } from '@angular/router';
import { Institution } from './state/institution/institution.model';
import { environment } from 'src/environments/environment';
import { hasBeenClickedOutside } from 'src/app/utils';

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
  @HostListener('document:click', ['$event.target'])
  onClick(target) {
    if (!!this.search && hasBeenClickedOutside(this.search, target)) {
      this.isInUse = false;
    }
  }

  constructor(
    private _institutionsSearchResultsQuery: InstitutionsSearchResultsQuery,
    private _institutionsSearchResultsService: InstitutionsSearchResultsService,
    private _institutionService: InstitutionService,
    private sessionService: SessionService,
    private profileQuery: ProfileQuery,
    private _router: Router,
    private _activatedRoute: ActivatedRoute,
    private _modalService: ModalService,
    private _modalQuery: ModalQuery
  ) {}

  ngOnInit() {
    this.showInstitutions$ = this.profileQuery.selectCanSeeInstitutions();
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
    if(!!firstSearchResult) {
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
