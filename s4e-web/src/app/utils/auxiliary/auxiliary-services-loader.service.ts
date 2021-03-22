import {Inject, Injectable} from '@angular/core';
import {RemoteConfiguration} from '../initializer/config.service';
import * as jQuery from 'jquery';
import {map, take, tap} from 'rxjs/operators';
import {filterTrue} from '../rxjs/observable';
import {DOCUMENT} from '@angular/common';
import {WINDOW} from '../../app.providers';

@Injectable({providedIn: 'root'})
export class AuxiliaryServicesLoader {
  private analyticsHref?: string;

  constructor(
    private remoteConfiguration: RemoteConfiguration,
    @Inject(WINDOW) private window: any,
    @Inject(DOCUMENT) private document: Document
  ) {}

  load$(): Promise<boolean> {
    return this.remoteConfiguration
      .getIsInitialized$()
      .pipe(
        filterTrue(),
        take(1),
        tap(() => {
          const helpdesk = this.remoteConfiguration.get().helpdesk;
          this.window.jQuery = jQuery;
          if (helpdesk?.type === 'jira' && helpdesk?.href) {
            jQuery.ajax({
              url: helpdesk.href,
              type: 'get',
              cache: true,
              dataType: 'script'
            });
          }
        }),
        tap(() => {
          const analytics = this.remoteConfiguration.get().analytics;
          if (analytics?.type === 'matomo' && analytics?.href) {
            this.analyticsHref = analytics.href;

            const _paq = (this.window._paq = this.window._paq || []);
            _paq.push(['deleteCustomVariables', 'page']);
            _paq.push(['trackPageView']);
            _paq.push(['setTrackerUrl', this.analyticsHref + 'matomo.php']);
            _paq.push(['setSiteId', '1']);
            // this is code copied from MATOMO documentation
            this.updateAnalytics();
            /* tracker methods like "setCustomDimension" should be called before "trackPageView" */
            (() => {
              const d = this.document,
                g = d.createElement('script'),
                s = d.getElementsByTagName('script')[0];
              g.type = 'text/javascript';
              g.async = true;
              g.src = analytics.href + 'matomo.js';
              s.parentNode.insertBefore(g, s);
            })();
          }
        }),
        map(() => true)
      )
      .toPromise();
  }

  updateAnalytics() {
    if (!this.analyticsHref) {
      return;
    }

    const _paq = (this.window._paq = this.window._paq || []);
    const content = this.document.getElementsByTagName('s4e-root')[0];
    _paq.push(['MediaAnalytics::scanForMedia', content]);
    _paq.push(['FormAnalytics::scanForForms', content]);
    _paq.push(['trackContentImpressionsWithinNode', content]);
    _paq.push(['enableLinkTracking']);
  }
}
