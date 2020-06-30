import { Observable, Observer } from 'rxjs';

export namespace File {
  export function getFirst($event) {
    const hasFirstFile = !!$event.target.files && !!$event.target.files[0];
    return hasFirstFile && $event.target.files[0] || null;
  }
}

export function getImageXhr(src: string, bearer: string): XMLHttpRequest {
  const xhr = new XMLHttpRequest();
  xhr.open('GET', src);
  xhr.responseType = 'arraybuffer';
  xhr.setRequestHeader('Authorization', bearer);

  return xhr;
}

export namespace ImageBase64 {
  export function getFromFile$(file) {
    return new Observable((observer: Observer<string>) => {
      const reader = new FileReader();
      reader.readAsDataURL(file);
      reader.onload = () => {
        const imageBase64 = !!reader.result
          ? (reader.result as string).substr('data:image/png;base64,'.length)
          : '';
        observer.next(imageBase64);
        observer.complete();
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
    const canvas = document.createElement('canvas');
    canvas.width = img.width;
    canvas.height = img.height;
    const ctx = canvas.getContext('2d');
    ctx.drawImage(img, 0, 0);
    return canvas
      .toDataURL('image/png')
      .replace(/^data:image\/(png|jpg);base64,/, '');
  }
}
