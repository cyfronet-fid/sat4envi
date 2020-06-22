import { ImageWMS } from 'ol/source';

export const IMAGE_WMS_LAYER = 'image-wms-layer-loader';
export class ImageWmsLoader {
  public start$: Promise<void>;
  public end$: Promise<void>;

  protected _error$: Promise<void>;

  constructor(source: ImageWMS) {
    this._error$ = new Promise((resolve, reject) => {
      source.once('imageloaderror', () => resolve());
    });

    this.end$ = new Promise((resolve, reject) => {
      source.once('imageloadend', () => resolve());
      this._error$.then(() => reject());
    });

    this.start$ = new Promise((resolve, reject) => {
      source.once('imageloadstart', () => resolve());
      this._error$.then(() => reject());
    });
  }
}
