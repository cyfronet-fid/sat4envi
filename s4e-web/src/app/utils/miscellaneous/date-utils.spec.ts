import {yyyymm, yyyymmdd} from './date-utils';


describe('convertion to string', () => {
  beforeEach(() => {
  });

  it('should format year month day', () => {
    let date = new Date(2019, 0, 19);
    expect(yyyymmdd(date)).toBe('2019-01-19');

    date = new Date(2016, 11, 17);
    expect(yyyymmdd(date)).toBe('2016-12-17');
  });

  it('should format year month', () => {
    const date = new Date(2019, 4, 19);
    expect(yyyymm(date)).toBe('2019-05');
  });
});
