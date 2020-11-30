import {Component, OnDestroy, OnInit} from '@angular/core';
import {Legend} from '../../state/legend/legend.model';
import {FormControl} from '@ng-stack/forms';
import {untilDestroyed} from 'ngx-take-until-destroy';

@Component({
  selector: 's4e-legend-designer',
  templateUrl: './legend-designer.component.html',
  styleUrls: ['./legend-designer.component.scss']
})
/**
 * This component should only be available in development mode, it is created so that
 * creation of legend's urls is simplified for people responsible for content creation
 */
export class LegendDesignerComponent implements OnInit, OnDestroy {
  legend: Legend = {
    type: 'gradient',
    url: '/assets/images/gfx_temp_legend.svg',
    leftDescription: {
      '0.0': 'something',
      '0.25': 'something',
      '0.50': 'something',
      '1.0': 'something'
    },
    rightDescription: {},
    topMetric: {},
    bottomMetric: {}
  };

  invalidJSON: boolean = false;
  isOpen = true;

  jsonFc: FormControl<string> = new FormControl<string>(JSON.stringify(this.legend, null, 2));

  constructor() {
    this.jsonFc.valueChanges.pipe(untilDestroyed(this)).subscribe(json => {
      try {
        this.invalidJSON = false;
        this.legend = JSON.parse(json);
      } catch (e) {
        this.invalidJSON = true;
      }
    })
  }

  ngOnInit() {
  }

  ngOnDestroy(): void {
  }
}
