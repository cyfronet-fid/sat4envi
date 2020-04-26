function noOp() {}

// mocking this function is required, as new open layers use it, but jsdom does not implement it yet
if (typeof window.URL.createObjectURL === 'undefined') {
  Object.defineProperty(window.URL, 'createObjectURL', { value: noOp})
}
