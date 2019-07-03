import { ErrorKeysPipe } from './error-keys.pipe';

describe('ErrorKeysPipe', () => {
  it('should work', () => {
    const pipe = new ErrorKeysPipe();
    expect(pipe.transform({__highlight__: '...', error_field: ['error']})).toEqual(['error_field'])
  });
});
