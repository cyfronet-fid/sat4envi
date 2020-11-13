import {Injectable} from '@angular/core';
import environment from '../../../../../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {ReportTemplateStore} from './report-template.store';
import {handleHttpRequest$} from '../../../../../common/store.util';
import {ReportTemplate} from './report-template.model';
import {finalize, tap} from 'rxjs/operators';
import {NotificationService} from 'notifications';
import {ProductQuery} from '../../../state/product/product.query';
import {OverlayQuery} from '../../../state/overlay/overlay.query';
import {ProductService} from '../../../state/product/product.service';
import {SceneService} from '../../../state/scene/scene.service';
import {OverlayService} from '../../../state/overlay/overlay.service';

@Injectable({providedIn: 'root'})
export class ReportTemplateService {
  static URL_BASE = `${environment.apiPrefixV1}/report-template`;

  constructor(
    private _http: HttpClient,
    private _store: ReportTemplateStore,
    private _notificationService: NotificationService,

    private _productService: ProductService,
    private _productQuery: ProductQuery,

    private _overlayService: OverlayService,
    private _overlayQuery: OverlayQuery
  ) {}

  public get$() {
    return this._http.get<ReportTemplate[]>(ReportTemplateService.URL_BASE)
      .pipe(
        handleHttpRequest$(this._store),
        tap((reportsTemplates: ReportTemplate[]) => this._store.set(reportsTemplates))
      );
  }

  public delete$(reportTemplate: ReportTemplate) {
    const url = ReportTemplateService.URL_BASE + '/' + reportTemplate.id;
    return this._http.delete(url)
      .pipe(
        handleHttpRequest$(this._store),
        tap(() => this._store.remove(reportTemplate.id)),
        tap(() => this._notificationService.addGeneral({
          content: 'Report template has been removed',
          type: 'success'
        }))
      );
  }

  public create$(reportTemplate: Partial<ReportTemplate>) {
    const activeProduct = this._productQuery.getActive();
    if (!!activeProduct) {
      reportTemplate = {...reportTemplate, productId: activeProduct.id};
    }

    const activeOverlaysIds = this._overlayQuery.getActive()
      .map(overlay => parseInt(overlay.id));
    reportTemplate = {...reportTemplate, overlaysIds: activeOverlaysIds};

    return this._http.post(ReportTemplateService.URL_BASE, reportTemplate)
      .pipe(
        handleHttpRequest$(this._store),
        tap((createdReportTemplate: ReportTemplate) => this._store.add(createdReportTemplate)),
        tap(() => this._notificationService.addGeneral({
          content: 'Report template has been created',
          type: 'success'
        }))
      );
  }

  public load(reportTemplate: ReportTemplate) {
    const activeProductId = !!reportTemplate.productId ? reportTemplate.productId : null;
    this._productService.setActive(activeProductId);
    if (!!reportTemplate.productId) {
      this._productService.getLastAvailableScene();
    }

    this._overlayService.setAllActive(reportTemplate.overlaysIds as any[]);

    this._store.setActive(reportTemplate.id);
  }
}
