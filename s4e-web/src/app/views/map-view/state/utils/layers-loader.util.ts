import { Observable, Observer } from 'rxjs';
import { ImageWMS } from 'ol/source';

export const IMAGE_WMS_LAYER = 'image-wms-layer-loader';
export class ImageWmsLoader {
  public start$: Promise<void>;
  public end$: Promise<void>;

  protected _error$: Promise<void>;
  protected _isLoading = false;
  protected _hasEnded = false;

  constructor(source: ImageWMS) {
    this._error$ = new Promise((resolve, reject) => {
      source.on('imageloaderror', () => resolve());
    });

    this.end$ = new Promise((resolve, reject) => {
      source.on('imageloadend', () => resolve());
      this._error$.then(() => reject());
    });

    this.start$ = new Promise((resolve, reject) => {
      source.on('imageloadstart', () => resolve());
      this._error$.then(() => reject());
    });
  }
}
