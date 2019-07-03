import * as Factory from "factory.ts";

export const ProductResponseFactory = Factory.makeFactory({
  id: Factory.each(i => i),
  productTypeId: Factory.each(i => i + 1000),
  timestamp: Factory.each(i => '2019-05-01T00:00:00'),
  layerName: Factory.each(i => `layer #${i}`)
});
