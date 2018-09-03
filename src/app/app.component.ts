import {Component, OnInit} from '@angular/core';
import Map from 'ol/Map';
import View from 'ol/View';
import { Tile, Image } from 'ol/layer';
import { ImageWMS, OSM } from 'ol/source';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
  static PRODUCT_VIEWS = [{
    id: 'opady',
    layers: [{
      id: 1,
      timestamp: '20180824T100000',
      layerName: 'test:201807051330_PL_HRV_gtif_mercator'
    }, {
      id: 2,
      timestamp: '20180824T110000',
      layerName: 'test:201807051330_PL_HRV_gtif_mercator'
    }, {
      id: 3,
      timestamp: '20180824T120000',
      layerName: 'test:201807051330_PL_HRV_gtif_mercator'
    }]
  }, {
    id: 'zachmurzenie',
    layers: [{
      id: 4,
      timestamp: '20180824T200000',
      layerName: 'test:201807051330_PL_HRV_gtif_mercator'
    }, {
      id: 5,
      timestamp: '20180824T210000',
      layerName: 'test:201807051330_PL_HRV_gtif_mercator'
    }, {
      id: 6,
      timestamp: '20180824T220000',
      layerName: 'test:201807051330_PL_HRV_gtif_mercator'
    }]
  }];

  private map: Map;
  productViews = AppComponent.PRODUCT_VIEWS;
  selectedProductView = this.productViews[0];
  selectedLayer: { timestamp: string; layerName: string };

  ngOnInit() {

    this.map = new Map({
      target: 'map',
      layers: [
        new Tile({
          source: new OSM()
        })
      ],
      view: new View({
        center: [2158581, 6841419],
        zoom: 6
      })
    });
  }

  selectProductView(id: string) {
    this.selectedProductView = this.productViews.find(productView => productView.id === id);
    this.showLayer(this.selectedProductView.layers[0]);
  }

  private showLayer(layer: { timestamp: string; layerName: string }) {
    this.selectedLayer = layer;
    const layers = this.map.getLayers();
    layers.clear();
    layers.push(new Tile({
      source: new OSM()
    }));
    layers.push(new Image({
      source: new ImageWMS({
        url: 'http://geoserver-3434.cloud.plgrid.pl/geoserver/wms',
        params: { 'LAYERS': layer.layerName }
      })
    }));
  }
}
