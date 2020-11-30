import Canvg from 'canvg';
import * as JsPDF from 'jspdf';
import {forkJoin, fromEvent, Observable, of, ReplaySubject} from 'rxjs';
import {delay, filter, switchMap, take, tap} from 'rxjs/operators';
import {HttpClient} from '@angular/common/http';
import {fromPromise} from 'rxjs/internal-compatibility';
import moment from 'moment';
import {Legend} from '../../state/legend/legend.model';

interface ImageMeta {
  data: string;
  width: number;
  height: number;
}

interface ReportImages {
  s4eLogo: null | ImageMeta;
  partners: null | ImageMeta;
}

interface ReportSVG {
  legend: null | string;
}

class ScaleComposer {
  readonly LEADING_DIGITS = [1, 2, 5];
  readonly SCALE_BAR_STEPS: number = 4;

  private width: number;
  private height: number;
  minWidth: number = 40;

  constructor(private doc: JsPDF,
              private xStart: number,
              private yStart: number,
              private xEnd: number,
              private yEnd: number) {
    this.width = this.xEnd - this.xStart;
    this.height = (this.yEnd - this.yStart) / 3;
    this.minWidth = this.width * 0.50;
  }

  drawScale(pointResolution: number) {
    const minWidth = this.minWidth;


    let nominalCount = minWidth * pointResolution;
    let suffix = '';

    if (nominalCount < 0.001) {
      suffix = 'μm';
      pointResolution *= 1000000;
    } else if (nominalCount < 1) {
      suffix = 'mm';
      pointResolution *= 1000;
    } else if (nominalCount < 1000) {
      suffix = 'm';
    } else {
      suffix = 'km';
      pointResolution /= 1000;
    }


    let i = 3 * Math.floor(Math.log(minWidth * pointResolution) / Math.log(10));
    let count, width, decimalCount;
    while (true) {
      decimalCount = Math.floor(i / 3);
      const decimal = Math.pow(10, decimalCount);
      count = this.LEADING_DIGITS[((i % 3) + 3) % 3] * decimal;
      width = Math.round(count / pointResolution);
      if (isNaN(width)) {
        return;
      } else if (width >= minWidth) {
        break;
      }
      ++i;
    }

    this.createScaleBar(width, count, suffix);
    this.doc.setDrawColor('#000000');
    this.doc.rect(this.xStart, this.yStart, width, this.height);
  }

  createScaleBar(width, scale, suffix) {
    const scaleSteps = [];
    const stepWidth = width / this.SCALE_BAR_STEPS;
    let currentX = this.xStart;
    let backgroundColor = '#ffffff';
    for (let i = 0; i < this.SCALE_BAR_STEPS; i++) {
      this.doc.setFillColor(backgroundColor);
      this.doc.rect(currentX, this.yStart, stepWidth, this.height, 'F');
      if (i === 0) {
        // create the first marker at position 0
        this.createMarker(currentX);
      }


      this.createMarker(currentX + stepWidth)
      this.createStepText(currentX, i, width, false, scale, suffix)
      if (i === this.SCALE_BAR_STEPS - 1) {
        {
          /*render text at the end */
        }
        this.createStepText(currentX + stepWidth, i + 1, width, true, scale, suffix);
      }
      // switch colors of steps between black and white
      if (backgroundColor === '#ffffff') {
        backgroundColor = '#000000';
      } else {
        backgroundColor = '#ffffff';
      }
      currentX += stepWidth;
    }
  }

  createStepText(xPosition, i, width, isLast, scale, suffix) {
    const length = i === 0 ? 0 : Math.round((scale / this.SCALE_BAR_STEPS) * i * 100) / 100;
    const lengthString = length + (i === 0 ? '' : ' ' + suffix);
    this.doc.setFontSize(5);
    this.doc.text(lengthString, xPosition, this.yStart + this.height * 2.1, {maxWidth: width / this.SCALE_BAR_STEPS, align: 'center', baseline: 'top'});
  }

  createMarker(xPosition: number) {
    this.doc.setDrawColor('#000000');
    this.doc.line(xPosition, this.yStart, xPosition, this.yStart + this.height * 2);
  }
}

class LegendComposer {
  readonly PPM = (72.0 / 25.6); // density per mm
  readonly IMAGE_SCALING = 0.125;
  readonly FONT_NAME = 'Ubuntu-Regular';
  private width: number;
  private height: number;
  private currentY: number;
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

  public insertScale(pointResolution: number) {
    this.doc.setFont(this.FONT_NAME);

    const composer = new ScaleComposer(this.doc, this.xStart, this.currentY + 2.5,
      this.xEnd, this.currentY + 7.5);

    composer.drawScale(pointResolution);

    this.advanceY(this.defaultSpacing + 7.5);
  }

  public insertTextBox(text: string, options?: Partial<{ fontSize: number, fontStyle: 'bold' | 'normal', align: 'right' | 'left' | 'center', margin }>) {
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

  private advanceY(distance: number) {
    if (this.direction === 'top-bottom') {
      this.currentY += distance;
    } else if (this.direction === 'bottom-top') {
      this.currentY -= distance;
    }
  }

  insertLegend(legend: Legend, svgData: string) {
    const height = 50;
    const width = 3;
    const margin = 5;
    const fontSize = 4
    const yStart = this.currentY + margin;

    this.doc.addSvgAsImage(svgData, this.xStart, yStart, width, height);
    this.doc.setFont(this.FONT_NAME, 'normal');
    this.doc.setFontSize(fontSize);

    Object.entries(legend.leftDescription).forEach(([position, value]) => {
      const positionN = Number(position);
      this.doc.text(value, this.xStart + width + 1, yStart + height - (height * positionN), {maxWidth: this.width, align: 'left', baseline: 'top'});
    });

    this.advanceY(height + margin);
  }
}

export class ReportGenerator {
  public readonly loading$: ReplaySubject<boolean> = new ReplaySubject<boolean>(1);
  public readonly working$: ReplaySubject<boolean> = new ReplaySubject<boolean>(1);

  private images: ReportImages = {
    s4eLogo: null,
    partners: null,
  };

  private svg: ReportSVG = {
    legend: null
  }

  constructor(private http: HttpClient,) {
    this.loading$.next(true);
    this.working$.next(false);
  }

  public generate(
    imageData: string,
    imageWidth: number,
    imageHeight: number,
    caption: string,
    notes: string,
    productName: string,
    sceneDate: string,
    pointResolution: number,
    legend: Legend|null): Observable<any> {
    // ignore this call if the generator is doing something
    return this.working$.pipe(
      take(1),
      filter(w => !w),
      tap(() => this.working$.next(true)),
      delay(0),
      tap(() => this.combineDocument(imageData, imageWidth, imageHeight, caption, notes, productName, sceneDate, pointResolution, legend)),
      delay(0),
      tap(() => this.working$.next(false))
    );
  }

  loadAssets(legend: string|null = null) {
    forkJoin([
      this.loadFont(import('./fonts/Ubuntu-Regular-normal')),
      this.loadFont(import('./fonts/Ubuntu-Regular-bold')),
      this.loadImage('./assets/images/logo_s4e_color.png', 's4eLogo'),
      this.loadImage('./assets/images/logo_partners.png', 'partners'),
      this.loadSvg(legend, 'legend')
    ]).subscribe(
      () =>
        this.loading$.next(false),
      error => this.loading$.error(error)
    );
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

  private loadSvg(url: string|null, key: keyof ReportSVG): Observable<any> {
    return url == null ? of(null) : this.http.get(url, {observe: 'response', responseType: 'text'}).pipe(tap(data => this.svg[key] = data.body));
  }

  private loadFont(fontPromise: Promise<any>): Observable<any> {
    return fromPromise(fontPromise.then((fontModule) => {
      fontModule.registerFont(JsPDF);
    }));
  }

  /**
   *
   * @return height of the added text box
   */

  private combineDocument(
    imageData: string,
    imageWidth: number,
    imageHeight: number,
    caption: string,
    notes: string,
    productName: string,
    sceneDate: string,
    pointResolution: number,
    legend: Legend|null) {
    if (notes.length > 740) {
      throw Error('description too long');
    }
    if (caption.length > 80) {
      throw Error('caption too long');
    }

    const doc = new JsPDF({
      orientation: 'landscape',
      unit: 'mm',
      format: 'a4'
    });

    const A4Width = 297;
    const A4Height = 210;
    const printImageHeight = imageHeight / imageWidth * A4Width;
    const margin = 10;
    const middleMargin = margin;

    const headerFontSize = 11.0;
    const noteFontSize = 8.0;
    const detailsFontSize = 8.0;
    const noteHeaderFontSize = 10.0;

    const distance = imageWidth * pointResolution;

    const [printWidth, printHeight] = this.calcMapPrintSize(A4Height, imageWidth, imageHeight, margin);
    const printWidthInMeters = printWidth / 1000.0;
    const scale = distance / printWidthInMeters;

    const composer = new LegendComposer(doc, A4Height, margin, A4Width - margin, A4Height - margin);

    composer.insertImage(this.images.s4eLogo);

    composer.insertTextBox(caption, {fontStyle: 'bold', fontSize: headerFontSize, margin: 5, align: 'center'});
    composer.insertTextBox('Opis', {fontStyle: 'bold', fontSize: noteHeaderFontSize, margin: 5});
    composer.insertTextBox(notes, {fontSize: noteFontSize, margin: 2.5});


    composer.insertTextBox(`1 : ${Math.round(scale)}`, {align: 'center', fontSize: 6, margin: 2});

    const pxPerMM = imageWidth / printWidth

    composer.insertScale(pxPerMM * pointResolution);

    if (legend) {
      composer.insertLegend(legend, this.svg.legend);
    }


    composer.insertTextBox('Szczegóły', {fontStyle: 'bold', fontSize: noteHeaderFontSize, margin: 4});

    if (productName) {
      composer.insertTextBox('Źródła danych', {fontSize: noteFontSize, fontStyle: 'bold', margin: 2});
      composer.insertTextBox(productName, {fontSize: noteFontSize});
    }

    if (sceneDate) {
      composer.insertTextBox('Data pozyskania', {fontSize: noteFontSize, fontStyle: 'bold', margin: 1});
      composer.insertTextBox(sceneDate, {fontSize: noteFontSize});
    }

    composer.insertTextBox('Raport wygenerowany', {fontSize: noteFontSize, fontStyle: 'bold', margin: 1});
    composer.insertTextBox(moment.utc().format('DD.MM.YYYY g. HH:mm UTC'), {fontSize: noteFontSize});

    composer.setFlow('bottom-top');

    composer.insertTextBox('© 2020 sat4envi', {fontSize: detailsFontSize, align: 'center'});
    composer.insertTextBox('Mapa została wygenerowana w aplikacji www.sat4envi.pl', {
      fontSize: detailsFontSize,
      align: 'center',
      margin: 2
    });

    composer.insertImage(this.images.partners, 3);

    this.drawMapImage(imageData, imageHeight, imageWidth, doc, margin, A4Height, middleMargin);

    doc.save(`RAPORT.${new Date().toISOString()}.pdf`);
  }

  private calcMapPrintSize(A4Height, imageWidth, imageHeight, margin): [number, number] {
    let imageScaleFactor = (A4Height - 2 * margin) / imageHeight;
    let printHeight = A4Height - 2 * margin;

    if (imageHeight > imageWidth) {
      imageScaleFactor = (A4Height - 2 * margin) / imageWidth;
      printHeight = imageHeight * imageScaleFactor;
    }

    let printWidth = imageWidth * imageScaleFactor;

    return [printWidth, printHeight]
  }

  private drawMapImage(imageData: string, imageHeight: number, imageWidth: number, doc: JsPDF, margin: number, A4Height: number, middleMargin: number) {
    doc.rect(margin, margin, A4Height - margin - middleMargin, A4Height - 2 * margin, null);
    doc.clip();

    const [printWidth, printHeight] = this.calcMapPrintSize(A4Height, imageWidth, imageHeight, margin);

    doc.addImage(imageData, 'PNG', A4Height / 2 - printWidth / 2, A4Height / 2 - printHeight / 2, printWidth, printHeight);

    doc.setDrawColor('#3a3a3a');
    doc.rect(margin, margin, A4Height - margin - middleMargin, A4Height - 2 * margin);
  }
}
