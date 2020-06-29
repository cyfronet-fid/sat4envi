import { GroupArrayPipe } from './group-array.pipe';

describe('GroupArrayPipe', () => {
  it('should partition correctly', () => {
    const pipe = new GroupArrayPipe();
    expect(pipe.transform([1, 2, 3, 4, 5], 2)).toEqual([[1, 2], [3, 4], [5]]);
    expect(pipe.transform([1, 2, 3, 4, 5], 3)).toEqual([[1, 2, 3], [4, 5]]);
  });
});
