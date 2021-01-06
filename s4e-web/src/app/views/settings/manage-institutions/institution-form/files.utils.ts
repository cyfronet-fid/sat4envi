/*
 * Copyright 2021 ACC Cyfronet AGH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
  const MAX_HEIGHT = 200;
  const MAX_WIDTH = 200;

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
    let width = img.width;
    let height = img.height;

    // scale up
    if (width > MAX_WIDTH || height > MAX_HEIGHT) {
      scale = width > height
        ? width / MAX_WIDTH
        : height / MAX_HEIGHT;

      scale = !scale || scale === 0 ? 1 : scale;
    }

    // scale down
    if (width < MIN_WIDTH || img.height < MIN_HEIGHT) {
      scale = width < height
        ? width / MIN_WIDTH
        : height / MIN_HEIGHT;
    }
    width = width / scale;
    height = height / scale;

    canvas.width = width;
    canvas.height = height;

    canvas
      .getContext('2d')
      .drawImage(img, 0, 0, img.width, img.height, 0, 0, width, height);

    return canvas;
  }
}
