import { SceneQuery } from './scene.query.service';
import { SceneStore } from './scene.store.service';

describe('SceneQuery', () => {
  let query: SceneQuery;

  beforeEach(() => {
    query = new SceneQuery(new SceneStore);
  });

  it('should create an instance', () => {
    expect(query).toBeTruthy();
  });

});
