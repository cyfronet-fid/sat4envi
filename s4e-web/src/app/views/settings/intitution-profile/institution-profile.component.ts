import { filter, map } from 'rxjs/operators';
import { InstitutionQuery } from './../state/institution/institution.query';
import { InstitutionService } from './../state/institution/institution.service';
import { ModalService } from './../../../modal/state/modal.service';
import { Institution } from './../state/institution/institution.model';
import { ActivatedRoute } from '@angular/router';
import { Component, OnInit, OnDestroy } from '@angular/core';
import { InstitutionsSearchResultsQuery } from '../state/institutions-search/institutions-search-results.query';
import { untilDestroyed } from 'ngx-take-until-destroy';
import { Observable, combineLatest } from 'rxjs';

@Component({
  selector: 's4e-institution-profile',
  templateUrl: './institution-profile.component.html',
  styleUrls: ['./institution-profile.component.scss']
})
export class InstitutionProfileComponent implements OnInit, OnDestroy {
  public activeInstitution: Institution | null;
  public isLoading$: Observable<boolean>;
  public institutions$: Observable<Institution[]>;
  public error$: Observable<any>;

  constructor(
    private _institutionsSearchResultsQuery: InstitutionsSearchResultsQuery,
    private _activatedRoute: ActivatedRoute,
    private _modalService: ModalService,
    private _institutionService: InstitutionService,
    private _institutionQuery: InstitutionQuery
  ) {
    this.isLoading$ = this._institutionQuery.selectLoading();
    this.institutions$ = combineLatest(
      this._institutionQuery.selectAll(),
      this._institutionsSearchResultsQuery.getInstitutionFrom$(this._activatedRoute)
    )
      .pipe(
        filter(([institutions, parentInstitution]) => !!parentInstitution && !!institutions && institutions.length > 0),
        map(([institutions, parentInstitution]) => this._filterChildrenDeep(institutions, [parentInstitution]))
      );
    this.error$ = this._institutionQuery.selectError();
  }

  ngOnInit() {
    this._institutionsSearchResultsQuery
      .getInstitutionFrom$(this._activatedRoute)
      .pipe(untilDestroyed(this))
      .subscribe(institution => this.activeInstitution = institution);
  }

  async deleteInstitution(slug: string) {
    if(await this._modalService.confirm('Usuń instytucję',
      'Czy na pewno chcesz usunąć tą instytucję? Operacja jest nieodwracalna.')) {
      this._institutionService.delete(slug);
    }
  }

  ngOnDestroy() {}

  protected _filterChildrenDeep(source: Institution[], parents: Institution[], children: Institution[] = []): Institution[] {
    children = [...children, ...parents];
    parents = source
      .filter(institution => parents.some(parent => institution.parentSlug === parent.slug));
    return !!parents && parents.length > 0 && this._filterChildrenDeep(source, parents, children)
      || !!children.shift() && children;
  }
}
