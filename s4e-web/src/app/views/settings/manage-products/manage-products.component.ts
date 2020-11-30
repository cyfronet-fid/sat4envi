import {Component, OnDestroy, OnInit} from '@angular/core';
import {ProductQuery} from '../../map-view/state/product/product.query';
import {ProductService} from '../../map-view/state/product/product.service';
import {Observable} from 'rxjs';
import {Institution} from '../state/institution/institution.model';
import {InstitutionsSearchResultsQuery} from '../state/institutions-search/institutions-search-results.query';
import {ActivatedRoute} from '@angular/router';
import {untilDestroyed} from 'ngx-take-until-destroy';
import {map, switchMap, take} from 'rxjs/operators';
import {LicensedProduct} from '../../map-view/state/product/product.model';

@Component({
  selector: 's4e-settings',
  templateUrl: './manage-products.component.html',
  styleUrls: ['./manage-products.component.scss']
})
export class ManageProductsComponent implements OnInit, OnDestroy {
  public areLoading$ = this._productQuery.selectLoading();
  public errors$ = this._productQuery.selectError();
  public licences$ = this._productQuery.select('licensedProducts');

  public activeInstitution$: Observable<Institution> = this._institutionsSearchResultsQuery
    .selectActive$(this._activatedRoute);

  constructor(
    private _productQuery: ProductQuery,
    private _productService: ProductService,
    private _institutionsSearchResultsQuery: InstitutionsSearchResultsQuery,
    private _activatedRoute: ActivatedRoute
  ) {}

  ngOnInit() {
    this._productService.get()
      .pipe(untilDestroyed(this))
      .subscribe();
    this.activeInstitution$
      .pipe(
        untilDestroyed(this),
        switchMap(institution => this._productService.getProductsLicencesFor$(institution))
      )
      .subscribe();
  }

  toggleInstitutionVisibilityOf(licensedProduct: LicensedProduct) {
    this.activeInstitution$
      .pipe(
        switchMap(institution => licensedProduct.institutionsSlugs.includes(institution.slug)
          ? this._productService.removeProductLicence(licensedProduct, institution)
          : this._productService.addProductLicence(licensedProduct, institution)
        )
      )
      .subscribe();
  }

  ngOnDestroy() {}
}
