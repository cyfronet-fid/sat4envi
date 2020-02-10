import * as JsPDF from 'jspdf';
import {forkJoin, fromEvent, Observable, ReplaySubject} from 'rxjs';
import {delay, filter, map, switchMap, take, tap} from 'rxjs/operators';
import {HttpClient} from '@angular/common/http';
import {fromPromise} from 'rxjs/internal-compatibility';
import moment from "moment";

interface ImageMeta {
  data: string;
  width: number;
  height: number;
}

interface ReportImages {
  s4eLogo: null | ImageMeta;
  partners: null | ImageMeta;
}

class LegendComposer {
  private width: number;
  private height: number;
  private currentY: number;
  readonly PPM = (72.0 / 25.6); // density per mm
  readonly IMAGE_SCALING = 0.125;
  readonly FONT_NAME = 'Ubuntu-Regular';
  private direction: 'bottom-top' | 'top-bottom';

  constructor(private doc: JsPDF,
              private xStart: number,
              private yStart: number,
              private xEnd: number,
              private yEnd: number,
              private defaultSpacing: number = 0) {

    this.width = this.xEnd - this.xStart;
    this.height = this.yEnd - this.yStart;
    this.direction = 'top-bottom';
    this.currentY = this.yStart;
  }

  private advanceY(distance: number) {
    if (this.direction === 'top-bottom') {
      this.currentY += distance;
    } else if (this.direction === 'bottom-top') {
      this.currentY -= distance;
    }
  }

  public insertTextBox(text: string, options?: Partial<{fontSize: number, fontStyle: 'bold'|'normal', align: 'right' | 'left' | 'center', margin}>) {
    options = options || {};
    let align = options.align || 'left';
    let marginTop = options.margin || this.defaultSpacing;
    let fontStyle = options.fontStyle || 'normal';

    if (options.fontSize !== undefined) {
      this.doc.setFontSize(options.fontSize);
    }

    this.doc.setFont(this.FONT_NAME, fontStyle);

    this.advanceY(marginTop);

    let x: number = this.xStart;
    let y: number = this.currentY;

    switch (align) {
      case 'center': {
        x = this.xStart + this.width / 2.0;
        break;
      }
      case 'right': {
        x = this.xEnd;
        break;
      }
    }
    const textBoxHeight = this.doc.splitTextToSize(text, this.width).length * this.doc.getFontSize() / this.PPM * this.doc.getLineHeightFactor();

    if (this.direction === 'bottom-top') {
      y -= textBoxHeight;
    }

    this.doc.text(text, x, y, {maxWidth: this.width, align: align, baseline: 'top'});

    this.advanceY(textBoxHeight);
  }

  public insertImage(img: ImageMeta, marginTop?: number) {
    this.advanceY(marginTop || this.defaultSpacing);

    const imgWidthScaled = img.width * this.IMAGE_SCALING;
    const imgHeightScaled = img.height * this.IMAGE_SCALING;

    let y = this.currentY;

    if (this.direction === 'bottom-top') {
      y -= imgHeightScaled;
    }

    this.doc.addImage(img.data, 'PNG', this.xStart + (this.width - imgWidthScaled) / 2.0, y, imgWidthScaled, imgHeightScaled);

    this.advanceY(imgHeightScaled);
  }

  setFlow(direction: 'bottom-top' | 'top-bottom') {
    if (direction === 'top-bottom') {
      this.currentY = this.yStart;
    } else if (direction === 'bottom-top') {
      this.currentY = this.yEnd;
    }
    this.direction = direction;
  }
}

export class ReportGenerator {
  public readonly loading$: ReplaySubject<boolean> = new ReplaySubject<boolean>(1);
  public readonly working$: ReplaySubject<boolean> = new ReplaySubject<boolean>(1);

  private images: ReportImages = {
    s4eLogo: null,
    partners: null,
  };

  constructor(private http: HttpClient,
              private readonly image: string,
              private readonly imageWidth: number,
              private readonly imageHeight: number) {
    this.loading$.next(true);
    this.working$.next(false);
  }

  private loadImage(url: string, key: keyof ReportImages): Observable<any> {
    return this.http.get(url, {observe: 'response', responseType: 'blob'})
      .pipe(switchMap(
        img => {

          const reader = new FileReader();
          const r = fromEvent(reader, 'load').pipe(take(1), switchMap(
            () => {
              const image = new Image();
              const _r = fromEvent(image, 'load').pipe(take(1), tap(() => {
                this.images[key] = {
                  height: image.height,
                  width: image.width,
                  data: reader.result as string
                };
              }));

              image.src = reader.result as string;
              return _r;
            }
          ));

          reader.readAsDataURL(img.body);
          return r;
        }),
      );
  }

  private loadFont(fontPromise: Promise<any>): Observable<any> {
    return fromPromise(fontPromise.then((fontModule) => {
      fontModule.registerFont(JsPDF);
    }));
  }

  public generate(caption: string, notes: string, productName: string, sceneDate: string): Observable<any> {
    // ignore this call if the generator is doing something
    return this.working$.pipe(
      take(1),
      filter(w => !w),
      tap(() => this.working$.next(true)),
      delay(0),
      tap(() => this.combineDocument(caption, notes, productName, sceneDate)),
      delay(0),
      tap(() => this.working$.next(false))
    );
  }

  /**
   *
   * @return height of the added text box
   */

  private combineDocument(caption: string, notes: string, productName: string, sceneDate: string) {
    if(notes.length > 800) {
      throw Error('description too long');
    }
    if(caption.length > 80) {
      throw Error('caption too long');
    }

    const doc = new JsPDF({
      orientation: 'landscape',
      unit: 'mm',
      format: 'a4'
    });

    const A4Width = 297;
    const A4Height = 210;
    const printImageHeight = this.imageHeight / this.imageWidth * A4Width;
    const margin = 10;
    const middleMargin = margin;

    const headerFontSize = 14.0;
    const noteFontSize = 10.0;
    const detailsFontSize = 9.0;
    const noteHeaderFontSize = 12.0;

    const composer = new LegendComposer(doc, A4Height, margin, A4Width - margin, A4Height - margin);

    composer.insertImage(this.images.s4eLogo);

    composer.insertTextBox(caption, {fontStyle: 'bold', fontSize: headerFontSize, margin: 5, align: 'center'});
    composer.insertTextBox('Opis', {fontStyle: 'bold', fontSize: noteHeaderFontSize, margin: 5});
    composer.insertTextBox(notes, {fontSize: noteFontSize, margin: 2.5});
    composer.insertTextBox('Szczegóły', {fontStyle: 'bold', fontSize: noteHeaderFontSize, margin: 5});

    if (productName) {
      composer.insertTextBox('Produkt', {fontSize: noteFontSize, fontStyle: 'bold', margin: 2});
      composer.insertTextBox(productName, {fontSize: noteFontSize});
    }

    if (sceneDate) {
      composer.insertTextBox('Data wykonania zdjęcia', {fontSize: noteFontSize, fontStyle: 'bold', margin: 5});
      composer.insertTextBox(sceneDate, {fontSize: noteFontSize});
    }

    composer.insertTextBox('Data wygenerowania raportu', {fontSize: noteFontSize, fontStyle: 'bold', margin: 5});
    composer.insertTextBox(moment(new Date()).format('DD.MM.YYYY g. HH:mm'), {fontSize: noteFontSize});

    composer.setFlow('bottom-top');

    composer.insertTextBox('© 2020 sat4envi', {fontSize: detailsFontSize, align: 'center'});
    composer.insertTextBox('Mapa została wygenerowana w aplikacji www.sat4envi.pl', {fontSize: detailsFontSize, align: 'center', margin: 2});

    composer.insertImage(this.images.partners, 3);

    this.drawMapImage(doc, margin, A4Height, middleMargin);

    doc.save(`RAPORT.${new Date().toISOString()}.pdf`);
  }

  private drawMapImage(doc: JsPDF, margin, A4Height, middleMargin) {
    doc.rect(margin, margin, A4Height - margin - middleMargin, A4Height - 2 * margin, null);
    doc.clip();

    let imageScaleFactor = (A4Height - 2 * margin) / this.imageHeight;
    let printHeight = A4Height - 2 * margin;

    if (this.imageHeight > this.imageWidth) {
      imageScaleFactor = (A4Height - 2*margin) / this.imageWidth;
      printHeight = this.imageHeight * imageScaleFactor
    }

    let printWidth = this.imageWidth * imageScaleFactor;

    doc.addImage(this.image, 'PNG', A4Height / 2 - printWidth / 2, A4Height / 2 - printHeight / 2, printWidth, printHeight);

    doc.setDrawColor('#3a3a3a');
    doc.rect(margin, margin, A4Height - margin - middleMargin, A4Height - 2 * margin);
  }

  loadAssets() {
    forkJoin([
      this.loadFont(import('./fonts/Ubuntu-Regular-normal')),
      this.loadFont(import('./fonts/Ubuntu-Regular-bold')),
      this.loadImage('./assets/images/logo_s4e_color.png', 's4eLogo'),
      this.loadImage('./assets/images/logo_partners.png', 'partners')
    ]).subscribe(
      () =>
        this.loading$.next(false),
      error => this.loading$.error(error)
    );
  }
}
