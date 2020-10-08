import { SessionQuery } from 'src/app/state/session/session.query';
import { filter, map, take } from 'rxjs/operators';
import { InstitutionQuery } from './../state/institution/institution.query';
import { InstitutionService } from './../state/institution/institution.service';
import { ModalService } from './../../../modal/state/modal.service';
import { Institution } from './../state/institution/institution.model';
import { ActivatedRoute } from '@angular/router';
import { Component, OnDestroy } from '@angular/core';
import { InstitutionsSearchResultsQuery } from '../state/institutions-search/institutions-search-results.query';
import { untilDestroyed } from 'ngx-take-until-destroy';
import { Observable, combineLatest } from 'rxjs';

@Component({
  selector: 's4e-institution-profile',
  templateUrl: './institution-profile.component.html',
  styleUrls: ['./institution-profile.component.scss']
})
export class InstitutionProfileComponent implements OnDestroy {
  public isLoading$: Observable<boolean> = this._institutionQuery.selectLoading();
  public activeInstitution$ = this._institutionsSearchResultsQuery
    .selectActive$(this._activatedRoute)
    .pipe(untilDestroyed(this));
  public childrenInstitutions$: Observable<Institution[]> = combineLatest(
      this._institutionQuery.selectAll(),
      this.activeInstitution$
    )
      .pipe(
        untilDestroyed(this),
        filter(([institutions, parentInstitution]) => !!parentInstitution && !!institutions && institutions.length > 0),
        map(([institutions, parentInstitution]) => this._filterChildrenDeep(institutions, [parentInstitution]))
      );
  public error$: Observable<any> = this._institutionQuery
    .selectError();

  public isManagerOfActive$ = this._institutionQuery
    .isManagerOf$(this.activeInstitution$)
    .pipe(untilDestroyed(this));

  constructor(
    private _institutionsSearchResultsQuery: InstitutionsSearchResultsQuery,
    private _activatedRoute: ActivatedRoute,
    private _modalService: ModalService,
    private _institutionService: InstitutionService,
    private _institutionQuery: InstitutionQuery,
    private _sessionQuery: SessionQuery
  ) {}

  isManagerOf(institution: Institution) {
    return this._institutionQuery.isManagerOf(institution);
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
