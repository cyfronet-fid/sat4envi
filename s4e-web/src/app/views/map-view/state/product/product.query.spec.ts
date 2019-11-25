import {ProductQuery} from './product.query';
import {ProductStore} from './product.store';
import {SceneQuery} from '../scene/scene.query.service';
import {SceneStore} from '../scene/scene.store.service';

describe('ProductQuery', () => {
  let query: ProductQuery;

  beforeEach(() => {
    query = new ProductQuery(new ProductStore, new SceneQuery(new SceneStore()));
  });

  it('should create an instance', () => {
    expect(query).toBeTruthy();
  });

});
