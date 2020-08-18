import { IsEmptyPipe } from './is-empty.pipe';

describe('IsEmptyPipe', () => {
  it('create an instance', () => {
    const pipe = new IsEmptyPipe();
    expect(pipe).toBeTruthy();
    expect(pipe.transform({})).toBeTruthy();
    expect(pipe.transform({prop: 1})).toBeFalsy();
  });
});
