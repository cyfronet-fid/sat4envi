/*
 * Copyright 2021 ACC Cyfronet AGH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import {TestBed} from '@angular/core/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {ReportTemplateService} from './report-template.service';
import {RouterTestingModule} from '@angular/router/testing';
import {HttpClient} from '@angular/common/http';
import {of} from 'rxjs';
import {ReportTemplateStore} from './report-template.store';
import {ProductQuery} from '../../../state/product/product.query';
import {OverlayQuery} from '../../../state/overlay/overlay.query';
import {ReportTemplateFactory} from './report-template.factory.spec';
import {ProductService} from '../../../state/product/product.service';
import {OverlayService} from '../../../state/overlay/overlay.service';
import {LocalStorageTestingProvider} from '../../../../../app.configuration.spec';
import {NotificationService} from '../../../../../notifications/state/notification.service';

describe('Report template Service', () => {
  let reportTemplatesService: ReportTemplateService;
  let httpClient: HttpClient;
  let store: ReportTemplateStore;
  let notificationService: NotificationService;
  let productQuery: ProductQuery;
  let overlayQuery: OverlayQuery;
  let productService: ProductService;
  let overlayService: OverlayService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [LocalStorageTestingProvider],
      imports: [HttpClientTestingModule, RouterTestingModule]
    });

    store = TestBed.inject(ReportTemplateStore);
    reportTemplatesService = TestBed.inject(ReportTemplateService);
    httpClient = TestBed.inject(HttpClient);
    notificationService = TestBed.inject(NotificationService);
    productQuery = TestBed.inject(ProductQuery);
    overlayQuery = TestBed.inject(OverlayQuery);
    productService = TestBed.inject(ProductService);
    overlayService = TestBed.inject(OverlayService);
  });

  it('should be created', () => {
    expect(reportTemplatesService).toBeDefined();
  });
  it('should delete template', () => {
    const spyHttp = spyOn(httpClient, 'delete').and.returnValue(of());
    const spyStoreRemove = spyOn(store, 'remove');
    const spyNotifications = spyOn(notificationService, 'addGeneral');

    const reportTemplateToRemove = ReportTemplateFactory.build();
    reportTemplatesService.delete$(reportTemplateToRemove).subscribe(() => {
      expect(spyHttp).toHaveBeenCalled();
      expect(spyStoreRemove).toHaveBeenCalledWith(reportTemplateToRemove.uuid);
      expect(spyNotifications).toHaveBeenCalled();
    });
  });
  it('should create template', () => {
    spyOn(productQuery, 'getActive').and.returnValue({id: 1});
    spyOn(overlayQuery, 'getActive').and.returnValue([1, 2, 3]);

    const createdReportTemplate = ReportTemplateFactory.build();
    const spyHttp = spyOn(httpClient, 'post').and.returnValue(of());
    const spyStoreAdd = spyOn(store, 'add');
    const spyNotifications = spyOn(notificationService, 'addGeneral');

    reportTemplatesService.create$(reportTemplatesService as any).subscribe(() => {
      expect(spyHttp).toHaveBeenCalled();
      expect(spyStoreAdd).toHaveBeenCalledWith(createdReportTemplate);
      expect(spyNotifications).toHaveBeenCalled();
    });
  });
  it('should load template', () => {
    const spyProductServiceSetActive = spyOn(
      productService,
      'setActive$'
    ).and.returnValue(of());
    const spyProductServiceGetLastScene = spyOn(
      productService,
      'getLastAvailableScene$'
    ).and.returnValue(of());
    const spyOverlayServiceSetActive = spyOn(overlayService, 'setAllActive');
    const spyStoreSetActive = spyOn(store, 'setActive');

    const reportTemplate = ReportTemplateFactory.build();
    reportTemplatesService.load$(reportTemplate).subscribe(() => {
      expect(spyProductServiceSetActive).toHaveBeenCalledWith(
        reportTemplate.productId
      );
      expect(spyProductServiceGetLastScene).toHaveBeenCalled();
      expect(spyOverlayServiceSetActive).toHaveBeenCalledWith(
        reportTemplate.overlayIds
      );
      expect(spyStoreSetActive).toHaveBeenCalledWith(reportTemplate.uuid);
    });
  });
});
