import { Observable, Observer } from 'rxjs';

export namespace File {
  export function getFirst($event) {
    const hasFirstFile = !!$event.target.files && !!$event.target.files[0];
    return hasFirstFile && $event.target.files[0] || null;
  }
}

export function getImageXhr(src: string): XMLHttpRequest {
  const xhr = new XMLHttpRequest();
  xhr.open('GET', src);
  xhr.responseType = 'arraybuffer';
  return xhr;
}

export namespace ImageBase64 {
  const MAX_HEIGHT = 500;
  const MAX_WIDTH = 500;

  const MIN_HEIGHT = 130;
  const MIN_WIDTH = 130;

  export function getFromFile$(file) {
    return new Observable((observer: Observer<string>) => {
      const reader = new FileReader();
      reader.readAsDataURL(file);
      reader.onload = () => {
        if (!reader.result) {
          observer.next('');
          observer.complete();
        }

        const img = new Image();
        img.crossOrigin = 'Anonymous';
        img.src = reader.result as string; //(reader.result as string).substr('data:image/png;base64,'.length);

        img.onload = () => {
          observer.next(getBase64Image(img));
          observer.complete();
        };
        img.onerror = (err) => observer.error(err);
      };
      reader.onerror = (error) => observer.error(error);
    });
  }

  export function getFromXhr(imageXhr: XMLHttpRequest): string {
    const arrayBufferView = new Uint8Array(imageXhr.response);
    const blob = new Blob([arrayBufferView], { type: 'image/png' });
    const urlCreator = (window as any).URL || (window as any).webkitURL;
    return urlCreator.createObjectURL(blob);
  }

  export function getFromUrl$(url: string) {
    return new Observable((observer: Observer<string>) => {
      const img = new Image();
      img.crossOrigin = 'Anonymous';
      img.src = url;

      img.onload = () => {
        observer.next(getBase64Image(img));
        observer.complete();
      };
      img.onerror = (err) => observer.error(err);
    });
  }

  function getBase64Image(img: HTMLImageElement) {
    const canvas = resizedCanvasFrom(img);
    return canvas
      .toDataURL('image/png')
      .replace(/^data:image\/(png|jpg);base64,/, '');
  }

  function resizedCanvasFrom(img: HTMLImageElement) {
    const canvas = document.createElement('canvas');

    let scale = 1;

    // scale up
    if (img.width > MAX_WIDTH || img.height > MAX_HEIGHT) {
      scale = img.width > img.height
        ? img.width / MAX_WIDTH
        : img.height / MAX_HEIGHT;

      scale = !scale || scale === 0 ? 1 : scale;
    }

    // scale down
    if (img.width < MIN_WIDTH || img.height < MIN_HEIGHT) {
      scale = img.width < img.height
        ? img.width / MIN_WIDTH
        : img.height / MIN_HEIGHT;
    }

    canvas.width = img.width / scale;
    canvas.height = img.height / scale;

    canvas
      .getContext('2d')
      .drawImage(img, 0, 0, canvas.width, canvas.height);

    return canvas;
  }
}
