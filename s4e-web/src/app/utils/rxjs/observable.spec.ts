import {from, of} from 'rxjs';
import {mapAnyTrue, filterTrue, filterFalse, filterNull, filterNotNull, mapAllTrue} from './observable';
import {take, toArray} from 'rxjs/operators';
import {filterNil} from '@datorama/akita';

describe('isAnyTrue', () => {
  it('should work for arrays', async () => {
    expect(await from([
      [false, false, false],
      [true, true, false]
    ]).pipe(mapAnyTrue(), toArray()).toPromise()).toEqual([false, true]);
  });

  it('should work for booleans', async () => {
    expect(await from([
      false,
      true
    ]).pipe(mapAnyTrue(), toArray()).toPromise()).toEqual([false, true]);
  });
});

describe('mapAllTrue', () => {
  it('should work', async () => {
    expect(await from([
      [true, false, null],
      [true, true, true]
    ]).pipe(mapAllTrue(), toArray()).toPromise())
      .toEqual([false, true])
  });
});

describe('filterTrue', () => {
  it('should work', async () => {
    expect(await from([false, null, undefined, true, {}, NaN]).pipe(filterTrue(), toArray()).toPromise())
      .toEqual([true, {}])
  });
});

describe('filterFalse', () => {
  it('should work', async () => {
    expect(await from([false, null, undefined, true, {}, NaN]).pipe(filterFalse(), toArray()).toPromise())
      .toEqual([false, null, undefined, NaN])
  });
});

describe('filterNull', () => {
  it('should work', async () => {
    expect(await from([false, null, undefined, true, {}, NaN]).pipe(filterNull(), toArray()).toPromise())
      .toEqual([null, undefined])
  });
});


describe('filterNotNull', () => {
  it('should work', async () => {
    expect(await from([false, null, undefined, true, {}, NaN]).pipe(filterNotNull(), toArray()).toPromise())
      .toEqual([false, true, {}, NaN])
  });
});
