import {TileWMS} from 'ol/source';

export class TileLoader {
  public start$: Promise<void>;

  constructor(tile: TileWMS) {
    this.start$ = new Promise((resolve) => tile.once('tileloadstart', () => resolve()));
  }
}
