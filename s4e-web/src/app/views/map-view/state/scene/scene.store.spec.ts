import { SceneStore } from './scene.store.service';

describe('SceneStore', () => {
  let store: SceneStore;

  beforeEach(() => {
    store = new SceneStore();
  });

  it('should create an instance', () => {
    expect(store).toBeTruthy();
  });

});
