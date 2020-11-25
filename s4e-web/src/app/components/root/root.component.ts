import {Component, Inject, OnDestroy, OnInit} from '@angular/core';
import {resetStores} from '@datorama/akita';
import {environment} from '../../../environments/environment';
import {NotificationService} from 'notifications';
import {ViewConfigurationQuery} from '../../views/map-view/state/view-configuration/view-configuration.query';
import {untilDestroyed} from 'ngx-take-until-destroy';
import {DOCUMENT} from '@angular/common';

@Component({
  selector: 's4e-root',
  templateUrl: './root.component.html',
  styleUrls: ['./root.component.scss']
})
export class RootComponent implements OnInit, OnDestroy {
  PRODUCTION: boolean = environment.production;

  constructor(
    private notificationService: NotificationService,
    private viewConfigurationQuery: ViewConfigurationQuery,
    @Inject(DOCUMENT) private document: Document
  ) {
    console.log('SOK version ' + environment.version);
  }

  ngOnInit() {
    this.viewConfigurationQuery.select('highContrast')
      .pipe(untilDestroyed(this))
      .subscribe(isHeightContrast => isHeightContrast
        ? this.document.body.classList.add('wcag_hc')
        : this.document.body.classList.remove('wcag_hc'));

    this.viewConfigurationQuery.select('largeFont')
      .pipe(untilDestroyed(this))
      .subscribe(isLargeFont => isLargeFont
        ? this.document.body.classList.add('wcag_fs')
        : this.document.body.classList.remove('wcag_fs'));
  }

  /**
   * THIS IS ONLY FOR NON PRODUCTION PURPOSES
   */
  devRefreshState() {
    resetStores();
    location.reload();
  }

  devShowNotifications() {
    this.notificationService.addGeneral({
      type: 'info',
      content: 'hello'
    });
    this.notificationService.addGeneral({
      type: 'warning',
      content: 'hello'
    });
    this.notificationService.addGeneral({
      type: 'success',
      content: 'hello'
    });
    this.notificationService.addGeneral({
      type: 'error',
      content: 'hello'
    });
  }

  ngOnDestroy() {}
}

